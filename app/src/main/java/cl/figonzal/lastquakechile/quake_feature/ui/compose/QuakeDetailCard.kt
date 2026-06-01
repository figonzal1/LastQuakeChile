package cl.figonzal.lastquakechile.quake_feature.ui.compose

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cl.figonzal.lastquakechile.R
import cl.figonzal.lastquakechile.core.ui.components.MagnitudeHeader
import cl.figonzal.lastquakechile.core.ui.theme.LastQuakeChileTheme
import cl.figonzal.lastquakechile.core.utils.views.coordinateToDMS
import cl.figonzal.lastquakechile.core.utils.views.quakeScaleText
import cl.figonzal.lastquakechile.quake_feature.domain.model.Coordinate
import cl.figonzal.lastquakechile.quake_feature.domain.model.Quake
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun QuakeDetailCard(quake: Quake, modifier: Modifier = Modifier) {
    val context = LocalContext.current

    // card_view_quake_detail.xml root was MaterialCardView — replicate elevation and surface color.
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            MagnitudeHeader(quake = quake)

            HorizontalDivider(modifier = Modifier.padding(top = 16.dp))

            // Two-column grid: left (datetime + coords), right (depth + scale)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 16.dp)
            ) {
                // Left column: date/hour + position DMS
                Column(modifier = Modifier.weight(1f)) {
                    DetailCell(
                        iconRes = R.drawable.round_date_range_24,
                        iconCd = stringResource(R.string.cd_datetime_icon),
                        title = stringResource(R.string.date_time_title),
                        value = quake.localDate
                    )
                    Spacer(Modifier.height(8.dp))
                    DetailCell(
                        iconRes = R.drawable.round_near_me_24,
                        iconCd = stringResource(R.string.gms_icon),
                        title = stringResource(R.string.gms_title),
                        value = coordinateToDMS(context, quake.coordinate)
                    )
                }

                // Right column: depth + scale
                Column(modifier = Modifier.weight(1f)) {
                    DetailCell(
                        iconRes = R.drawable.round_height_24,
                        iconCd = stringResource(R.string.cd_depth_icon),
                        title = stringResource(R.string.depth_title),
                        value = String.format(Locale.getDefault(), "%.1f Km", quake.depth)
                    )
                    Spacer(Modifier.height(8.dp))
                    DetailCell(
                        iconRes = R.drawable.round_ruler_24,
                        iconCd = stringResource(R.string.cd_scale_icon),
                        title = stringResource(R.string.scale_title),
                        value = quakeScaleText(context, quake.scale)
                    )
                }
            }
        } // Column
    } // Card
}

@Composable
private fun DetailCell(
    iconRes: Int,
    iconCd: String,
    title: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.padding(start = 8.dp, top = 8.dp, end = 8.dp)) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = iconCd,
            modifier = Modifier
                .size(40.dp)
                .padding(end = 8.dp),
            tint = MaterialTheme.colorScheme.onSurface
        )
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ─── Previews ─────────────────────────────────────────────────────────────────

private fun sampleQuake() = Quake(
    quakeCode = 1,
    localDate = LocalDateTime.now().minusMinutes(45)
        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
    city = "La Serena",
    reference = "45 km al OS de La Serena",
    magnitude = 4.2,
    depth = 34.8,
    scale = "Ml",
    isSensitive = false,
    isVerified = true,
    coordinate = Coordinate(-30.06, -71.31)
)

@Preview(showBackground = true, name = "QuakeDetailCard — Light")
@Composable
private fun QuakeDetailCardLightPreview() {
    LastQuakeChileTheme {
        QuakeDetailCard(quake = sampleQuake())
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "QuakeDetailCard — Dark"
)
@Composable
private fun QuakeDetailCardDarkPreview() {
    LastQuakeChileTheme(darkTheme = true) {
        QuakeDetailCard(quake = sampleQuake().copy(isSensitive = true, isVerified = false))
    }
}
