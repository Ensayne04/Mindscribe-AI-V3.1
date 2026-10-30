package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = TealPrimaryDark,
    secondary = CyanSecondaryDark,
    tertiary = WarmAmberDark,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onBackground = OnBackgroundDark,
    onSurface = OnSurfaceDark,
    onPrimary = OnBackgroundLight,
    onSecondary = OnBackgroundLight,
    onTertiary = OnBackgroundLight
)

private val LightColorScheme = lightColorScheme(
    primary = TealPrimaryLight,
    secondary = CyanSecondaryLight,
    tertiary = WarmAmberLight,
    background = LightBackground,
    surface = LightSurface,
    surfaceVariant = LightSurfaceVariant,
    onBackground = OnBackgroundLight,
    onSurface = OnSurfaceLight,
    onPrimary = OnBackgroundDark,
    onSecondary = OnBackgroundDark,
    onTertiary = OnBackgroundDark
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Always fallback to our curated premium brand colors as the primary aesthetic,
    // but allow dynamic color if desired. Let's make it our curated design by default!
    dynamicColor: Boolean = false,
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
