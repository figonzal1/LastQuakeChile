package cl.figonzal.lastquakechile.quake_feature.ui.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.compose.LocalLifecycleOwner
import cl.figonzal.lastquakechile.R
import cl.figonzal.lastquakechile.core.ui.dialog.MapTerrainDialogFragment
import cl.figonzal.lastquakechile.core.utils.makeSnapshot
import cl.figonzal.lastquakechile.quake_feature.domain.model.Quake
import com.google.android.gms.maps.GoogleMap
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuakeDetailScreen(
    quake: Quake,
    isSnapshotRequest: Boolean,
    fragmentManager: FragmentManager,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var googleMap by remember { mutableStateOf<GoogleMap?>(null) }

    // Listen for terrain dialog result and update the map type in-place.
    DisposableEffect(fragmentManager, lifecycleOwner) {
        fragmentManager.setFragmentResultListener(
            MapTerrainDialogFragment.REQUEST_KEY,
            lifecycleOwner
        ) { _, bundle ->
            googleMap?.mapType = bundle.getInt(MapTerrainDialogFragment.RESULT_MAP_TYPE)
        }
        onDispose { }
    }

    // Automatic snapshot triggered by notification deep-link.
    LaunchedEffect(googleMap) {
        val map = googleMap ?: return@LaunchedEffect
        if (isSnapshotRequest) {
            delay(1000)
            context.makeSnapshot(map, quake)
        }
    }

    val screenHeightDp = LocalConfiguration.current.screenHeightDp
    val mapHeightDp = (screenHeightDp * 0.4f).dp

    Scaffold(
        modifier = modifier,
        topBar = {
            // MaterialToolbar original usaba colorPrimary como fondo.
            // M3 TopAppBar por defecto usa surface (blanco/gris) — se sobreescribe explícito.
            TopAppBar(
                title = {
                    Text(
                        text = quake.city,
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(R.drawable.round_arrow_back_24),
                            contentDescription = null
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        MapTerrainDialogFragment.newInstance()
                            .show(fragmentManager, "map_terrain")
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.round_layers_24),
                            contentDescription = stringResource(R.string.menu_map_style_title)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
                )
            )
        },
        floatingActionButton = {
            // onPrimaryContainer no está definido en el esquema customizado y M3 lo calcula
            // como rosado. Se fuerza primary/onPrimary para coincidir con el toolbar.
            FloatingActionButton(
                onClick = { googleMap?.let { context.makeSnapshot(it, quake) } },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) {
                Icon(
                    painter = painterResource(R.drawable.round_share_24),
                    contentDescription = stringResource(R.string.cd_share_quake_button)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 8.dp)
        ) {
            QuakeMap(
                quake = quake,
                onMapReady = { map -> googleMap = map },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(mapHeightDp)
                    .padding(top = 16.dp, bottom = 4.dp)
            )

            NativeAdCard(modifier = Modifier.padding(top = 4.dp))

            QuakeDetailCard(
                quake = quake,
                modifier = Modifier
                    .padding(top = 8.dp, bottom = 64.dp)
                    .fillMaxWidth()
            )
        }
    }
}
