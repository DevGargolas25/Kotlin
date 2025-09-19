package com.example.brigadist.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = DeepPurple,
    secondary = TurquoiseBlue,
    tertiary = MintGreen,
    background = SoftWhite,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = DeepPurple,
    onSurface = DeepPurple
)

private val DarkColors = darkColorScheme(
    primary = DeepPurple,
    secondary = TurquoiseBlue,
    tertiary = MintGreen,
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = SoftWhite,
    onSurface = SoftWhite
)

@Composable
fun BrigadistTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}
