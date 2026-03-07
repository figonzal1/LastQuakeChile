package cl.figonzal.lastquakechile.core.ui.composables.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape

private val LightColors = lightColorScheme(
    primary = Color(0xFF3F51B5),
    onPrimary = Color(0xFFFFFFFF),
    secondary = Color(0xFF00BBFF),
    onSecondary = Color(0x9C000000),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF000000),
    // MDC2 no tiene surfaceVariant — mapear igual que surface para que Card use colorSurface
    surfaceVariant = Color(0xFFFFFFFF),
    onSurfaceVariant = Color(0xFF000000),
    background = Color(0xFFEFEFEF),
    onBackground = Color(0xFF000000)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF253561),
    onPrimary = Color(0xCCFFFFFF),
    secondary = Color(0xFF006994),
    onSecondary = Color(0xFFFFFFFF),
    surface = Color(0xFF323236),
    onSurface = Color(0x8CFFFFFF),
    // MDC2 no tiene surfaceVariant — mapear igual que surface para que Card use colorSurface
    surfaceVariant = Color(0xFF323236),
    onSurfaceVariant = Color(0x8CFFFFFF),
    background = Color(0xFF28282C),
    onBackground = Color(0xFFFFFFFF)
)

// shapeAppearanceMediumComponent (cards) = 16dp, shapeAppearanceSmallComponent (chips, FABs) = 16dp
// shapeAppearanceLargeComponent = 16dp  (see shape.xml)
private val AppShapes = Shapes(
    small = RoundedCornerShape(16.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(16.dp)
)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        shapes = AppShapes,
        content = content
    )
}
