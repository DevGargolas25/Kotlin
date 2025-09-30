package com.example.brigadist.ui.theme

import androidx.compose.material3.MaterialTheme
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

@Composable
fun BrigadistTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = BrigadistLightColors,
        content = content
    )
}
