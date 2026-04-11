package app.banana.bananaknowledge97.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// 1. DEFINE YOUR COLORS
val BananaYellow = Color(0xFFFFD700)
val DarkGrey = Color(0xFF1C1B1F)
val LightGrey = Color(0xFFFDFCFB)

// Dark Mode Palette
private val DarkColorScheme = darkColorScheme(
    primary = BananaYellow,
    secondary = Color(0xFFCCC2DC),
    tertiary = Color(0xFFEFB8C8),
    background = Color(0xFF121212),
    surface = Color(0xFF121212),
    onPrimary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White
)

// Light Mode Palette
private val LightColorScheme = lightColorScheme(
    primary = BananaYellow,
    secondary = Color(0xFF625B71),
    tertiary = Color(0xFF7D5260),
    background = LightGrey,
    surface = LightGrey,
    onPrimary = Color.Black,
    onBackground = DarkGrey,
    onSurface = DarkGrey
)

@Composable
fun BananaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+ (SDK 31+)
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // 2. OPTIMIZATION: Handle Status Bar & Navigation Bar Colors
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // We set the status bar to transparent so enableEdgeToEdge() works perfectly
            
            
            // This ensures icons (clock/battery) are visible against the background
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(), // Standard clean fonts
        content = content
    )
}
