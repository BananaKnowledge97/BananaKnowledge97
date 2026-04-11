package app.banana.bananaknowledge97

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import kotlin.random.Random

/**
 * Data class to manage the background animation particles (symbols) during loading.
 */
data class Symbol(
    val text: String,
    val initialOffset: Offset,
    val fontSize: Float,
    val initialRotation: Float
)

@Composable
fun BananaWebView(
    url: String,
    modifier: Modifier = Modifier,
    isManualLoading: MutableState<Boolean>,
    onCreated: (WebView) -> Unit,
    onStateChanged: (Boolean) -> Unit
) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(true) }
    var isOffline by remember { mutableStateOf(false) }
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }

    val showShimmer = isLoading || isManualLoading.value
    val surfaceColor = MaterialTheme.colorScheme.surface

    Box(modifier = modifier.fillMaxSize().background(surfaceColor)) {
        
        // Background "Code/Logic" animation shown during loading states
        if (showShimmer && !isOffline) {
            LogicVoid(surfaceColor)
        }

        if (isOffline) {
            OfflineScreen {
                isOffline = false
                webViewInstance?.reload()
            }
        } else {
            // Integration of the standard Android View into Jetpack Compose
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    WebView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, 
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        // Use hardware acceleration for smoother scrolling
                        setLayerType(View.LAYER_TYPE_HARDWARE, null)
                        setBackgroundColor(0) // Transparent background to show our shimmer/LogicVoid

                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            setSupportMultipleWindows(true)
                            javaScriptCanOpenWindowsAutomatically = true
                            
                            // User Preference: 90% zoom for a cleaner, tighter mobile look
                            textZoom = 90 
                            
                            // Standard zoom support
                            setSupportZoom(true)
                            builtInZoomControls = true
                            displayZoomControls = false 

                            // Remove "wv" from User Agent to help with site compatibility
                            userAgentString = userAgentString.replace("wv", "")
                        }

                        webChromeClient = object : WebChromeClient() {
                            override fun onProgressChanged(v: WebView?, p: Int) { 
                                // Hide loading indicator once progress hits 100%
                                if (p == 100) isLoading = false 
                            }
                        }

                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(v: WebView?, u: String?, f: Bitmap?) {
                                isManualLoading.value = false
                                isLoading = true
                                isOffline = false
                            }

                            override fun onPageFinished(v: WebView?, u: String?) {
                                isLoading = false
                                // Notify MainActivity if the back button should be enabled
                                onStateChanged(v?.canGoBack() ?: false)
                            }

                            override fun onReceivedError(v: WebView?, r: WebResourceRequest?, e: WebResourceError?) {
                                // Only trigger offline screen for the main page load failure
                                if (r?.isForMainFrame == true) { 
                                    isOffline = true
                                    isLoading = false 
                                }
                            }

                            override fun shouldOverrideUrlLoading(v: WebView?, r: WebResourceRequest?): Boolean {
                                // Decide whether to load URL in-app or open external browser
                                return handleUrl(context, r?.url ?: return false) { isLoading = it }
                            }
                        }
                        
                        loadUrl(url)
                        onCreated(this) // Pass instance back to MainActivity
                        webViewInstance = this
                        // Register for long-press context menu defined in MainActivity
                        (ctx as? Activity)?.registerForContextMenu(this)
                    }
                }
            )
        }

        /**
         * Top Progress Bar/Shimmer:
         * Animated gradient bar that appears at the top during page loads.
         */
        val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
        val offset by infiniteTransition.animateFloat(
            initialValue = -0.5f, targetValue = 1.5f,
            animationSpec = infiniteRepeatable(tween(1200, easing = LinearEasing), RepeatMode.Restart),
            label = "offset"
        )
        val shimmerAlpha by infiniteTransition.animateFloat(
            initialValue = 0.6f, targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(800, easing = LinearEasing), RepeatMode.Reverse),
            label = "alpha"
        )

        AnimatedVisibility(
            visible = showShimmer && !isOffline,
            modifier = Modifier.align(Alignment.TopCenter),
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 2.dp)) {
                Surface(
                    modifier = Modifier.fillMaxWidth().height(6.dp),
                    shape = CircleShape, 
                    color = Color.Gray.copy(alpha = 0.1f), 
                    shadowElevation = 4.dp
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawRoundRect(
                            brush = Brush.linearGradient(listOf(Color(0xFF4FACFE), Color(0xFF00F2FE))),
                            topLeft = Offset(x = size.width * offset, y = 0f),
                            size = Size(width = size.width * 0.4f, height = size.height),
                            cornerRadius = CornerRadius(12f, 12f),
                            alpha = shimmerAlpha
                        )
                    }
                }
            }
        }
    }
}

