package cl.figonzal.lastquakechile.quake_feature.ui.map

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.figonzal.lastquakechile.R
import cl.figonzal.lastquakechile.core.ui.MapViewInScroll
import cl.figonzal.lastquakechile.core.ui.map.rememberMapViewWithLifecycle
import cl.figonzal.lastquakechile.core.utils.calculateMeanCords
import cl.figonzal.lastquakechile.core.utils.setNightMode
import cl.figonzal.lastquakechile.core.utils.views.getMagnitudeColor
import cl.figonzal.lastquakechile.quake_feature.domain.model.Quake
import cl.figonzal.lastquakechile.quake_feature.ui.QuakeViewModel
import cl.figonzal.lastquakechile.quake_feature.ui.compose.QuakeBottomSheetContent
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.google.maps.android.ktx.addCircle
import org.koin.androidx.compose.koinViewModel
import timber.log.Timber

private const val MAX_MAP_PINS = 50

/**
 * Compose replacement for `MapsFragment` (fragment_maps.xml).
 *
 * Keeps the exact original map behaviour — Chile camera bounds, night mode, clustered pins
 * with magnitude circles loaded incrementally (max 50), and a bottom sheet on marker tap —
 * but hosts the [com.google.android.gms.maps.MapView] inside an [AndroidView] and replaces
 * the `BottomSheetBehavior` with a Material3 [ModalBottomSheet].
 *
 * @param mapType current map type (driven by the terrain dialog result from the host fragment).
 */
@SuppressLint("PotentialBehaviorOverride")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapsScreen(
    mapType: Int,
    modifier: Modifier = Modifier,
    viewModel: QuakeViewModel = koinViewModel(),
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // MapViewInScroll requests the ViewPager2 to stop intercepting touches, so map
    // pan/zoom gestures are not stolen by the horizontal pager (original regression fix).
    val mapView = rememberMapViewWithLifecycle { MapViewInScroll(it) }

    var googleMap by remember { mutableStateOf<GoogleMap?>(null) }
    var selectedQuake by remember { mutableStateOf<Quake?>(null) }
    val sheetState = rememberModalBottomSheetState()

    // Mutable holder that must survive recomposition without triggering it.
    val cluster = remember { MapClusterState() }

    // One-time map configuration.
    LaunchedEffect(mapView) {
        mapView.getMapAsync { map ->
            googleMap = map
            map.apply {
                setLatLngBoundsForCameraTarget(
                    LatLngBounds(LatLng(-60.15, -78.06), LatLng(-15.6, -66.5))
                )
                setNightMode(context)
                setMinZoomPreference(4.0f)

                with(uiSettings) {
                    isZoomControlsEnabled = true
                    isTiltGesturesEnabled = true
                    isMapToolbarEnabled = true
                    isZoomGesturesEnabled = true
                    isRotateGesturesEnabled = false
                    isCompassEnabled = false
                }

                cluster.clusterManager = ClusterManager<QuakeClusterItem>(context, this).also { cm ->
                    setOnCameraIdleListener(cm)
                    setOnMarkerClickListener(cm)

                    cm.setOnClusterItemClickListener { item ->
                        cluster.lastMarker?.setIcon(BitmapDescriptorFactory.defaultMarker())
                        val marker =
                            (cm.renderer as? DefaultClusterRenderer<QuakeClusterItem>)?.getMarker(item)
                        marker?.setIcon(
                            BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
                        )
                        cluster.lastMarker = marker

                        selectedQuake = item.quake
                        true
                    }
                }

                setOnMapClickListener { selectedQuake = null }
            }
            Timber.d("Map ready")
        }
    }

    // Apply terrain-dialog selection (and the persisted initial value) to the map.
    LaunchedEffect(mapType, googleMap) {
        googleMap?.mapType = mapType
    }

    // Incremental pin loading, mirroring MapsFragment.uiState collector + addIncrementalPins().
    LaunchedEffect(uiState.quakes, googleMap) {
        val map = googleMap ?: return@LaunchedEffect
        val quakes = uiState.quakes
        if (uiState.isLoading || quakes.isEmpty()) return@LaunchedEffect

        // Data set shrunk (e.g. pull-to-refresh) → reset overlays and recenter.
        if (quakes.size < cluster.loadedCount) {
            cluster.clearOverlays()
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(calculateMeanCords(quakes), 5.0f))
        }

        // First batch centers the camera, just like onMapReady originally did.
        if (cluster.loadedCount == 0) {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(calculateMeanCords(quakes), 5.0f))
        }

        cluster.addIncrementalPins(context, map, quakes)
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(factory = { mapView }, modifier = Modifier.fillMaxSize())
    }

    selectedQuake?.let { quake ->
        ModalBottomSheet(
            onDismissRequest = { selectedQuake = null },
            sheetState = sheetState
        ) {
            QuakeBottomSheetContent(quake = quake)
        }
    }
}

/**
 * Holds the imperative map-overlay state (cluster manager, drawn circles, highlighted marker
 * and how many pins were already drawn) outside the Compose snapshot system, so paging logic
 * matches the original fragment one-to-one.
 */
private class MapClusterState {
    var clusterManager: ClusterManager<QuakeClusterItem>? = null
    var lastMarker: Marker? = null
    val circles = mutableListOf<Circle>()
    var loadedCount = 0

    fun addIncrementalPins(
        context: android.content.Context,
        map: GoogleMap,
        quakes: List<Quake>,
    ) {
        val cm = clusterManager ?: return
        if (loadedCount >= MAX_MAP_PINS) return

        val newQuakes = quakes.subList(loadedCount, minOf(quakes.size, MAX_MAP_PINS))
        if (newQuakes.isEmpty()) return

        newQuakes.forEach { quake ->
            cm.addItem(QuakeClusterItem(quake))
            map.addCircle {
                center(LatLng(quake.coordinate.latitude, quake.coordinate.longitude))
                radius(10000 * quake.magnitude)
                fillColor(context.getColor(getMagnitudeColor(quake.magnitude, true)))
                strokeColor(context.getColor(R.color.grey_dark_alpha))
            }.also { circles.add(it) }
        }
        cm.cluster()
        loadedCount = minOf(quakes.size, MAX_MAP_PINS)
    }

    fun clearOverlays() {
        circles.forEach { it.remove() }
        circles.clear()
        clusterManager?.clearItems()
        clusterManager?.cluster()
        loadedCount = 0
    }
}
