package app.banana.bananaknowledge97

import android.app.DownloadManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.ContextMenu
import android.view.View
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle 
import androidx.lifecycle.LifecycleEventObserver 
import app.banana.bananaknowledge97.ui.theme.BananaTheme
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Handle the Android 12+ Splash Screen API
        installSplashScreen()
        super.onCreate(savedInstanceState)
        
        // Enable edge-to-edge drawing (content appears behind status/navigation bars)
        enableEdgeToEdge()
        
        setContent {
            BananaTheme {
                MainScreen()
            }
        }
    }

    /**
     * Context Menu: Triggers when a user long-presses an image or a link in the WebView.
     */
    override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        val result = (v as? WebView)?.hitTestResult ?: return
        val extra = result.extra ?: return

        when (result.type) {
            // Handle Long-press on Images
            WebView.HitTestResult.IMAGE_TYPE -> {
                menu?.setHeaderTitle("🖼️ Image Actions")
                menu?.add(0, 1, 0, "📥 Download Image")?.setOnMenuItemClickListener { downloadImage(extra); true }
                menu?.add(0, 2, 1, "🔗 Copy Image Link")?.setOnMenuItemClickListener { copyToClipboard(extra); true }
            }
            // Handle Long-press on Hyperlinks
            WebView.HitTestResult.SRC_ANCHOR_TYPE, WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE -> {
                val displayLink = if (extra.length > 35) extra.take(32) + "..." else extra
                menu?.setHeaderTitle("🔗 $displayLink")
                if (result.type == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {
                    menu?.add(0, 1, 0, "📥 Download Image/Icon")?.setOnMenuItemClickListener { downloadImage(extra); true }
                }
                menu?.add(0, 4, 1, "📋 Copy Link Address")?.setOnMenuItemClickListener { copyToClipboard(extra); true }
                menu?.add(0, 5, 2, "📤 Share Link")?.setOnMenuItemClickListener { shareText(extra); true }
                menu?.add(0, 6, 3, "🌐 Open in Browser")?.setOnMenuItemClickListener { 
                    try { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(extra))) } catch (e: Exception) {}
                    true 
                }
            }
        }
    }

    private fun shareText(text: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"; putExtra(Intent.EXTRA_TEXT, text)
        }
        startActivity(Intent.createChooser(intent, "Share via"))
    }

    private fun copyToClipboard(text: String) {
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("Link", text))
    }

    private fun downloadImage(url: String) {
        val request = DownloadManager.Request(Uri.parse(url))
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "banana_${System.currentTimeMillis()}.jpg")
        (getSystemService(DOWNLOAD_SERVICE) as DownloadManager).enqueue(request)
    }
}

