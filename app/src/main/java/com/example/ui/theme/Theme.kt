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

val BasoDarkColorScheme = darkColorScheme(
  primary = BasoCyan,
  onPrimary = BasoDarkBackground,
  primaryContainer = BasoDarkSurfaceHigh,
  onPrimaryContainer = BasoCyanLight,
  secondary = BasoBlue,
  onSecondary = BasoDarkBackground,
  secondaryContainer = BasoDarkSurfaceVariant,
  onSecondaryContainer = BasoTextPrimary,
  tertiary = BasoPurple,
  background = BasoDarkBackground,
  onBackground = BasoTextPrimary,
  surface = BasoDarkSurface,
  onSurface = BasoTextPrimary,
  surfaceVariant = BasoDarkSurfaceVariant,
  onSurfaceVariant = BasoTextSecondary,
  outline = BasoDarkBorder
)

val BasoLightColorScheme = lightColorScheme(
  primary = BasoBlue,
  onPrimary = BasoLightBackground,
  primaryContainer = BasoLightSurfaceVariant,
  onPrimaryContainer = BasoBlueDark,
  secondary = BasoCyanDark,
  tertiary = BasoPurple,
  background = BasoLightBackground,
  onBackground = BasoLightTextPrimary,
  surface = BasoLightSurface,
  onSurface = BasoLightTextPrimary,
  surfaceVariant = BasoLightSurfaceVariant,
  onSurfaceVariant = BasoLightTextSecondary,
  outline = BasoLightBorder
)

@Composable
fun BasoAiTheme(
  darkTheme: Boolean = true, // Default to dark theme as requested
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = when {
    dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
      val context = LocalContext.current
      if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    }
    darkTheme -> BasoDarkColorScheme
    else -> BasoLightColorScheme
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}

// Backward compatibility alias for tests
@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) = BasoAiTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
