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
import android.view.WindowManager
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout

/**
 * Main launcher Activity for Detective Genius.
 * Enforces strict portrait orientation, hardware acceleration,
 * and hosts the offline local asset web environment with native bridges.
 */
class MainActivity : Activity() {

    private lateinit var webView: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // STRICT INVARIANT: Force portrait orientation exclusively!
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        // Enable hardware acceleration for fluid 60fps animations
        window.setFlags(
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
        )

        // Immersive dark status bar matching Detective Genius palette
        window.statusBarColor = 0xFF070B18.toInt()
        window.navigationBarColor = 0xFF070B18.toInt()

        val rootLayout = FrameLayout(this)
        rootLayout.setBackgroundColor(0xFF070B18.toInt())

        webView = WebView(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(0xFF070B18.toInt())
            isVerticalScrollBarEnabled = false
            isHorizontalScrollBarEnabled = false

            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                databaseEnabled = true
                allowFileAccess = true
                allowContentAccess = true
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

        rootLayout.addView(webView)
        setContentView(rootLayout)

        // Load offline packaged game bundle from Android assets
        webView.loadUrl("file:///android_asset/www/index.html")
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (this::webView.isInitialized && webView.canGoBack()) {
            webView.goBack()
        } else {
            @Suppress("DEPRECATION")
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        if (this::webView.isInitialized) {
            webView.destroy()
        }
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
        } catch (_: Exception) {
            // Graceful fallback if device lacks vibrator permission or hardware
        }
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
        } catch (_: Exception) {
        }
    }

    @JavascriptInterface
    fun getPlatform(): String {
        return "Android"
    }

    @JavascriptInterface
    fun getAppVersion(): String {
        return "1.0.0"
    }
}
