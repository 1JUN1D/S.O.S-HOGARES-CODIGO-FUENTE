package com.uv.sos.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat


private val lightScheme = lightColorScheme(
    primary = com.uv.mapa_usuario.ui.theme.primaryLight,
    onPrimary = com.uv.mapa_usuario.ui.theme.onPrimaryLight,
    primaryContainer = com.uv.mapa_usuario.ui.theme.primaryContainerLight,
    onPrimaryContainer = com.uv.mapa_usuario.ui.theme.onPrimaryContainerLight,
    secondary = com.uv.mapa_usuario.ui.theme.secondaryLight,
    onSecondary = com.uv.mapa_usuario.ui.theme.onSecondaryLight,
    secondaryContainer = com.uv.mapa_usuario.ui.theme.secondaryContainerLight,
    onSecondaryContainer = com.uv.mapa_usuario.ui.theme.onSecondaryContainerLight,
    tertiary = com.uv.mapa_usuario.ui.theme.tertiaryLight,
    onTertiary = com.uv.mapa_usuario.ui.theme.onTertiaryLight,
    tertiaryContainer = com.uv.mapa_usuario.ui.theme.tertiaryContainerLight,
    onTertiaryContainer = com.uv.mapa_usuario.ui.theme.onTertiaryContainerLight,
    error = com.uv.mapa_usuario.ui.theme.errorLight,
    onError = com.uv.mapa_usuario.ui.theme.onErrorLight,
    errorContainer = com.uv.mapa_usuario.ui.theme.errorContainerLight,
    onErrorContainer = com.uv.mapa_usuario.ui.theme.onErrorContainerLight,
    background = com.uv.mapa_usuario.ui.theme.backgroundLight,
    onBackground = com.uv.mapa_usuario.ui.theme.onBackgroundLight,
    surface = com.uv.mapa_usuario.ui.theme.surfaceLight,
    onSurface = com.uv.mapa_usuario.ui.theme.onSurfaceLight,
    surfaceVariant = com.uv.mapa_usuario.ui.theme.surfaceVariantLight,
    onSurfaceVariant = com.uv.mapa_usuario.ui.theme.onSurfaceVariantLight,
    outline = com.uv.mapa_usuario.ui.theme.outlineLight,
    outlineVariant = com.uv.mapa_usuario.ui.theme.outlineVariantLight,
    scrim = com.uv.mapa_usuario.ui.theme.scrimLight,
    inverseSurface = com.uv.mapa_usuario.ui.theme.inverseSurfaceLight,
    inverseOnSurface = com.uv.mapa_usuario.ui.theme.inverseOnSurfaceLight,
    inversePrimary = com.uv.mapa_usuario.ui.theme.inversePrimaryLight,

    )

private val darkScheme = darkColorScheme(
    primary = com.uv.mapa_usuario.ui.theme.primaryDark,
    onPrimary = com.uv.mapa_usuario.ui.theme.onPrimaryDark,
    primaryContainer = com.uv.mapa_usuario.ui.theme.primaryContainerDark,
    onPrimaryContainer = com.uv.mapa_usuario.ui.theme.onPrimaryContainerDark,
    secondary = com.uv.mapa_usuario.ui.theme.secondaryDark,
    onSecondary = com.uv.mapa_usuario.ui.theme.onSecondaryDark,
    secondaryContainer = com.uv.mapa_usuario.ui.theme.secondaryContainerDark,
    onSecondaryContainer = com.uv.mapa_usuario.ui.theme.onSecondaryContainerDark,
    tertiary = com.uv.mapa_usuario.ui.theme.tertiaryDark,
    onTertiary = com.uv.mapa_usuario.ui.theme.onTertiaryDark,
    tertiaryContainer = com.uv.mapa_usuario.ui.theme.tertiaryContainerDark,
    onTertiaryContainer = com.uv.mapa_usuario.ui.theme.onTertiaryContainerDark,
    error = com.uv.mapa_usuario.ui.theme.errorDark,
    onError = com.uv.mapa_usuario.ui.theme.onErrorDark,
    errorContainer = com.uv.mapa_usuario.ui.theme.errorContainerDark,
    onErrorContainer = com.uv.mapa_usuario.ui.theme.onErrorContainerDark,
    background = com.uv.mapa_usuario.ui.theme.backgroundDark,
    onBackground = com.uv.mapa_usuario.ui.theme.onBackgroundDark,
    surface = com.uv.mapa_usuario.ui.theme.surfaceDark,
    onSurface = com.uv.mapa_usuario.ui.theme.onSurfaceDark,
    surfaceVariant = com.uv.mapa_usuario.ui.theme.surfaceVariantDark,
    onSurfaceVariant = com.uv.mapa_usuario.ui.theme.onSurfaceVariantDark,
    outline = com.uv.mapa_usuario.ui.theme.outlineDark,
    outlineVariant = com.uv.mapa_usuario.ui.theme.outlineVariantDark,
    scrim = com.uv.mapa_usuario.ui.theme.scrimDark,
    inverseSurface = com.uv.mapa_usuario.ui.theme.inverseSurfaceDark,
    inverseOnSurface = com.uv.mapa_usuario.ui.theme.inverseOnSurfaceDark,
    inversePrimary = com.uv.mapa_usuario.ui.theme.inversePrimaryDark,

    )

