package cl.figonzal.lastquakechile.reports_feature.ui.compose

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cl.figonzal.lastquakechile.R
import cl.figonzal.lastquakechile.core.ui.theme.LastQuakeChileTheme
import cl.figonzal.lastquakechile.reports_feature.domain.model.CityQuakes
import cl.figonzal.lastquakechile.reports_feature.domain.model.Report

@Composable
fun ReportCard(report: Report, modifier: Modifier = Modifier) {
    val split = report.reportMonth.split("-")
    val year = split[0]
    val monthIndex = split.getOrNull(1)?.toIntOrNull() ?: 1
    val monthName = monthName(monthIndex)
    val title = "$monthName $year"

    Card(
        modifier = modifier.fillMaxWidth(),
        // Fuerza colorSurface para coincidir con MaterialCardView original (M2)
        // M3 Card usa surfaceContainerLow por defecto, que difiere del XML
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(R.drawable.round_sticky_note_24),
                    contentDescription = stringResource(R.string.cd_monthly_report_icon),
                    modifier = Modifier.size(40.dp),
                    // Color.Unspecified deja que el drawable use su propio android:tint
                    // (colorPrimary en light, colorSecondary en dark-night)
                    tint = Color.Unspecified
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            ReportStatRow(stringResource(R.string.n_report_quakes), report.nQuakes.toString())
            ReportStatRow(stringResource(R.string.n_quakes_sensitives), report.nSensitive.toString())
            ReportStatRow(stringResource(R.string.magnitude_mean), "${report.promMagnitude}")
            ReportStatRow(stringResource(R.string.mean_depth_epicentre), "${report.promDepth} km")
            ReportStatRow(stringResource(R.string.max_magnitude), "${report.maxMagnitude}")
            ReportStatRow(stringResource(R.string.min_depth), "${report.minDepth} km")

            val topCities = report.cityQuakes.take(4)
            if (topCities.isNotEmpty()) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Text(
                    text = stringResource(R.string.top_cities),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                topCities.forEach { city ->
                    ReportStatRow(city.city, city.nQuakes.toString())
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
private fun ReportStatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun monthName(month: Int) = when (month) {
    1 -> stringResource(R.string.JAN)
    2 -> stringResource(R.string.FEB)
    3 -> stringResource(R.string.MAR)
    4 -> stringResource(R.string.APR)
    5 -> stringResource(R.string.MAY)
    6 -> stringResource(R.string.JUN)
    7 -> stringResource(R.string.JUL)
    8 -> stringResource(R.string.AUG)
    9 -> stringResource(R.string.SEP)
    10 -> stringResource(R.string.OCT)
    11 -> stringResource(R.string.NOV)
    12 -> stringResource(R.string.DEC)
    else -> ""
}

private val previewReport = Report(
    reportMonth = "2024-03",
    nSensitive = 12,
    nQuakes = 142,
    promMagnitude = 3.4,
    promDepth = 56.2,
    maxMagnitude = 6.1,
    minDepth = 5.0,
    cityQuakes = listOf(
        CityQuakes("Santiago", 34),
        CityQuakes("Valparaíso", 22),
        CityQuakes("Concepción", 18),
        CityQuakes("La Serena", 9),
    )
)

@Preview(showBackground = true, name = "ReportCard — Light")
@Composable
private fun ReportCardLightPreview() {
    LastQuakeChileTheme(darkTheme = false) {
        ReportCard(report = previewReport, modifier = Modifier.padding(8.dp))
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES, name = "ReportCard — Dark")
@Composable
private fun ReportCardDarkPreview() {
    LastQuakeChileTheme(darkTheme = true) {
        ReportCard(report = previewReport, modifier = Modifier.padding(8.dp))
    }
}

@Preview(showBackground = true, fontScale = 1.5f, name = "ReportCard — Large font")
@Composable
private fun ReportCardLargeFontPreview() {
    LastQuakeChileTheme {
        ReportCard(report = previewReport, modifier = Modifier.padding(8.dp))
    }
}

@Preview(showBackground = true, name = "ReportCard — No cities")
@Composable
private fun ReportCardNoCitiesPreview() {
    LastQuakeChileTheme {
        ReportCard(
            report = previewReport.copy(cityQuakes = emptyList()),
            modifier = Modifier.padding(8.dp)
        )
    }
}
