package cl.figonzal.lastquakechile.core.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Replica ShapeAppearance.LastQuakeChile.* del tema XML (todas 16dp rounded,
// excepto Button que usa 8dp → mapea a extraSmall/small en M3).
val Shapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),  // botones (corner_radius_8dp)
    small = RoundedCornerShape(16.dp),
    medium = RoundedCornerShape(16.dp),     // Card usa medium por defecto
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp)  // corner_radius_32dp (nombre engañoso, valor 24dp)
)