private val mediumContrastLightColorScheme = lightColorScheme(
    primary = com.uv.mapa_usuario.ui.theme.primaryLightMediumContrast,
    onPrimary = com.uv.mapa_usuario.ui.theme.onPrimaryLightMediumContrast,
    primaryContainer = com.uv.mapa_usuario.ui.theme.primaryContainerLightMediumContrast,
    onPrimaryContainer = com.uv.mapa_usuario.ui.theme.onPrimaryContainerLightMediumContrast,
    secondary = com.uv.mapa_usuario.ui.theme.secondaryLightMediumContrast,
    onSecondary = com.uv.mapa_usuario.ui.theme.onSecondaryLightMediumContrast,
    secondaryContainer = com.uv.mapa_usuario.ui.theme.secondaryContainerLightMediumContrast,
    onSecondaryContainer = com.uv.mapa_usuario.ui.theme.onSecondaryContainerLightMediumContrast,
    tertiary = com.uv.mapa_usuario.ui.theme.tertiaryLightMediumContrast,
    onTertiary = com.uv.mapa_usuario.ui.theme.onTertiaryLightMediumContrast,
    tertiaryContainer = com.uv.mapa_usuario.ui.theme.tertiaryContainerLightMediumContrast,
    onTertiaryContainer = com.uv.mapa_usuario.ui.theme.onTertiaryContainerLightMediumContrast,
    error = com.uv.mapa_usuario.ui.theme.errorLightMediumContrast,
    onError = com.uv.mapa_usuario.ui.theme.onErrorLightMediumContrast,
    errorContainer = com.uv.mapa_usuario.ui.theme.errorContainerLightMediumContrast,
    onErrorContainer = com.uv.mapa_usuario.ui.theme.onErrorContainerLightMediumContrast,
    background = com.uv.mapa_usuario.ui.theme.backgroundLightMediumContrast,
    onBackground = com.uv.mapa_usuario.ui.theme.onBackgroundLightMediumContrast,
    surface = com.uv.mapa_usuario.ui.theme.surfaceLightMediumContrast,
    onSurface = com.uv.mapa_usuario.ui.theme.onSurfaceLightMediumContrast,
    surfaceVariant = com.uv.mapa_usuario.ui.theme.surfaceVariantLightMediumContrast,
    onSurfaceVariant = com.uv.mapa_usuario.ui.theme.onSurfaceVariantLightMediumContrast,
    outline = com.uv.mapa_usuario.ui.theme.outlineLightMediumContrast,
    outlineVariant = com.uv.mapa_usuario.ui.theme.outlineVariantLightMediumContrast,
    scrim = com.uv.mapa_usuario.ui.theme.scrimLightMediumContrast,
    inverseSurface = com.uv.mapa_usuario.ui.theme.inverseSurfaceLightMediumContrast,
    inverseOnSurface = com.uv.mapa_usuario.ui.theme.inverseOnSurfaceLightMediumContrast,
    inversePrimary = com.uv.mapa_usuario.ui.theme.inversePrimaryLightMediumContrast,

    )

