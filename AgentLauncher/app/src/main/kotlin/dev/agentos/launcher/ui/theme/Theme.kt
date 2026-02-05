package dev.agentos.launcher.ui.theme

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

// Agent OS Brand Colors
private val AgentPrimary = Color(0xFF6750A4)
private val AgentOnPrimary = Color(0xFFFFFFFF)
private val AgentPrimaryContainer = Color(0xFFEADDFF)
private val AgentOnPrimaryContainer = Color(0xFF21005D)

private val AgentSecondary = Color(0xFF625B71)
private val AgentOnSecondary = Color(0xFFFFFFFF)
private val AgentSecondaryContainer = Color(0xFFE8DEF8)
private val AgentOnSecondaryContainer = Color(0xFF1D192B)

private val AgentTertiary = Color(0xFF7D5260)
private val AgentOnTertiary = Color(0xFFFFFFFF)
private val AgentTertiaryContainer = Color(0xFFFFD8E4)
private val AgentOnTertiaryContainer = Color(0xFF31111D)

private val AgentError = Color(0xFFB3261E)
private val AgentOnError = Color(0xFFFFFFFF)
private val AgentErrorContainer = Color(0xFFF9DEDC)
private val AgentOnErrorContainer = Color(0xFF410E0B)

private val AgentBackground = Color(0xFFFFFBFE)
private val AgentOnBackground = Color(0xFF1C1B1F)
private val AgentSurface = Color(0xFFFFFBFE)
private val AgentOnSurface = Color(0xFF1C1B1F)
private val AgentSurfaceVariant = Color(0xFFE7E0EC)
private val AgentOnSurfaceVariant = Color(0xFF49454F)
private val AgentOutline = Color(0xFF79747E)

// Dark Theme Colors
private val AgentPrimaryDark = Color(0xFFD0BCFF)
private val AgentOnPrimaryDark = Color(0xFF381E72)
private val AgentPrimaryContainerDark = Color(0xFF4F378B)
private val AgentOnPrimaryContainerDark = Color(0xFFEADDFF)

private val AgentSecondaryDark = Color(0xFFCCC2DC)
private val AgentOnSecondaryDark = Color(0xFF332D41)
private val AgentSecondaryContainerDark = Color(0xFF4A4458)
private val AgentOnSecondaryContainerDark = Color(0xFFE8DEF8)

private val AgentTertiaryDark = Color(0xFFEFB8C8)
private val AgentOnTertiaryDark = Color(0xFF492532)
private val AgentTertiaryContainerDark = Color(0xFF633B48)
private val AgentOnTertiaryContainerDark = Color(0xFFFFD8E4)

private val AgentErrorDark = Color(0xFFF2B8B5)
private val AgentOnErrorDark = Color(0xFF601410)
private val AgentErrorContainerDark = Color(0xFF8C1D18)
private val AgentOnErrorContainerDark = Color(0xFFF9DEDC)

private val AgentBackgroundDark = Color(0xFF1C1B1F)
private val AgentOnBackgroundDark = Color(0xFFE6E1E5)
private val AgentSurfaceDark = Color(0xFF1C1B1F)
private val AgentOnSurfaceDark = Color(0xFFE6E1E5)
private val AgentSurfaceVariantDark = Color(0xFF49454F)
private val AgentOnSurfaceVariantDark = Color(0xFFCAC4D0)
private val AgentOutlineDark = Color(0xFF938F99)

private val LightColorScheme = lightColorScheme(
    primary = AgentPrimary,
    onPrimary = AgentOnPrimary,
    primaryContainer = AgentPrimaryContainer,
    onPrimaryContainer = AgentOnPrimaryContainer,
    secondary = AgentSecondary,
    onSecondary = AgentOnSecondary,
    secondaryContainer = AgentSecondaryContainer,
    onSecondaryContainer = AgentOnSecondaryContainer,
    tertiary = AgentTertiary,
    onTertiary = AgentOnTertiary,
    tertiaryContainer = AgentTertiaryContainer,
    onTertiaryContainer = AgentOnTertiaryContainer,
    error = AgentError,
    onError = AgentOnError,
    errorContainer = AgentErrorContainer,
    onErrorContainer = AgentOnErrorContainer,
    background = AgentBackground,
    onBackground = AgentOnBackground,
    surface = AgentSurface,
    onSurface = AgentOnSurface,
    surfaceVariant = AgentSurfaceVariant,
    onSurfaceVariant = AgentOnSurfaceVariant,
    outline = AgentOutline
)

private val DarkColorScheme = darkColorScheme(
    primary = AgentPrimaryDark,
    onPrimary = AgentOnPrimaryDark,
    primaryContainer = AgentPrimaryContainerDark,
    onPrimaryContainer = AgentOnPrimaryContainerDark,
    secondary = AgentSecondaryDark,
    onSecondary = AgentOnSecondaryDark,
    secondaryContainer = AgentSecondaryContainerDark,
    onSecondaryContainer = AgentOnSecondaryContainerDark,
    tertiary = AgentTertiaryDark,
    onTertiary = AgentOnTertiaryDark,
    tertiaryContainer = AgentTertiaryContainerDark,
    onTertiaryContainer = AgentOnTertiaryContainerDark,
    error = AgentErrorDark,
    onError = AgentOnErrorDark,
    errorContainer = AgentErrorContainerDark,
    onErrorContainer = AgentOnErrorContainerDark,
    background = AgentBackgroundDark,
    onBackground = AgentOnBackgroundDark,
    surface = AgentSurfaceDark,
    onSurface = AgentOnSurfaceDark,
    surfaceVariant = AgentSurfaceVariantDark,
    onSurfaceVariant = AgentOnSurfaceVariantDark,
    outline = AgentOutlineDark
)

@Composable
fun AgentLauncherTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
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

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