@Composable
fun MainScreen() {
    // State management for WebView control and UI visibility
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var canGoBack by remember { mutableStateOf(false) }
    var isUiVisible by remember { mutableStateOf(true) }
    var isMenuExpanded by remember { mutableStateOf(false) }
    val manualLoading = remember { mutableStateOf(false) }
    
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current

    /**
     * Lifecycle Observer: Ensures the WebView pauses and resumes correctly
     * with the Activity to save battery and memory.
     */
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                webViewInstance?.apply {
                    onResume()
                    requestLayout()
                    invalidate()
                }
            } else if (event == Lifecycle.Event.ON_PAUSE) {
                webViewInstance?.onPause()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Handles the Android System Back Button/Gesture
    BackHandler(enabled = canGoBack) { webViewInstance?.goBack() }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
        
        // The main content area: Custom WebView component
        BananaWebView(
            url = "https://bananaknowledge97.in",
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
                .navigationBarsPadding()
                // Adjusts padding when the top "Island Bar" is hidden/shown
                .absolutePadding(top = if (isUiVisible) 26.dp else 0.dp), 
            isManualLoading = manualLoading,
            onCreated = { webViewInstance = it },
            onStateChanged = { canGoBack = it }
        )

        /**
         * Top Island Bar: Displays the brand name.
         * Slides in/out based on the 'isUiVisible' state.
         */
        AnimatedVisibility(
            visible = isUiVisible,
            modifier = Modifier.align(Alignment.TopCenter).windowInsetsPadding(WindowInsets.statusBars),
            enter = slideInVertically { -it } + fadeIn(),
            exit = slideOutVertically { -it } + fadeOut()
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(0.74f).height(24.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                shape = RoundedCornerShape(14.dp),
                shadowElevation = 12.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("BananaKnowledge97", fontSize = 13.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        /**
         * Navigation Back FAB: Only appears when the user can actually navigate back.
         */
        AnimatedVisibility(
            visible = canGoBack,
            modifier = Modifier.align(Alignment.BottomStart).navigationBarsPadding().padding(16.dp),
            enter = scaleIn() + fadeIn(),
            exit = scaleOut() + fadeOut()
        ) {
            FloatingActionButton(
                onClick = { webViewInstance?.goBack() }, 
                containerColor = MaterialTheme.colorScheme.primaryContainer, 
                shape = CircleShape
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
            }
        }

        /**
         * Side Menu (Top Left): Contains Home, Refresh, and Fullscreen toggle.
         */
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .windowInsetsPadding(WindowInsets.statusBars)
                .absolutePadding(top = if (isUiVisible) 36.dp else 8.dp, left = 8.dp)
        ) {
            // Animates the menu icon rotation when opened
            val rotation by animateFloatAsState(if (isMenuExpanded) 90f else 0f, label = "rotation")
            
            FloatingActionButton(
                onClick = { isMenuExpanded = !isMenuExpanded }, 
                modifier = Modifier.size(52.dp), 
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = if (isMenuExpanded) Icons.Default.Close else Icons.Default.Menu, 
                    contentDescription = null, 
                    modifier = Modifier.rotate(rotation)
                )
            }

            // Expanding list of menu options
            AnimatedVisibility(
                visible = isMenuExpanded, 
                enter = expandVertically() + fadeIn(), 
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 10.dp, start = 6.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Home Button
                    MenuSmallButton(Icons.Default.Home, MaterialTheme.colorScheme.surfaceVariant) { 
                        manualLoading.value = true
                        webViewInstance?.loadUrl("https://bananaknowledge97.in")
                        isMenuExpanded = false 
                    }

                    // Refresh Button
                    MenuSmallButton(Icons.Default.Refresh, MaterialTheme.colorScheme.secondaryContainer) { 
                        manualLoading.value = true
                        webViewInstance?.reload()
                        isMenuExpanded = false 
                    }

                    // Fullscreen/UI Toggle
                    MenuSmallButton(
                        icon = if (isUiVisible) Icons.Default.FullscreenExit else Icons.Default.Fullscreen, 
                        color = MaterialTheme.colorScheme.tertiaryContainer
                    ) { 
                        isUiVisible = !isUiVisible
                        isMenuExpanded = false 
                    }
                }
            }
        }

        /**
         * Share Button (Top Right): Quickly share the current page URL.
         */
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .windowInsetsPadding(WindowInsets.statusBars)
                .absolutePadding(top = if (isUiVisible) 36.dp else 8.dp, right = 8.dp)
        ) {
            FloatingActionButton(
                onClick = { 
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, webViewInstance?.url ?: "https://bananaknowledge97.in")
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Share Page"))
                },
                modifier = Modifier.size(52.dp),
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Share, "Share")
            }
        }
    }
}

/**
 * Reusable component for the small buttons inside the side menu.
 */
@Composable
fun MenuSmallButton(icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, onClick: () -> Unit) {
    SmallFloatingActionButton(
        onClick = onClick, 
        containerColor = color, 
        shape = RoundedCornerShape(12.dp)
    ) {
        Icon(icon, null, modifier = Modifier.size(20.dp))
    }
}