private val highContrastLightColorScheme = lightColorScheme(
    primary = com.uv.mapa_usuario.ui.theme.primaryLightHighContrast,
    onPrimary = com.uv.mapa_usuario.ui.theme.onPrimaryLightHighContrast,
    primaryContainer = com.uv.mapa_usuario.ui.theme.primaryContainerLightHighContrast,
    onPrimaryContainer = com.uv.mapa_usuario.ui.theme.onPrimaryContainerLightHighContrast,
    secondary = com.uv.mapa_usuario.ui.theme.secondaryLightHighContrast,
    onSecondary = com.uv.mapa_usuario.ui.theme.onSecondaryLightHighContrast,
    secondaryContainer = com.uv.mapa_usuario.ui.theme.secondaryContainerLightHighContrast,
    onSecondaryContainer = com.uv.mapa_usuario.ui.theme.onSecondaryContainerLightHighContrast,
    tertiary = com.uv.mapa_usuario.ui.theme.tertiaryLightHighContrast,
    onTertiary = com.uv.mapa_usuario.ui.theme.onTertiaryLightHighContrast,
    tertiaryContainer = com.uv.mapa_usuario.ui.theme.tertiaryContainerLightHighContrast,
    onTertiaryContainer = com.uv.mapa_usuario.ui.theme.onTertiaryContainerLightHighContrast,
    error = com.uv.mapa_usuario.ui.theme.errorLightHighContrast,
    onError = com.uv.mapa_usuario.ui.theme.onErrorLightHighContrast,
    errorContainer = com.uv.mapa_usuario.ui.theme.errorContainerLightHighContrast,
    onErrorContainer = com.uv.mapa_usuario.ui.theme.onErrorContainerLightHighContrast,
    background = com.uv.mapa_usuario.ui.theme.backgroundLightHighContrast,
    onBackground = com.uv.mapa_usuario.ui.theme.onBackgroundLightHighContrast,
    surface = com.uv.mapa_usuario.ui.theme.surfaceLightHighContrast,
    onSurface = com.uv.mapa_usuario.ui.theme.onSurfaceLightHighContrast,
    surfaceVariant = com.uv.mapa_usuario.ui.theme.surfaceVariantLightHighContrast,
    onSurfaceVariant = com.uv.mapa_usuario.ui.theme.onSurfaceVariantLightHighContrast,
    outline = com.uv.mapa_usuario.ui.theme.outlineLightHighContrast,
    outlineVariant = com.uv.mapa_usuario.ui.theme.outlineVariantLightHighContrast,
    scrim = com.uv.mapa_usuario.ui.theme.scrimLightHighContrast,
    inverseSurface = com.uv.mapa_usuario.ui.theme.inverseSurfaceLightHighContrast,
    inverseOnSurface = com.uv.mapa_usuario.ui.theme.inverseOnSurfaceLightHighContrast,
    inversePrimary = com.uv.mapa_usuario.ui.theme.inversePrimaryLightHighContrast,

    )

private val mediumContrastDarkColorScheme = darkColorScheme(
    primary = com.uv.mapa_usuario.ui.theme.primaryDarkMediumContrast,
    onPrimary = com.uv.mapa_usuario.ui.theme.onPrimaryDarkMediumContrast,
    primaryContainer = com.uv.mapa_usuario.ui.theme.primaryContainerDarkMediumContrast,
    onPrimaryContainer = com.uv.mapa_usuario.ui.theme.onPrimaryContainerDarkMediumContrast,
    secondary = com.uv.mapa_usuario.ui.theme.secondaryDarkMediumContrast,
    onSecondary = com.uv.mapa_usuario.ui.theme.onSecondaryDarkMediumContrast,
    secondaryContainer = com.uv.mapa_usuario.ui.theme.secondaryContainerDarkMediumContrast,
    onSecondaryContainer = com.uv.mapa_usuario.ui.theme.onSecondaryContainerDarkMediumContrast,
    tertiary = com.uv.mapa_usuario.ui.theme.tertiaryDarkMediumContrast,
    onTertiary = com.uv.mapa_usuario.ui.theme.onTertiaryDarkMediumContrast,
    tertiaryContainer = com.uv.mapa_usuario.ui.theme.tertiaryContainerDarkMediumContrast,
    onTertiaryContainer = com.uv.mapa_usuario.ui.theme.onTertiaryContainerDarkMediumContrast,
    error = com.uv.mapa_usuario.ui.theme.errorDarkMediumContrast,
    onError = com.uv.mapa_usuario.ui.theme.onErrorDarkMediumContrast,
    errorContainer = com.uv.mapa_usuario.ui.theme.errorContainerDarkMediumContrast,
    onErrorContainer = com.uv.mapa_usuario.ui.theme.onErrorContainerDarkMediumContrast,
    background = com.uv.mapa_usuario.ui.theme.backgroundDarkMediumContrast,
    onBackground = com.uv.mapa_usuario.ui.theme.onBackgroundDarkMediumContrast,
    surface = com.uv.mapa_usuario.ui.theme.surfaceDarkMediumContrast,
    onSurface = com.uv.mapa_usuario.ui.theme.onSurfaceDarkMediumContrast,
    surfaceVariant = com.uv.mapa_usuario.ui.theme.surfaceVariantDarkMediumContrast,
    onSurfaceVariant = com.uv.mapa_usuario.ui.theme.onSurfaceVariantDarkMediumContrast,
    outline = com.uv.mapa_usuario.ui.theme.outlineDarkMediumContrast,
    outlineVariant = com.uv.mapa_usuario.ui.theme.outlineVariantDarkMediumContrast,
    scrim = com.uv.mapa_usuario.ui.theme.scrimDarkMediumContrast,
    inverseSurface = com.uv.mapa_usuario.ui.theme.inverseSurfaceDarkMediumContrast,
    inverseOnSurface = com.uv.mapa_usuario.ui.theme.inverseOnSurfaceDarkMediumContrast,
    inversePrimary = com.uv.mapa_usuario.ui.theme.inversePrimaryDarkMediumContrast,

    )

