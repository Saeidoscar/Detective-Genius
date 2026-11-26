package com.detectivegenius.networking.server

import java.io.BufferedReader
import java.io.InputStreamReader
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
 * Binds strictly to 0.0.0.0 and handles client joins, heartbeat polling,
 * real-time action forwarding, and authoritative state synchronization.
 * Zero external dependencies: works on all Android versions and standard JVMs.
 */
class EmbeddedHttpServer(
    private val preferredPort: Int = 8080
) {
    private var serverSocket: ServerSocket? = null
    private val isRunning = AtomicBoolean(false)
    private val threadPool = Executors.newCachedThreadPool()

    var activePort: Int = preferredPort
        private set

    var currentRoomId: String = "DG-4829"
    private val roomStateRef = AtomicReference<String>("{}")
    private val pendingJoins = ConcurrentLinkedQueue<String>() // Raw JSON strings
    private val pendingActions = ConcurrentLinkedQueue<String>() // Raw JSON strings
    private val connectedPlayers = ConcurrentHashMap<String, Long>() // playerId -> lastSeen

    fun start(roomId: String, initialRoomStateJson: String): Int {
        if (isRunning.get()) {
            currentRoomId = roomId
            roomStateRef.set(initialRoomStateJson)
            return activePort
        }

        currentRoomId = roomId
        roomStateRef.set(initialRoomStateJson)
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

        return activePort
    }

    fun stop() {
        isRunning.set(false)
        try {
            serverSocket?.close()
        } catch (_: Throwable) {}
        serverSocket = null
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
        val json = """{"status":"ok","roomId":"$currentRoomId","port":$activePort,"isRunning":true,"timestamp":$now}"""
        return HttpResponse(200, "application/json", json)
    }

    private fun handleJoin(body: String): HttpResponse {
        val reqPin = extractJsonField(body, "roomId")
        if (reqPin.isNotEmpty() && normalizePin(reqPin) != normalizePin(currentRoomId)) {
            val err = """{"status":"error","code":"INVALID_ROOM_ID","message":"Room ID mismatch"}"""
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
        val resp = """{"status":"ok","roomId":"$currentRoomId","playerId":"$playerId","gameState":$state}"""
        return HttpResponse(200, "application/json", resp)
    }

    private fun handlePoll(query: Map<String, String>): HttpResponse {
        val playerId = query["playerId"]
        if (!playerId.isNullOrBlank()) {
            connectedPlayers[playerId] = System.currentTimeMillis()
        }

        val now = System.currentTimeMillis()
        val state = roomStateRef.get()
        val resp = """{"status":"ok","roomId":"$currentRoomId","timestamp":$now,"gameState":$state}"""
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
