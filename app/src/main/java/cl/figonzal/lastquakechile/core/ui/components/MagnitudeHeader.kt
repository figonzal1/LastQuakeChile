package cl.figonzal.lastquakechile.core.ui.components

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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cl.figonzal.lastquakechile.R
import cl.figonzal.lastquakechile.core.utils.views.getMagnitudeColor
import cl.figonzal.lastquakechile.core.utils.views.quakeTimeText
import cl.figonzal.lastquakechile.core.utils.views.toast
import cl.figonzal.lastquakechile.quake_feature.domain.model.Quake
import java.util.Locale

/**
 * Shared header row used by both [QuakeCard] and [QuakeDetailCard].
 * Renders: magnitude circle with optional verified badge + city / time / reference row.
 */
@Composable
fun MagnitudeHeader(
    quake: Quake,
    modifier: Modifier = Modifier,
    startPadding: Dp = 16.dp,
    topPadding: Dp = 16.dp,
    endPadding: Dp = 16.dp,
) {
    val context = LocalContext.current
    val magnitudeColor = colorResource(getMagnitudeColor(quake.magnitude, false))
    val timeText = quakeTimeText(context, quake, isShortVersion = true)

    // Original XML: circle and text are top-aligned, not center-aligned.
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = startPadding, top = topPadding, end = endPadding),
        verticalAlignment = Alignment.Top
    ) {
        MagnitudeCircle(quake = quake, magnitudeColor = magnitudeColor)

        Spacer(modifier = Modifier.width(16.dp))

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
}

/**
 * 50dp squircle showing the magnitude value, with an optional verified badge.
 * Extracted so it can be reused in list cards and the detail card.
 */
@Composable
fun MagnitudeCircle(
    quake: Quake,
    magnitudeColor: Color,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    // background_magnitude_circle_shape is a 100dp rect with corner_radius_32dp (=24dp).
    // FIT_CENTER scales it 0.5x into the 50dp ImageView → 12dp effective corners.
    // RoundedCornerShape(12.dp) replicates that squircle, not a plain circle.
    Box(modifier = modifier.size(50.dp)) {
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
        // Verified badge: background_verified_quake layer-list (white oval + round_verified_24).
        // layer-list is not supported by painterResource so we compose the two layers manually.
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
}
