package cl.figonzal.lastquakechile.core.ui.composables

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cl.figonzal.lastquakechile.R
import cl.figonzal.lastquakechile.core.ui.composables.theme.AppTheme
import cl.figonzal.lastquakechile.core.utils.views.getMagnitudeColor
import cl.figonzal.lastquakechile.core.utils.views.toRelativeTimeText
import cl.figonzal.lastquakechile.quake_feature.domain.model.Coordinate
import cl.figonzal.lastquakechile.quake_feature.domain.model.Quake
import java.util.Locale

@Composable
fun QuakeCard(
    quake: Quake,
    onClick: () -> Unit,
    onVerifiedClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 16.dp, end = 16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(modifier = Modifier.size(50.dp)) {
                val circleColor = colorResource(getMagnitudeColor(quake.magnitude, false))
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(circleColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = String.format(Locale.getDefault(), "%.1f", quake.magnitude),
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White
                    )
                }
                if (quake.isVerified) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .align(Alignment.TopEnd)
                            .offset(x = 5.dp, y = (-5).dp)
                            .clickable(onClick = onVerifiedClick),
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
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f).padding(top = 4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = quake.city,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (quake.isSensitive) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                painter = painterResource(R.drawable.round_warning_24),
                                contentDescription = stringResource(R.string.cd_sensitive_icon),
                                tint = Color.Unspecified,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Text(
                        text = quake.toRelativeTimeText(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                Text(
                    text = quake.reference,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
        }
        HorizontalDivider(
            modifier = Modifier
                .padding(start = 82.dp, end = 16.dp, top = 16.dp),
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
        )
    }
}

private val previewQuake = Quake(
    quakeCode = 1,
    localDate = "2026-03-06 12:00:00",
    city = "Santiago",
    reference = "10 km al NE de Santiago",
    magnitude = 4.5,
    depth = 30.0,
    scale = "Ml",
    isSensitive = true,
    isVerified = true,
    coordinate = Coordinate(latitude = -33.45, longitude = -70.67)
)

@Preview(showBackground = true)
@Composable
private fun QuakeCardPreview() {
    AppTheme {
        QuakeCard(quake = previewQuake, onClick = {}, onVerifiedClick = {})
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun QuakeCardPreviewDark() {
    AppTheme(darkTheme = true) {
        QuakeCard(quake = previewQuake, onClick = {}, onVerifiedClick = {})
    }
}
