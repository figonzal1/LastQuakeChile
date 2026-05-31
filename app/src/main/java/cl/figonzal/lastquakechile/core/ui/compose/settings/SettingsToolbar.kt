package cl.figonzal.lastquakechile.core.ui.compose.settings

import android.content.res.Configuration
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import cl.figonzal.lastquakechile.R
import cl.figonzal.lastquakechile.core.ui.theme.LastQuakeChileTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsToolbar(
    title: String,
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        title = { Text(text = title) },
        navigationIcon = {
            IconButton(onClick = onNavigateUp) {
                Icon(
                    painter = painterResource(R.drawable.round_arrow_back_24),
                    contentDescription = null
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
        ),
        modifier = modifier,
    )
}

@Preview(showBackground = true, name = "SettingsToolbar — Light")
@Composable
private fun SettingsToolbarLightPreview() {
    LastQuakeChileTheme {
        SettingsToolbar(title = stringResource(R.string.menu_settings), onNavigateUp = {})
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "SettingsToolbar — Dark"
)
@Composable
private fun SettingsToolbarDarkPreview() {
    LastQuakeChileTheme(darkTheme = true) {
        SettingsToolbar(title = stringResource(R.string.menu_settings), onNavigateUp = {})
    }
}
