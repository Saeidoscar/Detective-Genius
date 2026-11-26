package com.detectivegenius

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.View
import android.view.WindowManager
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import com.detectivegenius.networking.server.EmbeddedHttpServer
import java.io.InputStream
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.HttpURLConnection
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.URL

/**
 * Robust, production launcher Activity for Detective Genius.
 * Enforces strict portrait orientation, hardware acceleration,
 * and hosts the offline asset package with embedded HTTP multiplayer server.
 */
class MainActivity : Activity() {

    private var webView: WebView? = null
    private var nativeBridge: DetectiveNativeBridge? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            // STRICT INVARIANT: Force portrait orientation exclusively!
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        } catch (_: Throwable) {}

        try {
            // Enable hardware acceleration for fluid 60fps animations
            window.setFlags(
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
            )
            window.statusBarColor = 0xFF070B18.toInt()
            window.navigationBarColor = 0xFF070B18.toInt()
        } catch (_: Throwable) {}

        val rootLayout = FrameLayout(this).apply {
            setBackgroundColor(0xFF070B18.toInt())
        }

        try {
            val bridge = DetectiveNativeBridge(this)
            nativeBridge = bridge

            val wv = WebView(this).apply {
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
                setBackgroundColor(0xFF070B18.toInt())
                isVerticalScrollBarEnabled = false
                isHorizontalScrollBarEnabled = false
                overScrollMode = View.OVER_SCROLL_NEVER

                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    databaseEnabled = true
                    allowFileAccess = true
                    allowContentAccess = true
                    @Suppress("DEPRECATION")
                    allowFileAccessFromFileURLs = true
                    @Suppress("DEPRECATION")
                    allowUniversalAccessFromFileURLs = true
                    cacheMode = WebSettings.LOAD_DEFAULT
                    useWideViewPort = true
                    loadWithOverviewMode = true
                    mediaPlaybackRequiresUserGesture = false
                    defaultTextEncodingName = "UTF-8"
                    mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                }

                addJavascriptInterface(bridge, "DetectiveNative")

                webChromeClient = WebChromeClient()
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                        return false
                    }
                }
            }

            rootLayout.addView(wv)
            webView = wv
            wv.loadUrl("file:///android_asset/www/index.html")
        } catch (t: Throwable) {
            // Graceful fallback if device webview is temporarily unavailable
            val errorView = android.widget.TextView(this).apply {
                text = "Detective Genius is initializing...\nPlease relaunch if updating."
                setTextColor(0xFFF8FAFC.toInt())
                textSize = 16f
                setPadding(40, 40, 40, 40)
            }
            rootLayout.addView(errorView)
        }

        setContentView(rootLayout)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        val wv = webView
        if (wv != null && wv.canGoBack()) {
            wv.goBack()
        } else {
            @Suppress("DEPRECATION")
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        try {
            nativeBridge?.stopHostServer()
            webView?.destroy()
            webView = null
        } catch (_: Throwable) {}
        super.onDestroy()
    }
}

/**
 * JavaScript interface exposed to the WebView as window.DetectiveNative.
 * Bridges Android OS features (Vibration/Haptics, Share, Platform detection,
 * embedded HTTP server for Hotspot/LAN multiplayer, radar scanner, and native HTTP client).
 */
class DetectiveNativeBridge(private val context: Context) {

    private val httpServer = EmbeddedHttpServer(preferredPort = 8080)

    @JavascriptInterface
    fun startHostServer(
        port: Int,
        roomId: String,
        roomName: String,
        roomPassword: String,
        hostNickname: String,
        initialData: String
    ): String {
        return try {
            val actualPort = httpServer.start(
                roomId = roomId,
                name = roomName,
                password = roomPassword,
                hostNick = hostNickname,
                initialRoomStateJson = initialData
            )
            val safeName = roomName.replace("\"", "\\\"")
            """{"status":"ok","port":$actualPort,"roomId":"$roomId","roomName":"$safeName"}"""
        } catch (t: Throwable) {
            val safeMsg = (t.message ?: "Failed to start server").replace("\"", "\\\"")
            """{"status":"error","message":"$safeMsg"}"""
        }
    }

