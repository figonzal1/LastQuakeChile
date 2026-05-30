package cl.figonzal.lastquakechile.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = OnPrimaryWhite,
    primaryContainer = PrimaryBlueDark,
    secondary = SecondaryBlue,
    onSecondary = OnSecondaryDark,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    background = BackgroundLight,
    onBackground = OnSurfaceLight,
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryBlueDarkTheme,
    onPrimary = OnPrimaryDarkTheme,
    primaryContainer = PrimaryBlueDarker,
    secondary = SecondaryBlueDarkTheme,
    onSecondary = OnSecondaryDarkTheme,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    background = BackgroundDark,
    onBackground = OnSurfaceDark,
)

@Composable
fun LastQuakeChileTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
