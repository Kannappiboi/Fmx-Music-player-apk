package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = AudiophileGold,
    onPrimary = VoidBlack,
    primaryContainer = SurfaceCard,
    onPrimaryContainer = AudiophileGoldGlow,
    secondary = ElectricCyan,
    onSecondary = VoidBlack,
    secondaryContainer = SurfaceElevated,
    onSecondaryContainer = ElectricCyan,
    tertiary = BitPerfectGreen,
    onTertiary = VoidBlack,
    background = VoidBlack,
    onBackground = TextPrimary,
    surface = DeepObsidian,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceDark,
    onSurfaceVariant = TextSecondary,
    outline = SurfaceBorder,
    outlineVariant = SurfaceDark
)

@Composable
fun ResonanceTheme(
    content: @Composable () -> Unit
) {
    // Dark mode by default for audiophile high-fidelity aesthetic
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}

// Backward-compatible alias
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    ResonanceTheme(content = content)
}
