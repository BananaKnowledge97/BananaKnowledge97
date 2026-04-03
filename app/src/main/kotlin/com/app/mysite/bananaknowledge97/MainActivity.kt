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
import android.app.DownloadManager
import android.content.ClipData
import android.content.ClipboardManager
import android.os.Environment
import android.view.ContextMenu
import android.view.MenuItem
import android.webkit.URLUtil
import android.widget.Toast


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
        registerForContextMenu(webView)

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
    // Animate the progress change for a "liquid" feel
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
        progressBar.setProgress(newProgress, true)
    } else {
        progressBar.progress = newProgress
    }

    if (newProgress < 100) {
        progressBar.visibility = View.VISIBLE
        progressBar.alpha = 1f
    } else {
        // Smoothly fade and slide out when done
        progressBar.animate()
            .alpha(0f)
            .translationY(-20f) // Small upward slide
            .setDuration(400)
            .withEndAction { 
                progressBar.visibility = View.GONE 
                progressBar.translationY = 0f // Reset for next load
            }
            .start()
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
    val extra = result.extra ?: return // The URL or Image source

    // SCENARIO 1: It's a plain Image (not clickable)
    if (result.type == WebView.HitTestResult.IMAGE_TYPE) {
        menu?.setHeaderTitle("Image Options")
        addDownloadOption(menu, extra)
        addCopyOption(menu, extra, "Copy Image Link")
        addShareOption(menu, extra)
    } 
    
    // SCENARIO 2: It's a Link (text link)
    else if (result.type == WebView.HitTestResult.SRC_ANCHOR_TYPE) {
        menu?.setHeaderTitle("Link Options")
        addBrowserOption(menu, extra)
        addCopyOption(menu, extra, "Copy Link")
        addShareOption(menu, extra)
    }

    // SCENARIO 3: It's an Image that is ALSO a Link (Clickable Banner)
    else if (result.type == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {
        menu?.setHeaderTitle("Image Link Options")
        addDownloadOption(menu, extra) // Downloads the image
        addBrowserOption(menu, extra)  // Opens the link destination
        addCopyOption(menu, extra, "Copy Link")
        addShareOption(menu, extra)
    }
}

// 1. Action: Download
private fun addDownloadOption(menu: ContextMenu?, url: String) {
    menu?.add(0, 1, 0, "Download Image")?.setOnMenuItemClickListener {
        try {
            val request = DownloadManager.Request(Uri.parse(url))
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            val fileName = URLUtil.guessFileName(url, null, null)
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
            
            val dm = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
            dm.enqueue(request)
            Toast.makeText(this, "Download Started...", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
        true
    }
}

// 2. Action: Copy to Clipboard
private fun addCopyOption(menu: ContextMenu?, url: String, label: String) {
    menu?.add(0, 2, 0, label)?.setOnMenuItemClickListener {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("URL", url))
        Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT).show()
        true
    }
}

// 3. Action: Share
private fun addShareOption(menu: ContextMenu?, url: String) {
    menu?.add(0, 3, 0, "Share")?.setOnMenuItemClickListener {
        val i = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, url)
        }
        startActivity(Intent.createChooser(i, "Share via"))
        true
    }
}

// 4. Action: Open in External Browser
private fun addBrowserOption(menu: ContextMenu?, url: String) {
    menu?.add(0, 4, 0, "Open in Browser")?.setOnMenuItemClickListener {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        true
    }
}


}
