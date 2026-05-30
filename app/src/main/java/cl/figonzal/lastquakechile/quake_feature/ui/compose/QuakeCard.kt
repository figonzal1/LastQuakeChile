package cl.figonzal.lastquakechile.quake_feature.ui.compose

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cl.figonzal.lastquakechile.core.ui.components.MagnitudeHeader
import cl.figonzal.lastquakechile.core.ui.theme.LastQuakeChileTheme
import cl.figonzal.lastquakechile.quake_feature.domain.model.Coordinate
import cl.figonzal.lastquakechile.quake_feature.domain.model.Quake
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun QuakeCard(
    quake: Quake,
    onClick: (Quake) -> Unit,
    modifier: Modifier = Modifier
) {
    // color transparente: deja ver el fondo elevado del MaterialCardView contenedor,
    // igual que el card_view_quake.xml original (transparente). Pintar surface plano
    // se vería más oscuro que el contenedor en dark mode (M2 elevation overlay).
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onSurface,
        onClick = { onClick(quake) }
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            MagnitudeHeader(quake = quake)

            // Divider at card level (not inside text column) so it doesn't inflate the column
            // height and break vertical alignment. start=82dp = 16(row padding)+50(circle)+16(spacer).
            HorizontalDivider(
                modifier = Modifier.padding(start = 82.dp, top = 16.dp)
            )
        }
    }
}

// ─── Previews ─────────────────────────────────────────────────────────────────

private fun sampleQuake(
    code: Int = 123,
    city: String = "La Serena",
    reference: String = "45km al OS de La Serena",
    magnitude: Double = 3.6,
    isSensitive: Boolean = false,
    isVerified: Boolean = true
) = Quake(
    quakeCode = code,
    localDate = LocalDateTime.now().minusMinutes(12)
        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
    city = city,
    reference = reference,
    magnitude = magnitude,
    depth = 34.8,
    scale = "ml",
    isSensitive = isSensitive,
    isVerified = isVerified,
    coordinate = Coordinate(-30.06, -71.31)
)

@Preview(showBackground = true, name = "QuakeCard — Verified")
@Composable
private fun QuakeCardVerifiedPreview() {
    LastQuakeChileTheme {
        QuakeCard(quake = sampleQuake(), onClick = {})
    }
}

@Preview(showBackground = true, name = "QuakeCard — Sensitive")
@Composable
private fun QuakeCardSensitivePreview() {
    LastQuakeChileTheme {
        QuakeCard(
            quake = sampleQuake(
                city = "Concepción",
                reference = "14km al OS de Concepción",
                magnitude = 6.6,
                isSensitive = true,
                isVerified = true
            ),
            onClick = {}
        )
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "QuakeCard — Dark"
)
@Composable
private fun QuakeCardDarkPreview() {
    LastQuakeChileTheme(darkTheme = true) {
        QuakeCard(
            quake = sampleQuake(
                city = "Santiago",
                reference = "14km al OS de Santiago",
                magnitude = 7.2,
                isSensitive = true
            ),
            onClick = {}
        )
    }
}
