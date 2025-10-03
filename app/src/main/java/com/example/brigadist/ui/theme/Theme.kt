package com.example.brigadist.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val BrigadistLightColors = lightColorScheme(
    primary = TealPrimary,
    onPrimary = Color.White,
    primaryContainer = TealContainer,
    onPrimaryContainer = DeepPurpleText,

    secondary = GreenSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE6F3EE),
    onSecondaryContainer = DeepPurpleText,

    tertiary = PeachTertiary,
    onTertiary = Color.White,

    background = Color.White,
    onBackground = DeepPurpleText,

    surface = Color.White,
    onSurface = DeepPurpleText,

    surfaceVariant = AquaSoftSurface,
    onSurfaceVariant = DeepPurpleText.copy(alpha = 0.6f),

    outline = OutlineAqua.copy(alpha = 0.5f),
    outlineVariant = OutlineAqua,
    
    error = ErrorRed,
    onError = Color.White
)

private val BrigadistDarkColors = darkColorScheme(
    primary = TealPrimaryDark,
    onPrimary = Color.Black,
    primaryContainer = TealContainerDark,
    onPrimaryContainer = DeepPurpleTextDark,

    secondary = GreenSecondaryDark,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF1A2D25),
    onSecondaryContainer = DeepPurpleTextDark,

    tertiary = PeachTertiaryDark,
    onTertiary = Color.Black,

    background = AquaSoftSurfaceDark,
    onBackground = DeepPurpleTextDark,

    surface = AquaSoftSurfaceDark,
    onSurface = DeepPurpleTextDark,

    surfaceVariant = Color(0xFF1A2A2B),
    onSurfaceVariant = DeepPurpleTextDark.copy(alpha = 0.7f),

    outline = OutlineAquaDark.copy(alpha = 0.5f),
    outlineVariant = OutlineAquaDark,
    
    error = ErrorRedDark,
    onError = Color.Black
)

@Composable
fun BrigadistTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) BrigadistDarkColors else BrigadistLightColors
    
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
