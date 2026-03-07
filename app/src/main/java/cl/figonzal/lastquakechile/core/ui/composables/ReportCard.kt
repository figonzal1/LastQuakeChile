package cl.figonzal.lastquakechile.core.ui.composables

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cl.figonzal.lastquakechile.R
import cl.figonzal.lastquakechile.core.ui.composables.theme.AppTheme
import cl.figonzal.lastquakechile.core.utils.views.getMonth
import cl.figonzal.lastquakechile.reports_feature.domain.model.CityQuakes
import cl.figonzal.lastquakechile.reports_feature.domain.model.Report

@Composable
fun ReportCard(
    report: Report,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val split = report.reportMonth.split("-")
    val month = context.getMonth(split[1].toInt())
    val year = split[0]

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(bottom = 16.dp)) {
            Row(
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.round_sticky_note_24),
                    contentDescription = stringResource(R.string.cd_monthly_report_icon),
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "$month $year",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
            )

            StatRow(
                label = stringResource(R.string.n_report_quakes),
                value = report.nQuakes.toString()
            )
            StatRow(
                label = stringResource(R.string.n_quakes_sensitives),
                value = report.nSensitive.toString()
            )
            StatRow(
                label = stringResource(R.string.magnitude_mean),
                value = "${report.promMagnitude}"
            )
            StatRow(
                label = stringResource(R.string.mean_depth_epicentre),
                value = "${report.promDepth} km"
            )
            StatRow(
                label = stringResource(R.string.max_magnitude),
                value = "${report.maxMagnitude}"
            )
            StatRow(
                label = stringResource(R.string.min_depth),
                value = "${report.minDepth} km"
            )

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
            )

            Text(
                text = stringResource(R.string.top_cities),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 32.dp, vertical = 4.dp)
            )
            report.cityQuakes.forEach { cityQuake ->
                StatRow(label = cityQuake.city, value = cityQuake.nQuakes.toString())
            }
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 2.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = value,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private val previewReport = Report(
    reportMonth = "2026-03",
    nSensitive = 12,
    nQuakes = 148,
    promMagnitude = 3.2,
    promDepth = 45.0,
    maxMagnitude = 6.1,
    minDepth = 5.0,
    cityQuakes = listOf(
        CityQuakes("Santiago", 32),
        CityQuakes("Valparaíso", 21),
        CityQuakes("Concepción", 18),
        CityQuakes("La Serena", 14)
    )
)

@Preview(showBackground = true)
@Composable
private fun ReportCardPreview() {
    AppTheme {
        ReportCard(report = previewReport)
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ReportCardPreviewDark() {
    AppTheme(darkTheme = true) {
        ReportCard(report = previewReport)
    }
}
