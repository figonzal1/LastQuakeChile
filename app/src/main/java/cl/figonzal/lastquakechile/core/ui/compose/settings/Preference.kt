package cl.figonzal.lastquakechile.core.ui.compose.settings

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cl.figonzal.lastquakechile.core.ui.theme.LastQuakeChileTheme

private const val DISABLED_ALPHA = 0.38f

@Composable
fun Preference(
    title: String,
    subTitle: String,
    modifier: Modifier = Modifier,
    isTitlePresent: Boolean = true,
    enabled: Boolean = true,
    onClick: () -> Unit = {},
) {
    val rippleIndication = remember { ripple() }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else DISABLED_ALPHA)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rippleIndication,
                enabled = enabled,
                onClick = onClick,
            ),
    ) {
        // Left spacer column — keeps content aligned with PreferenceCategory icon column.
        Column(modifier = Modifier.padding(start = 16.dp)) {
            Spacer(modifier = Modifier.size(24.dp))
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 32.dp, end = 16.dp, top = 12.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.Top,
        ) {
            if (isTitlePresent) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            if (subTitle.isNotEmpty()) {
                Text(
                    text = subTitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Preference — Light")
@Composable
private fun PreferenceLightPreview() {
    LastQuakeChileTheme {
        Column {
            Preference(
                title = "Contact developer",
                subTitle = "Suggestions? Problems?",
            )
            Preference(
                title = "Privacy policy",
                subTitle = "",
                isTitlePresent = true,
                enabled = false,
            )
        }
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Preference — Dark"
)
@Composable
private fun PreferenceDarkPreview() {
    LastQuakeChileTheme(darkTheme = true) {
        Preference(
            title = "Version",
            subTitle = "1.7.10",
        )
    }
}
