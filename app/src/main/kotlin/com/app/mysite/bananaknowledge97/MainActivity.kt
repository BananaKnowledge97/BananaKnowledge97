package com.app.mysite.bananaknowledge97

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.*
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar
    private lateinit var layoutOffline: LinearLayout
    private lateinit var btnRetry: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webView)
        progressBar = findViewById(R.id.progressBar)
        layoutOffline = findViewById(R.id.layoutOffline)
        btnRetry = findViewById(R.id.btnRetry)

        setupWebView()

        btnRetry.setOnClickListener { tryLoading() }
        tryLoading()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.canGoBack()) {
                    webView.goBack()
                } else {
                    isEnabled = false 
                    onBackPressedDispatcher.onBackPressed() 
                }
            }
        })
    }

    private fun setupWebView() {
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            useWideViewPort = true
            loadWithOverviewMode = true
            cacheMode = WebSettings.LOAD_DEFAULT
        }

        webView.webViewClient = object : WebViewClient() {
            
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val url = request?.url.toString()
                val host = request?.url?.host

                // 1. STAY: Keep your website links inside the app
                if (host != null && host.contains("bananaknowledge97.in")) {
                    return false 
                }

                // 2. JUMP: Instant-open popular apps if installed
                if (url.contains("youtube.com") || url.contains("youtu.be") || 
                    url.contains("t.me") || url.startsWith("tg://") || 
                    url.startsWith("whatsapp://") || 
                    url.contains("facebook.com") || 
                    url.contains("instagram.com") || 
                    url.contains("twitter.com") || url.contains("x.com")) {
                    
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        startActivity(intent)
                        return true 
                    } catch (e: Exception) {
                        // If app isn't installed, let it fall through to Step 3
                    }
                }

                // 3. SLIDE: Use Custom Tabs for all other external links
                try {
                    val builder = CustomTabsIntent.Builder()
                    builder.setShowTitle(true)
                    val customTabsIntent = builder.build()
                    customTabsIntent.launchUrl(this@MainActivity, Uri.parse(url))
                    return true
                } catch (e: Exception) {
                    // Ultimate fallback to system browser
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    startActivity(intent)
                    return true
                }
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                webView.animate().alpha(1.0f).setDuration(300).start()
            }

            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                if (request?.isForMainFrame == true) {
                    showError()
                }
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                progressBar.progress = newProgress
                progressBar.visibility = if (newProgress < 100) View.VISIBLE else View.GONE
            }
        }
    }

    private fun tryLoading() {
        if (isNetworkAvailable()) {
            layoutOffline.visibility = View.GONE
            webView.visibility = View.VISIBLE
            webView.loadUrl("https://bananaknowledge97.in")
        } else {
            showError()
        }
    }

    private fun showError() {
        webView.visibility = View.GONE
        layoutOffline.visibility = View.VISIBLE
        progressBar.visibility = View.GONE
    }

    private fun isNetworkAvailable(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
