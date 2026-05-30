package cl.figonzal.lastquakechile.quake_feature.ui.compose

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cl.figonzal.lastquakechile.R
import cl.figonzal.lastquakechile.core.ui.theme.LastQuakeChileTheme
import cl.figonzal.lastquakechile.core.utils.views.getMagnitudeColor
import cl.figonzal.lastquakechile.core.utils.views.quakeTimeText
import cl.figonzal.lastquakechile.core.utils.views.toast
import cl.figonzal.lastquakechile.quake_feature.domain.model.Coordinate
import cl.figonzal.lastquakechile.quake_feature.domain.model.Quake
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun QuakeCard(
    quake: Quake,
    onClick: (Quake) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val magnitudeColor = colorResource(getMagnitudeColor(quake.magnitude, false))
    val timeText = quakeTimeText(context, quake, isShortVersion = true)

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
        // Original XML: circle and text are top-aligned (not center-aligned).
        // tv_city is marginTop=4dp from circle top; tv_reference is marginBottom=4dp from
        // circle bottom. Using Alignment.Top here to replicate that.
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Magnitude circle with optional verified badge
            Box(modifier = Modifier.size(50.dp)) {
                // background_magnitude_circle_shape is a 100dp rectangle with corner_radius_32dp
                // (= 24dp). FIT_CENTER scales it 0.5x into the 50dp ImageView → 12dp effective
                // corners. RoundedCornerShape(12.dp) replicates that squircle, not a circle.
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp))
                        .background(magnitudeColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = String.format(Locale.getDefault(), "%.1f", quake.magnitude),
                        color = Color.White,
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
                // Verified badge: replicates background_verified_quake layer-list
                // (white oval behind round_verified_24). layer-list is not supported
                // by painterResource so we compose the two layers manually.
                if (quake.isVerified) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .align(Alignment.TopEnd)
                            .offset(x = 5.dp, y = (-5).dp)
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) { context.toast(R.string.quake_verified_toast) },
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                        )
                        Icon(
                            painter = painterResource(R.drawable.round_verified_24),
                            contentDescription = stringResource(R.string.cd_verified_quake_icon),
                            tint = Color.Unspecified,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // padding(top=4dp) mirrors tv_city marginTop=4dp from circle top.
            Column(modifier = Modifier.weight(1f).padding(top = 4.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = quake.city,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (quake.isSensitive) {
                        Icon(
                            painter = painterResource(R.drawable.round_warning_24),
                            contentDescription = stringResource(R.string.cd_sensitive_icon),
                            // preserve the drawable's own adaptive tint (#fe6b1a16 orange-red)
                            tint = Color.Unspecified,
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .size(16.dp)
                        )
                    }
                    Text(
                        text = timeText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = quake.reference,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
        }

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
