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
import java.net.HttpURLConnection
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
 * embedded HTTP server for Hotspot/LAN multiplayer, and native HTTP client).
 */
class DetectiveNativeBridge(private val context: Context) {

    private val httpServer = EmbeddedHttpServer(preferredPort = 8080)

    @JavascriptInterface
    fun startHostServer(port: Int, roomId: String, initialData: String): String {
        return try {
            val actualPort = httpServer.start(roomId, initialData)
            """{"status":"ok","port":$actualPort,"roomId":"$roomId"}"""
        } catch (t: Throwable) {
            val safeMsg = (t.message ?: "Failed to start server").replace("\"", "\\\"")
            """{"status":"error","message":"$safeMsg"}"""
        }
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

    @JavascriptInterface
    fun getDeviceIp(): String {
        try {
            val interfaces = java.net.NetworkInterface.getNetworkInterfaces()
            var hotspotIp: String? = null
            var wifiIp: String? = null
            var otherIp: String? = null

            while (interfaces.hasMoreElements()) {
                val iface = interfaces.nextElement()
                val name = iface.name.lowercase()
                val addresses = iface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val addr = addresses.nextElement()
                    if (!addr.isLoopbackAddress && addr is java.net.Inet4Address) {
                        val ip = addr.hostAddress ?: continue
                        if (ip.startsWith("127.")) continue

                        // Prioritize standard Android hotspot gateway 192.168.43.x or ap interface
                        if (ip.startsWith("192.168.43.") || name.contains("ap") || name.contains("swlan") || name.contains("hotspot")) {
                            hotspotIp = ip
                        } else if (name.contains("wlan") || ip.startsWith("192.168.")) {
                            if (wifiIp == null) wifiIp = ip
                        } else {
                            if (otherIp == null) otherIp = ip
                        }
                    }
                }
            }
            return hotspotIp ?: wifiIp ?: otherIp ?: "192.168.43.1"
        } catch (_: Throwable) {
            return "192.168.43.1"
        }
    }
}