    // Overload for backward compatibility
    @JavascriptInterface
    fun startHostServer(port: Int, roomId: String, initialData: String): String {
        return startHostServer(port, roomId, "اتاق کارآگاهان", "", "کارآگاه (میزبان)", initialData)
    }

    @JavascriptInterface
    fun stopHostServer(): Boolean {
        return try {
            httpServer.stop()
            true
        } catch (_: Throwable) {
            false
        }
    }

    @JavascriptInterface
    fun isHostServerRunning(): Boolean {
        return httpServer.isServerRunning()
    }

    @JavascriptInterface
    fun syncRoomState(stateJson: String): Boolean {
        return try {
            httpServer.updateRoomState(stateJson)
            true
        } catch (_: Throwable) {
            false
        }
    }

    @JavascriptInterface
    fun getRoomState(): String {
        return httpServer.getRoomState()
    }

    @JavascriptInterface
    fun drainPendingJoins(): String {
        return httpServer.drainPendingJoins()
    }

    @JavascriptInterface
    fun drainPendingActions(): String {
        return httpServer.drainPendingActions()
    }

    /**
     * Automatic room discovery radar:
     * 1. Probes common local gateways (192.168.43.1:8080, 127.0.0.1)
     * 2. Listens for UDP broadcast beacons on port 8889
     * Returns JSON array string of discovered active rooms.
     */
    @JavascriptInterface
    fun scanLocalRooms(): String {
        val foundRooms = mutableListOf<String>()
        val testedEndpoints = mutableSetOf<String>()

        // 1. Probing candidate host IPs: Hotspot gateway and localhost
        val candidateIps = listOf("192.168.43.1", "127.0.0.1", "10.0.2.2")
        val ports = listOf(8080, 8081, 8888, 3000)

        for (ip in candidateIps) {
            for (p in ports) {
                val key = "$ip:$p"
                if (testedEndpoints.add(key)) {
                    val roomInfo = probeHttpRoom(ip, p)
                    if (roomInfo != null) {
                        foundRooms.add(roomInfo)
                    }
                }
            }
        }

        // 2. Listen for UDP broadcast beacon on port 8889 for up to 1000ms
        try {
            val udpSocket = DatagramSocket(8889)
            udpSocket.soTimeout = 1000
            val buf = ByteArray(1024)
            val packet = DatagramPacket(buf, buf.size)
            try {
                udpSocket.receive(packet)
                val msg = String(packet.data, 0, packet.length, Charsets.UTF_8)
                val senderIp = packet.address?.hostAddress ?: "192.168.43.1"
                if (msg.contains("DG_ROOM_BEACON")) {
                    val portMatch = Regex("\"port\"\\s*:\\s*([0-9]+)").find(msg)
                    val p = portMatch?.groupValues?.get(1)?.toIntOrNull() ?: 8080
                    val roomJson = probeHttpRoom(senderIp, p) ?: msg.replace("DG_ROOM_BEACON", "ok")
                    foundRooms.add(roomJson)
                }
            } catch (_: Throwable) {
                // Timeout listening for beacon
            } finally {
                udpSocket.close()
            }
        } catch (_: Throwable) {}

        return "[" + foundRooms.distinct().joinToString(",") + "]"
    }

    private fun probeHttpRoom(ip: String, port: Int): String? {
        var conn: HttpURLConnection? = null
        return try {
            val url = URL("http://$ip:$port/api/ping")
            conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 600
            conn.readTimeout = 600
            conn.requestMethod = "GET"
            if (conn.responseCode in 200..299) {
                val body = conn.inputStream.bufferedReader(Charsets.UTF_8).readText()
                if (body.contains("\"status\":\"ok\"")) {
                    val withIp = if (!body.contains("\"hostIp\"")) {
                        body.replaceFirst("{", "{\"hostIp\":\"$ip\",")
                    } else body
                    withIp
                } else null
            } else null
        } catch (_: Throwable) {
            null
        } finally {
            try { conn?.disconnect() } catch (_: Throwable) {}
        }
    }