private val highContrastDarkColorScheme = darkColorScheme(
    primary = com.uv.mapa_usuario.ui.theme.primaryDarkHighContrast,
    onPrimary = com.uv.mapa_usuario.ui.theme.onPrimaryDarkHighContrast,
    primaryContainer = com.uv.mapa_usuario.ui.theme.primaryContainerDarkHighContrast,
    onPrimaryContainer = com.uv.mapa_usuario.ui.theme.onPrimaryContainerDarkHighContrast,
    secondary = com.uv.mapa_usuario.ui.theme.secondaryDarkHighContrast,
    onSecondary = com.uv.mapa_usuario.ui.theme.onSecondaryDarkHighContrast,
    secondaryContainer = com.uv.mapa_usuario.ui.theme.secondaryContainerDarkHighContrast,
    onSecondaryContainer = com.uv.mapa_usuario.ui.theme.onSecondaryContainerDarkHighContrast,
    tertiary = com.uv.mapa_usuario.ui.theme.tertiaryDarkHighContrast,
    onTertiary = com.uv.mapa_usuario.ui.theme.onTertiaryDarkHighContrast,
    tertiaryContainer = com.uv.mapa_usuario.ui.theme.tertiaryContainerDarkHighContrast,
    onTertiaryContainer = com.uv.mapa_usuario.ui.theme.onTertiaryContainerDarkHighContrast,
    error = com.uv.mapa_usuario.ui.theme.errorDarkHighContrast,
    onError = com.uv.mapa_usuario.ui.theme.onErrorDarkHighContrast,
    errorContainer = com.uv.mapa_usuario.ui.theme.errorContainerDarkHighContrast,
    onErrorContainer = com.uv.mapa_usuario.ui.theme.onErrorContainerDarkHighContrast,
    background = com.uv.mapa_usuario.ui.theme.backgroundDarkHighContrast,
    onBackground = com.uv.mapa_usuario.ui.theme.onBackgroundDarkHighContrast,
    surface = com.uv.mapa_usuario.ui.theme.surfaceDarkHighContrast,
    onSurface = com.uv.mapa_usuario.ui.theme.onSurfaceDarkHighContrast,
    surfaceVariant = com.uv.mapa_usuario.ui.theme.surfaceVariantDarkHighContrast,
    onSurfaceVariant = com.uv.mapa_usuario.ui.theme.onSurfaceVariantDarkHighContrast,
    outline = com.uv.mapa_usuario.ui.theme.outlineDarkHighContrast,
    outlineVariant = com.uv.mapa_usuario.ui.theme.outlineVariantDarkHighContrast,
    scrim = com.uv.mapa_usuario.ui.theme.scrimDarkHighContrast,
    inverseSurface = com.uv.mapa_usuario.ui.theme.inverseSurfaceDarkHighContrast,
    inverseOnSurface = com.uv.mapa_usuario.ui.theme.inverseOnSurfaceDarkHighContrast,
    inversePrimary = com.uv.mapa_usuario.ui.theme.inversePrimaryDarkHighContrast,

    )

@Immutable
data class ColorFamily(
    val color: Color,
    val onColor: Color,
    val colorContainer: Color,
    val onColorContainer: Color
)

val unspecified_scheme = ColorFamily(
    Color.Unspecified, Color.Unspecified, Color.Unspecified, Color.Unspecified
)

@Composable
fun SOSTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable() () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> darkScheme
        else -> lightScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }


    MaterialTheme(
        colorScheme = colorScheme,
        typography = com.uv.mapa_usuario.ui.theme.Typography,
        content = content
    )
}