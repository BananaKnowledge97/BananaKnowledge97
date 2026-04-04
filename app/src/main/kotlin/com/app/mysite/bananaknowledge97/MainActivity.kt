package com.app.mysite.bananaknowledge97

import android.app.DownloadManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.ContextMenu
import android.view.View
import android.webkit.*
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar
    private lateinit var layoutOffline: LinearLayout
    private lateinit var btnRetry: Button

    private val backPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (webView.canGoBack()) {
                webView.goBack()
            } else {
                finish()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webView)
        progressBar = findViewById(R.id.progressBar)
        layoutOffline = findViewById(R.id.layoutOffline)
        btnRetry = findViewById(R.id.btnRetry)

        setupWebView()
        registerForContextMenu(webView)

        btnRetry.setOnClickListener { tryLoading() }
        tryLoading()

        onBackPressedDispatcher.addCallback(this, backPressedCallback)
    }

    override fun onResume() {
        super.onResume()
        backPressedCallback.isEnabled = true
    }

    private fun setupWebView() {
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            useWideViewPort = true
            loadWithOverviewMode = true
            cacheMode = WebSettings.LOAD_DEFAULT
            @Suppress("DEPRECATION")
            setRenderPriority(WebSettings.RenderPriority.HIGH)
        }

        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null)

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val url = request?.url.toString()
                val host = request?.url?.host ?: ""

                if (host.contains("bananaknowledge97.in")) {
                    return false
                }

                val externalApps = listOf("youtube.com", "youtu.be", "t.me", "tg://", "whatsapp://", "facebook.com", "instagram.com", "x.com", "twitter.com")
                if (externalApps.any { url.contains(it) }) {
                    try {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                        return true
                    } catch (e: Exception) {}
                }

                return try {
                    val customTabsIntent = CustomTabsIntent.Builder().setShowTitle(true).build()
                    customTabsIntent.launchUrl(this@MainActivity, Uri.parse(url))
                    true
                } catch (e: Exception) {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    true
                }
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                
            }

            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                if (request?.isForMainFrame == true) showError()
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    progressBar.setProgress(newProgress, true)
                } else {
                    progressBar.progress = newProgress
                }

                if (newProgress < 100) {
                    progressBar.visibility = View.VISIBLE
                    progressBar.alpha = 1f
                } else {
                    progressBar.animate()
                        .alpha(0f)
                        .translationY(-20f)
                        .setDuration(400)
                        .withEndAction {
                            progressBar.visibility = View.GONE
                            progressBar.translationY = 0f
                        }.start()
                }
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

    override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        val result = webView.hitTestResult
        val extra = result.extra ?: return

        when (result.type) {
            WebView.HitTestResult.IMAGE_TYPE -> {
                menu?.setHeaderTitle("Image Options")
                addMenuOptions(menu, extra, isImage = true, isLink = false)
            }
            WebView.HitTestResult.SRC_ANCHOR_TYPE -> {
                menu?.setHeaderTitle("Link Options")
                addMenuOptions(menu, extra, isImage = false, isLink = true)
            }
            WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE -> {
                menu?.setHeaderTitle("Image Link Options")
                addMenuOptions(menu, extra, isImage = true, isLink = true)
            }
        }
    }

    private fun addMenuOptions(menu: ContextMenu?, url: String, isImage: Boolean, isLink: Boolean) {
        if (isImage) {
            menu?.add(0, 1, 0, "Download Image")?.setOnMenuItemClickListener {
                downloadFile(url); true
            }
        }
        if (isLink) {
            menu?.add(0, 4, 0, "Open in Browser")?.setOnMenuItemClickListener {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))); true
            }
        }
        menu?.add(0, 2, 0, "Copy Link")?.setOnMenuItemClickListener {
            val cb = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            cb.setPrimaryClip(ClipData.newPlainText("URL", url))
            Toast.makeText(this, "Copied!", Toast.LENGTH_SHORT).show(); true
        }
        menu?.add(0, 3, 0, "Share")?.setOnMenuItemClickListener {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, url)
            }
            startActivity(Intent.createChooser(intent, "Share via")); true
        }
    }

    private fun downloadFile(url: String) {
        try {
            val request = DownloadManager.Request(Uri.parse(url))
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, URLUtil.guessFileName(url, null, null))
            (getSystemService(DOWNLOAD_SERVICE) as DownloadManager).enqueue(request)
            Toast.makeText(this, "Downloading...", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