    /**
     * Native HTTP request implementation using standard Java HttpURLConnection.
     * Guaranteed to bypass WebView CORS, file:/// scheme barriers, and sandbox limitations.
     */
    @JavascriptInterface
    fun httpRequest(method: String, urlString: String, body: String?): String {
        var conn: HttpURLConnection? = null
        return try {
            val url = URL(urlString)
            conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = method.uppercase()
            conn.connectTimeout = 4000
            conn.readTimeout = 4000
            conn.setRequestProperty("Accept", "application/json")

            if (body != null && body.isNotEmpty() && (method.equals("POST", ignoreCase = true) || method.equals("PUT", ignoreCase = true))) {
                conn.doOutput = true
                conn.setRequestProperty("Content-Type", "application/json; charset=utf-8")
                conn.outputStream.use { os ->
                    os.write(body.toByteArray(Charsets.UTF_8))
                }
            }

            val code = conn.responseCode
            val stream: InputStream? = if (code in 200..299) conn.inputStream else conn.errorStream
            val respStr = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() } ?: "{}"
            respStr
        } catch (t: Throwable) {
            val safeMsg = (t.message ?: "Network error connecting to host").replace("\"", "\\\"")
            """{"status":"error","code":"NETWORK_ERROR","message":"$safeMsg"}"""
        } finally {
            try { conn?.disconnect() } catch (_: Throwable) {}
        }
    }

    @JavascriptInterface
    fun vibrate(durationMs: Long) {
        try {
            @Suppress("DEPRECATION")
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(
                        VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE)
                    )
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(durationMs)
                }
            }
        } catch (_: Throwable) {}
    }

    @JavascriptInterface
    fun shareRoom(roomCode: String, message: String) {
        try {
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, "$message: $roomCode")
                type = "text/plain"
            }
            context.startActivity(Intent.createChooser(sendIntent, "Detective Genius"))
        } catch (_: Throwable) {}
    }

    @JavascriptInterface
    fun getPlatform(): String {
        return "Android"
    }

    @JavascriptInterface
    fun getAppVersion(): String {
        return "1.0.1"
    }

    /**
     * Resolves the LAN / Hotspot IP address.
     * Strictly filters out mobile carrier / cellular interfaces (rmnet, ccmni, pdp, lte)
     * so cellular CGNAT IPs (e.g. 10.253.x.x) are never mistakenly used as the host IP!
     * Prioritizes standard Android Hotspot gateway 192.168.43.1 or active Wi-Fi interface.
     */
    @JavascriptInterface
    fun getDeviceIp(): String {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            var hotspotIp: String? = null
            var wifiIp: String? = null

            while (interfaces.hasMoreElements()) {
                val iface = interfaces.nextElement()
                val name = iface.name.lowercase()

                // STRICT FILTER: Exclude cellular/mobile carrier, VPN, and dummy interfaces!
                if (name.contains("rmnet") || name.contains("ccmni") || name.contains("pdp") ||
                    name.contains("lte") || name.contains("data") || name.contains("radio") ||
                    name.contains("wwan") || name.contains("tun") || name.contains("ppp") ||
                    name.contains("dummy")) {
                    continue
                }

                val addresses = iface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val addr = addresses.nextElement()
                    if (!addr.isLoopbackAddress && addr is java.net.Inet4Address) {
                        val ip = addr.hostAddress ?: continue
                        if (ip.startsWith("127.")) continue

                        // Exclude carrier CGNAT IPs (10.x.x.x or 100.x.x.x when not on internal wlan)
                        if (name.contains("wlan") || name.contains("eth") || name.contains("en")) {
                            if (wifiIp == null) wifiIp = ip
                        }

                        // Hotspot gateway check
                        if (ip == "192.168.43.1" || ip.startsWith("192.168.43.") ||
                            name.contains("ap") || name.contains("swlan") || name.contains("hotspot") || name.contains("tether")) {
                            hotspotIp = ip
                        }
                    }
                }
            }

            // Return Hotspot IP if active, or Wi-Fi IP, or standard Android Hotspot IP 192.168.43.1
            return hotspotIp ?: wifiIp ?: "192.168.43.1"
        } catch (_: Throwable) {
            return "192.168.43.1"
        }
    }
}
