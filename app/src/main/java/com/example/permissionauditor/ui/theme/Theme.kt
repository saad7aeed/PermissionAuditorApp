package com.example.permissionauditor.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ✨ Light Theme Color Scheme
private val LightColorScheme = lightColorScheme(
    // Primary colors (used for main UI elements)
    primary = black,                           // Main brand color
    onPrimary = white,                         // Text/icons on primary
    primaryContainer = Color(0xFFE8E8FF),      // Lighter primary for containers
    onPrimaryContainer = black,                // Text on primary containers

    // Secondary colors (used for less prominent elements)
    secondary = blue,                          // Accent color
    onSecondary = white,                       // Text on secondary
    secondaryContainer = Color(0xFFE3F2FD),    // Light blue container
    onSecondaryContainer = Color(0xFF001D35),  // Dark text on secondary container

    // Tertiary colors (additional accent)
    tertiary = air_orange200,                  // Third accent color
    onTertiary = white,                        // Text on tertiary
    tertiaryContainer = Color(0xFFFFDDB3),     // Light orange container
    onTertiaryContainer = Color(0xFF2A1800),   // Dark text on tertiary

    // Error colors
    error = air_red,                           // Error/danger color
    onError = white,                           // Text on error
    errorContainer = Color(0xFFFFDAD6),        // Light error container
    onErrorContainer = Color(0xFF410002),      // Dark text on error

    // Background colors
    background = white,                        // Main background (#FFFFFF)
    onBackground = Color(0xFF1C1B1F),          // Text on background

    // Surface colors (for cards, sheets, dialogs)
    surface = card_background_white,           // Card background (#FDFDFD)
    onSurface = Color(0xFF1C1B1F),             // Text on surface
    surfaceVariant = tags_background_white,    // Alternative surface (#EDEDED)
    onSurfaceVariant = Color(0xFF49454F),      // Text on surface variant

    // Outline colors
    outline = outline_color,                   // Border color (#ECECEC)
    outlineVariant = Color(0xFFCAC4D0),        // Alternative outline

    // Container colors
    surfaceContainer = Color(0xFFF3F2F7),      // Container background
    surfaceContainerHigh = Color(0xFFECEBF0),  // High emphasis container
    surfaceContainerHighest = Color(0xFFE6E1E6), // Highest emphasis

    // Inverse colors (for dark elements in light theme)
    inverseSurface = Color(0xFF313033),        // Dark surface in light theme
    inverseOnSurface = Color(0xFFF4EFF4),      // Light text on dark surface
    inversePrimary = Color(0xFFB8C3FF),        // Inverse primary

    // Scrim (for overlays)
    scrim = Color.Black.copy(alpha = 0.5f)     // Overlay color
)

// ✨ Dark Theme Color Scheme
private val DarkColorScheme = darkColorScheme(
    // Primary colors
    primary = custom_white,                    // Main brand color in dark mode
    onPrimary = black,                         // Text on primary
    primaryContainer = Color(0xFF3C3E52),      // Dark primary container
    onPrimaryContainer = Color(0xFFE0E0FF),    // Light text on container

    // Secondary colors
    secondary = blue,                          // Keep accent blue
    onSecondary = white,                       // Text on secondary
    secondaryContainer = Color(0xFF004881),    // Dark blue container
    onSecondaryContainer = Color(0xFFD1E4FF),  // Light text

    // Tertiary colors
    tertiary = air_orange200,                  // Orange accent
    onTertiary = black,                        // Text on tertiary
    tertiaryContainer = Color(0xFF5A3F00),     // Dark orange container
    onTertiaryContainer = Color(0xFFFFDDB3),   // Light text

    // Error colors
    error = Color(0xFFFFB4AB),                 // Lighter error for dark mode
    onError = Color(0xFF690005),               // Dark text on error
    errorContainer = Color(0xFF93000A),        // Dark error container
    onErrorContainer = Color(0xFFFFDAD6),      // Light text

    // Background colors
    background = black,                        // Dark background (#111111)
    onBackground = Color(0xFFE6E1E6),          // Light text on background

    // Surface colors
    surface = card_background_dark,            // Card background (#1E1E1E)
    onSurface = Color(0xFFE6E1E6),             // Light text on surface
    surfaceVariant = tags_background_dark,     // Alternative surface (#2A2A2A)
    onSurfaceVariant = Color(0xFFCAC4D0),      // Text on variant

    // Outline colors
    outline = Color(0xFF938F99),               // Border in dark mode
    outlineVariant = Color(0xFF49454F),        // Alternative outline

    // Container colors
    surfaceContainer = Color(0xFF1F1F1F),      // Container background
    surfaceContainerHigh = Color(0xFF2A2A2A),  // High emphasis
    surfaceContainerHighest = Color(0xFF353535), // Highest emphasis

    // Inverse colors
    inverseSurface = Color(0xFFE6E1E6),        // Light surface in dark theme
    inverseOnSurface = Color(0xFF313033),      // Dark text on light
    inversePrimary = Color(0xFF4B558F),        // Inverse primary

    // Scrim
    scrim = Color.Black.copy(alpha = 0.7f)     // Darker overlay for dark mode
)

@Composable
fun PermissionAuditorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // ✅ Disabled by default to use custom colors
    content: @Composable () -> Unit
) {
    val context = LocalContext.current

    // ✨ Determine color scheme
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            // Dynamic colors from wallpaper (Android 12+)
            if (darkTheme) {
                dynamicDarkColorScheme(context)
            } else {
                dynamicLightColorScheme(context)
            }
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // ✨ Set status bar color
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
