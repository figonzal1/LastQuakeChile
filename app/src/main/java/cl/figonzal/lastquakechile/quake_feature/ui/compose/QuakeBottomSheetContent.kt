package cl.figonzal.lastquakechile.quake_feature.ui.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cl.figonzal.lastquakechile.R
import cl.figonzal.lastquakechile.core.ui.components.MagnitudeHeader
import cl.figonzal.lastquakechile.core.utils.openQuakeDetails
import cl.figonzal.lastquakechile.quake_feature.domain.model.Quake

/**
 * Content shown inside the map's [androidx.compose.material3.ModalBottomSheet] when a
 * cluster marker is tapped. Compose port of quake_bottom_sheet.xml +
 * card_view_quake_bottom_sheet.xml.
 *
 * Reuses [MagnitudeHeader] (magnitude circle + city/time/reference) and offers the two
 * original actions: open details and share (snapshot-from-bottom-sheet).
 */
@Composable
fun QuakeBottomSheetContent(
    quake: Quake,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp)
    ) {
        MagnitudeHeader(quake = quake)

        // primary en dark mode es un azul marino muy oscuro (color de fondo del toolbar),
        // ilegible como contenido sobre la superficie del sheet. Se fuerza onSurface para
        // garantizar contraste en ambos modos.
        val buttonColors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.onSurface
        )

        Row(
            modifier = Modifier.padding(start = 16.dp, top = 8.dp, end = 16.dp)
        ) {
            OutlinedButton(
                onClick = { context.openQuakeDetails(quake) },
                colors = buttonColors
            ) {
                Icon(
                    painter = painterResource(R.drawable.round_open_in_new_24),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.details))
            }

            Spacer(Modifier.width(8.dp))

            OutlinedButton(
                onClick = {
                    context.openQuakeDetails(
                        quake,
                        isSnapshotRequestInBottomSheet = true
                    )
                },
                colors = buttonColors
            ) {
                Icon(
                    painter = painterResource(R.drawable.round_share_24),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.share))
            }
        }
    }
}
