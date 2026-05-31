package cl.figonzal.lastquakechile.core.ui.map

import android.content.Context
import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.android.gms.maps.MapView

/**
 * Creates a [MapView] that is bound to the current [LocalLifecycleOwner].
 * All lifecycle events (onCreate → onDestroy + onLowMemory) are forwarded automatically,
 * so callers only need to call [MapView.getMapAsync] to interact with the map.
 *
 * Reusable across Phase 3 (QuakeDetailsActivity) and Phase 5 (MapsScreen).
 *
 * @param factory how to instantiate the [MapView]. The map inside the swipeable pager
 * (MapsScreen) must use a [cl.figonzal.lastquakechile.core.ui.MapViewInScroll] so the
 * surrounding `ViewPager2` does not steal pan/zoom gestures.
 */
@Composable
fun rememberMapViewWithLifecycle(
    factory: (Context) -> MapView = { MapView(it) }
): MapView {
    val context = LocalContext.current
    val mapView = remember { factory(context) }
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_CREATE -> mapView.onCreate(Bundle())
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> {}
            }
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }

    return mapView
}
