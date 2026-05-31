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
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cl.figonzal.lastquakechile.core.ui.theme.LastQuakeChileTheme

private const val DISABLED_ALPHA = 0.38f

@Composable
fun SwitchPreference(
    title: String,
    subTitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
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
                onClick = { onCheckedChange(!checked) },
            ),
    ) {
        // Left spacer — same as Preference, keeps text aligned with category headers.
        Column(modifier = Modifier.padding(start = 16.dp)) {
            Spacer(modifier = Modifier.size(24.dp))
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 32.dp, end = 8.dp, top = 12.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.Top,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = subTitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            modifier = Modifier.padding(end = 16.dp),
        )
    }
}

@Preview(showBackground = true, name = "SwitchPreference — ON")
@Composable
private fun SwitchPreferenceOnPreview() {
    LastQuakeChileTheme {
        Column {
            var checked by remember { mutableStateOf(true) }
            SwitchPreference(
                title = "Quake alerts",
                subTitle = "Enabled",
                checked = checked,
                onCheckedChange = { checked = it },
            )
            SwitchPreference(
                title = "High priority",
                subTitle = "Show previews of notifications at the top of the screen",
                checked = false,
                onCheckedChange = {},
                enabled = false,
            )
        }
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "SwitchPreference — Dark"
)
@Composable
private fun SwitchPreferenceDarkPreview() {
    LastQuakeChileTheme(darkTheme = true) {
        SwitchPreference(
            title = "Night mode",
            subTitle = "Disabled",
            checked = false,
            onCheckedChange = {},
        )
    }
}
