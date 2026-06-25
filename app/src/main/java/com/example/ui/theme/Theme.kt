package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val BlackGoldColorScheme = darkColorScheme(
  primary = GoldPrimary,
  secondary = GoldSecondary,
  tertiary = GoldTertiary,
  background = BlackBackground,
  surface = SlateSurface,
  surfaceVariant = SlateSurfaceVariant,
  onPrimary = BlackBackground,
  onSecondary = BlackBackground,
  onTertiary = BlackBackground,
  onBackground = TextPrimary,
  onSurface = TextPrimary,
  onSurfaceVariant = TextSecondary,
  error = RedError,
  onError = BlackBackground
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false, // We force Black and Gold branding
  content: @Composable () -> Unit,
) {
  val colorScheme = BlackGoldColorScheme

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
