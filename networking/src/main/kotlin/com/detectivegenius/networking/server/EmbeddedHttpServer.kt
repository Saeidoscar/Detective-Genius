package com.detectivegenius.networking.server

import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import java.net.URLDecoder
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

/**
 * Lightweight, robust, pure standard library embedded HTTP REST server for
 * offline Wi-Fi and Hotspot multiplayer networking in Detective Genius.
 * Binds strictly to 0.0.0.0 and handles room radar beaconing, client joins,
 * heartbeat polling, password protection, and authoritative state synchronization.
 * Zero external dependencies: works on all Android versions and standard JVMs.
 */
class EmbeddedHttpServer(
    private val preferredPort: Int = 8080
) {
    private var serverSocket: ServerSocket? = null
    private var beaconSocket: DatagramSocket? = null
    private val isRunning = AtomicBoolean(false)
    private val threadPool = Executors.newCachedThreadPool()

    var activePort: Int = preferredPort
        private set

    var currentRoomId: String = "DG-1842"
    var roomName: String = "اتاق کارآگاهان"
    var roomPassword: String = ""
    var hostNickname: String = "کارآگاه (میزبان)"

    private val roomStateRef = AtomicReference<String>("{}")
    private val pendingJoins = ConcurrentLinkedQueue<String>() // Raw JSON strings
    private val pendingActions = ConcurrentLinkedQueue<String>() // Raw JSON strings
    private val connectedPlayers = ConcurrentHashMap<String, Long>() // playerId -> lastSeen

    fun start(
        roomId: String,
        name: String = "اتاق کارآگاهان",
        password: String = "",
        hostNick: String = "کارآگاه (میزبان)",
        initialRoomStateJson: String = "{}"
    ): Int {
        currentRoomId = roomId
        roomName = if (name.isNotBlank()) name.trim() else "اتاق کارآگاهان"
        roomPassword = password.trim()
        hostNickname = if (hostNick.isNotBlank()) hostNick.trim() else "کارآگاه (میزبان)"
        roomStateRef.set(initialRoomStateJson)

        if (isRunning.get()) {
            return activePort
        }

        pendingJoins.clear()
        pendingActions.clear()
        connectedPlayers.clear()

        // Try preferred port, fallback to alternatives if busy
        val candidatePorts = listOf(preferredPort, 8081, 8888, 9090, 0)
        var boundSocket: ServerSocket? = null

        for (p in candidatePorts) {
            try {
                boundSocket = ServerSocket(p, 50, InetAddress.getByName("0.0.0.0"))
                activePort = boundSocket.localPort
                break
            } catch (_: Throwable) {
                // Try next candidate
            }
        }

        if (boundSocket == null) {
            throw IllegalStateException("Failed to bind EmbeddedHttpServer on any port.")
        }

        serverSocket = boundSocket
        isRunning.set(true)

        // 1. TCP Server Thread
        threadPool.execute {
            while (isRunning.get() && !boundSocket.isClosed) {
                try {
                    val client = boundSocket.accept()
                    threadPool.execute {
                        handleClient(client)
                    }
                } catch (_: Throwable) {
                    break
                }
            }
        }

        // 2. UDP Radar Broadcast Beacon Thread (Every 1.2s to 255.255.255.255:8889)
        threadPool.execute {
            var bSocket: DatagramSocket? = null
            try {
                bSocket = DatagramSocket()
                bSocket.broadcast = true
                beaconSocket = bSocket

                while (isRunning.get()) {
                    try {
                        val playerCount = getConnectedPlayerCount()
                        val hasPw = roomPassword.isNotEmpty()
                        val safeName = roomName.replace("\"", "\\\"")
                        val safeNick = hostNickname.replace("\"", "\\\"")
                        val payload = """{"type":"DG_ROOM_BEACON","roomId":"$currentRoomId","roomName":"$safeName","hostNickname":"$safeNick","port":$activePort,"hasPassword":$hasPw,"playerCount":$playerCount,"timestamp":${System.currentTimeMillis()}}"""
                        val bytes = payload.toByteArray(Charsets.UTF_8)
                        val packet = DatagramPacket(bytes, bytes.size, InetAddress.getByName("255.255.255.255"), 8889)
                        bSocket.send(packet)
                    } catch (_: Throwable) {}

                    try {
                        Thread.sleep(1200)
                    } catch (_: InterruptedException) {
                        break
                    }
                }
            } catch (_: Throwable) {
            } finally {
                try { bSocket?.close() } catch (_: Throwable) {}
                beaconSocket = null
            }
        }

        return activePort
    }

    fun stop() {
        isRunning.set(false)
        try {
            serverSocket?.close()
        } catch (_: Throwable) {}
        serverSocket = null

        try {
            beaconSocket?.close()
        } catch (_: Throwable) {}
        beaconSocket = null
    }

    fun isServerRunning(): Boolean = isRunning.get() && serverSocket?.isClosed == false

    fun updateRoomState(stateJson: String) {
        roomStateRef.set(stateJson)
    }

    fun getRoomState(): String {
        return roomStateRef.get()
    }

    fun drainPendingJoins(): String {
        val list = mutableListOf<String>()
        while (true) {
            val item = pendingJoins.poll() ?: break
            list.add(item)
        }
        return "[" + list.joinToString(",") + "]"
    }

    fun drainPendingActions(): String {
        val list = mutableListOf<String>()
        while (true) {
            val item = pendingActions.poll() ?: break
            list.add(item)
        }
        return "[" + list.joinToString(",") + "]"
    }

    private fun getConnectedPlayerCount(): Int {
        return try {
            val state = roomStateRef.get()
            val matches = Regex("\"nickname\"\\s*:").findAll(state).count()
            if (matches > 0) matches else 1
        } catch (_: Throwable) { 1 }
    }

    private fun normalizePin(pin: String): String {
        return pin.replace("DG-", "", ignoreCase = true)
            .replace("dg-", "")
            .trim()
            .lowercase()
    }

    private fun extractJsonField(json: String, field: String): String {
        val regex = Regex("\"$field\"\\s*:\\s*\"([^\"]*)\"")
        return regex.find(json)?.groupValues?.get(1) ?: ""
    }

    private fun handleClient(socket: Socket) {
        try {
            socket.soTimeout = 8000
            val input = socket.getInputStream()
            val reader = BufferedReader(InputStreamReader(input, Charsets.UTF_8))
            val output = socket.getOutputStream()

            val requestLine = reader.readLine() ?: return
            val reqParts = requestLine.split(" ")
            if (reqParts.size < 2) return

            val method = reqParts[0].uppercase()
            val fullPath = reqParts[1]
            val path = fullPath.substringBefore("?")
            val queryStr = if (fullPath.contains("?")) fullPath.substringAfter("?") else ""

            val queryParams = mutableMapOf<String, String>()
            if (queryStr.isNotEmpty()) {
                queryStr.split("&").forEach { param ->
                    val kv = param.split("=", limit = 2)
                    if (kv.isNotEmpty()) {
                        val k = kv[0]
                        val v = if (kv.size > 1) {
                            try { URLDecoder.decode(kv[1], "UTF-8") } catch (_: Throwable) { kv[1] }
                        } else ""
                        queryParams[k] = v
                    }
                }
            }

            // Parse headers
            var contentLength = 0
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                val currentLine = line ?: break
                if (currentLine.isEmpty()) break
                val lower = currentLine.lowercase()
                if (lower.startsWith("content-length:")) {
                    contentLength = lower.substringAfter("content-length:").trim().toIntOrNull() ?: 0
                }
            }

            // Read payload body
            val body = if (contentLength > 0) {
                val buf = CharArray(contentLength)
                var readTotal = 0
                while (readTotal < contentLength) {
                    val read = reader.read(buf, readTotal, contentLength - readTotal)
                    if (read == -1) break
                    readTotal += read
                }
                String(buf, 0, readTotal)
            } else ""

            val response = when {
                method == "OPTIONS" -> {
                    HttpResponse(204, "text/plain", "")
                }
                path == "/api/ping" || path == "/api/host-info" -> {
                    handlePing()
                }
                path == "/api/join" && method == "POST" -> {
                    handleJoin(body)
                }
                path == "/api/poll" || path == "/api/room/state" -> {
                    handlePoll(queryParams)
                }
                path == "/api/action" && method == "POST" -> {
                    handleAction(body)
                }
                (path == "/api/sync" || path == "/api/room/sync") && method == "POST" -> {
                    handleSync(body)
                }
                else -> {
                    HttpResponse(404, "application/json", "{\"status\":\"error\",\"message\":\"Not Found\"}")
                }
            }

            val respBytes = response.body.toByteArray(Charsets.UTF_8)
            val header = buildString {
                append("HTTP/1.1 ${response.code} ${if (response.code == 200) "OK" else if (response.code == 204) "No Content" else "Response"}\r\n")
                append("Content-Type: ${response.contentType}\r\n")
                append("Content-Length: ${respBytes.size}\r\n")
                append("Access-Control-Allow-Origin: *\r\n")
                append("Access-Control-Allow-Methods: GET, POST, OPTIONS\r\n")
                append("Access-Control-Allow-Headers: *\r\n")
                append("Connection: close\r\n\r\n")
            }

            output.write(header.toByteArray(Charsets.UTF_8))
            if (respBytes.isNotEmpty()) {
                output.write(respBytes)
            }
            output.flush()
        } catch (_: Throwable) {
        } finally {
            try { socket.close() } catch (_: Throwable) {}
        }
    }

    private fun handlePing(): HttpResponse {
        val now = System.currentTimeMillis()
        val safeName = roomName.replace("\"", "\\\"")
        val safeNick = hostNickname.replace("\"", "\\\"")
        val hasPw = roomPassword.isNotEmpty()
        val count = getConnectedPlayerCount()

        val json = """{"status":"ok","roomId":"$currentRoomId","roomName":"$safeName","hostNickname":"$safeNick","port":$activePort,"hasPassword":$hasPw,"playerCount":$count,"isRunning":true,"timestamp":$now}"""
        return HttpResponse(200, "application/json", json)
    }

    private fun handleJoin(body: String): HttpResponse {
        // 1. Password check if room is password protected
        if (roomPassword.isNotEmpty()) {
            val suppliedPw = extractJsonField(body, "password")
            if (suppliedPw != roomPassword) {
                val err = """{"status":"error","code":"WRONG_PASSWORD","message":"رمز عبور اتاق اشتباه است."}"""
                return HttpResponse(401, "application/json", err)
            }
        }

        // 2. Room ID check if provided
        val reqPin = extractJsonField(body, "roomId")
        if (reqPin.isNotEmpty() && normalizePin(reqPin) != normalizePin(currentRoomId)) {
            val err = """{"status":"error","code":"INVALID_ROOM_ID","message":"شناسه اتاق همخوانی ندارد."}"""
            return HttpResponse(400, "application/json", err)
        }

        val playerIdMatch = Regex("\"id\"\\s*:\\s*\"([^\"]*)\"").find(body)
        val playerId = playerIdMatch?.groupValues?.get(1) ?: "p_${System.currentTimeMillis()}"

        connectedPlayers[playerId] = System.currentTimeMillis()

        // Extract player json block
        val playerBlockRegex = Regex("\"player\"\\s*:\\s*(\\{[^}]+\\})")
        val playerBlock = playerBlockRegex.find(body)?.groupValues?.get(1) ?: body

        pendingJoins.add(playerBlock)

        val state = roomStateRef.get()
        val safeName = roomName.replace("\"", "\\\"")
        val resp = """{"status":"ok","roomId":"$currentRoomId","roomName":"$safeName","playerId":"$playerId","gameState":$state}"""
        return HttpResponse(200, "application/json", resp)
    }

    private fun handlePoll(query: Map<String, String>): HttpResponse {
        val playerId = query["playerId"]
        if (!playerId.isNullOrBlank()) {
            connectedPlayers[playerId] = System.currentTimeMillis()
        }

        val now = System.currentTimeMillis()
        val state = roomStateRef.get()
        val safeName = roomName.replace("\"", "\\\"")
        val resp = """{"status":"ok","roomId":"$currentRoomId","roomName":"$safeName","timestamp":$now,"gameState":$state}"""
        return HttpResponse(200, "application/json", resp)
    }

    private fun handleAction(body: String): HttpResponse {
        pendingActions.add(body)
        val state = roomStateRef.get()
        val resp = """{"status":"ok","gameState":$state}"""
        return HttpResponse(200, "application/json", resp)
    }

    private fun handleSync(body: String): HttpResponse {
        val stateRegex = Regex("\"gameState\"\\s*:\\s*(\\{[\\s\\S]*\\})")
        val matchedState = stateRegex.find(body)?.groupValues?.get(1)
        val newState = matchedState ?: body
        roomStateRef.set(newState)
        return HttpResponse(200, "application/json", """{"status":"ok"}""")
    }

    data class HttpResponse(
        val code: Int,
        val contentType: String,
        val body: String
    )
}
