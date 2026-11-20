package com.nimelssa.compose

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val NimelssaLightColorScheme = lightColorScheme(
    brightness = androidx.compose.ui.graphics.Brightness.Light,
    primary = Color(0xFF1976D2),
    onPrimary = Color.WHITE,
    secondary = Color(0xFF673AB7),
    onSecondary = Color.WHITE,
    surface = Color(0xFFF5F5F5),
    onSurface = Color(0xFF212121),
    error = Color(0xFFCF6679),
    onError = Color.WHITE,
    container = Color(0xFFFAFAFA),
    onContainer = Color(0xFF212121),
)

val NimelssaDarkColorScheme = darkColorScheme(
    brightness = androidx.compose.ui.graphics.Brightness.Dark,
    primary = Color(0xFF90CAF9),
    onPrimary = Color(0xFF0D47A1),
    secondary = Color(0xFFBA68C8),
    onSecondary = Color(0xFF4A148C),
    surface = Color(0xFF424242),
    onSurface = Color(0xFFE0E0E0),
    error = Color(0xFFFFCDD2),
    onError = Color(0xFF841F2D),
    container = Color(0xFF333333),
    onContainer = Color(0xFFE0E0E0),
)

@Composable
fun NimelssaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) NimelssaDarkColorScheme else NimelssaLightColorScheme,
        typography = MaterialTheme.typography,
        content = content
    )
}