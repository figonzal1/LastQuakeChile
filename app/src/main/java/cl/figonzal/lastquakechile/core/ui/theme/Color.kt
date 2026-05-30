package cl.figonzal.lastquakechile.core.ui.theme

import androidx.compose.ui.graphics.Color

// Light palette
val PrimaryBlue = Color(0xFF3F51B5)
val PrimaryBlueDark = Color(0xFF303F9F)
val OnPrimaryWhite = Color(0xFFFFFFFF)
val SecondaryBlue = Color(0xFF00BBFF)
val OnSecondaryDark = Color(0x9C000000)
val SurfaceLight = Color(0xFFFFFFFF)
val OnSurfaceLight = Color(0xFF000000)
val BackgroundLight = Color(0xFFEFEFEF)

// Dark palette
val PrimaryBlueDarkTheme = Color(0xFF253561)
val PrimaryBlueDarker = Color(0xFF1C2746)
val OnPrimaryDarkTheme = Color(0xCCFFFFFF)
val SecondaryBlueDarkTheme = Color(0xFF006994)
val OnSecondaryDarkTheme = Color(0xFFFFFFFF)
val SurfaceDark = Color(0xFF323236)
// M2 textColorPrimary alto énfasis = 87% blanco (0xDE). El 0x8C original era
// colorOnSurface de M2 para elementos secundarios, no para texto primario.
val OnSurfaceDark = Color(0xDEFFFFFF)
val BackgroundDark = Color(0xFF28282C)

// Magnitude scale (reutilizada en composables igual que getMagnitudeColor)
val Magnitude1 = Color(0xFF006400)
val Magnitude2 = Color(0xFF2A7900)
val Magnitude3 = Color(0xFF6C9800)
val Magnitude4 = Color(0xFFC6C000)
val Magnitude5 = Color(0xFFFFB000)
val Magnitude6 = Color(0xFFF56B0B)
val Magnitude7 = Color(0xFFD83618)
val Magnitude8 = Color(0xFFB22222)

// Magnitude with alpha (for map circles)
val Magnitude1Alpha = Color(0x80006400)
val Magnitude2Alpha = Color(0x802A7900)
val Magnitude3Alpha = Color(0x806C9800)
val Magnitude4Alpha = Color(0x80C6C000)
val Magnitude5Alpha = Color(0x80FFB000)
val Magnitude6Alpha = Color(0x80F56B0B)
val Magnitude7Alpha = Color(0x80D83618)
val Magnitude8Alpha = Color(0x80B22222)
