package com.aistudio.mobileagent.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Material Black Color Scheme - Mandatory & Default
val MaterialBlackColorScheme = darkColorScheme(
    primary = ElegantBlue,
    onPrimary = PureBlack,
    primaryContainer = DarkCardBlack,
    onPrimaryContainer = ElegantBlue,
    secondary = ElegantPurple,
    onSecondary = PureBlack,
    secondaryContainer = DarkElevatedCard,
    onSecondaryContainer = TextWhite,
    tertiary = ElegantCyan,
    onTertiary = PureBlack,
    background = PureBlack,
    onBackground = TextWhite,
    surface = DarkSurfaceBlack,
    onSurface = TextWhite,
    surfaceVariant = DarkCardBlack,
    onSurfaceVariant = TextLightGray,
    outline = DarkBorderBlack,
    outlineVariant = DarkDivider,
    error = RoseError,
    onError = PureBlack
)

@Composable
fun MobileAgentTheme(
    darkTheme: Boolean = true, // Dark mode is default and mandatory
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = MaterialBlackColorScheme,
        content = content
    )
}
