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

/**
 * Robust, production launcher Activity for Detective Genius.
 * Enforces strict portrait orientation, hardware acceleration,
 * and hosts the offline asset package with zero external dependencies.
 */
class MainActivity : Activity() {

    private var webView: WebView? = null

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
                }

                addJavascriptInterface(DetectiveNativeBridge(this@MainActivity), "DetectiveNative")

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
            webView?.destroy()
            webView = null
        } catch (_: Throwable) {}
        super.onDestroy()
    }
}

/**
 * JavaScript interface exposed to the WebView as window.DetectiveNative.
 * Bridges Android OS features (Vibration/Haptics, Share, Platform detection).
 */
class DetectiveNativeBridge(private val context: Context) {

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
}