/**
 * LogicVoid: A custom Canvas animation that draws floating programming symbols.
 * Provides a "technical" aesthetic during the initial site load.
 */
@Composable
fun LogicVoid(surfaceColor: Color) {
    val textMeasurer = rememberTextMeasurer()
    val infiniteTransition = rememberInfiniteTransition(label = "logicpulse")

    val alphaPulse by infiniteTransition.animateFloat(
        initialValue = 0.25f, targetValue = 0.45f,
        animationSpec = infiniteRepeatable(tween(1500, easing = LinearEasing), RepeatMode.Reverse),
        label = "alphaPulse"
    )

    val verticalDrift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(12000, easing = LinearEasing), RepeatMode.Restart),
        label = "verticalDrift"
    )

    // Generate a static list of symbols with random positions
    val symbols = remember {
        val list = listOf("+", "-", "*", "/", "{", "}", "[", "]", "1", "0", "<", ">", "&", "|", "^")
        List(35) { 
            Symbol(
                text = list.random(),
                initialOffset = Offset(Random.nextFloat(), Random.nextFloat()),
                fontSize = Random.nextInt(35, 65).toFloat(),
                initialRotation = Random.nextInt(-30, 30).toFloat()
            )
        }
    }

    val textStyle = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF4FACFE)
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        symbols.forEach { symbol ->
            val measuredText = textMeasurer.measure(
                text = symbol.text,
                style = textStyle.copy(fontSize = symbol.fontSize.sp)
            )
            
            // Loop vertical movement
            val currentY = (symbol.initialOffset.y - verticalDrift + 1f) % 1f
            val actualOffset = Offset(
                x = symbol.initialOffset.x * size.width,
                y = currentY * size.height
            )

            rotate(degrees = symbol.initialRotation, pivot = actualOffset) {
                drawText(
                    textLayoutResult = measuredText,
                    topLeft = actualOffset,
                    alpha = alphaPulse
                )
            }
        }
        // Faint overlay to blend the symbols into the surface background
        drawRect(color = surfaceColor.copy(alpha = 0.3f))
    }
}

/**
 * handleUrl: Manages link clicking logic.
 * - Opens external sites in Chrome Custom Tabs.
 * - Opens system protocols (tel:, mailto:) via external intents.
 * - Keeps BananaKnowledge97 internal links inside the app.
 */
private fun handleUrl(context: Context, uri: Uri, onLoading: (Boolean) -> Unit): Boolean {
    val urlString = uri.toString().lowercase()
    
    // Handle non-HTTP links (Intents, system apps)
    if (!urlString.startsWith("http")) {
        try { 
            context.startActivity(Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            return true 
        } catch (e: Exception) { return true }
    }
    
    // Open external sites in a Custom Tab instead of leaving the app
    if (!urlString.contains("bananaknowledge97.in")) {
        CustomTabsIntent.Builder().build().launchUrl(context, uri)
        return true
    }
    
    // Internal link: Let the WebView handle it
    onLoading(true)
    return false 
}

/**
 * Simple UI shown when a network error occurs.
 */
@Composable
fun OfflineScreen(onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("No Internet Connection", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(24.dp))
        Button(onClick = onRetry, shape = CircleShape) {
            Icon(Icons.Default.Refresh, null)
            Spacer(Modifier.width(8.dp))
            Text("Retry")
        }
    }
}
