package cl.figonzal.lastquakechile.quake_feature.ui.compose

import android.animation.IntEvaluator
import android.animation.ValueAnimator
import android.graphics.Color
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import cl.figonzal.lastquakechile.core.ui.map.rememberMapViewWithLifecycle
import cl.figonzal.lastquakechile.core.utils.animate
import cl.figonzal.lastquakechile.core.utils.configMapType
import cl.figonzal.lastquakechile.core.utils.setNightMode
import cl.figonzal.lastquakechile.core.utils.views.getMagnitudeColor
import cl.figonzal.lastquakechile.quake_feature.domain.model.Quake
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.ktx.addCircle

/**
 * Renders a [com.google.android.gms.maps.MapView] via [AndroidView] interop.
 * Map lifecycle is handled by [rememberMapViewWithLifecycle].
 * Circle animators are cancelled in [DisposableEffect.onDispose].
 *
 * @param onMapReady called once when the [GoogleMap] is ready; used by the caller to store the
 *   reference for snapshot/share and to react to terrain-dialog result changes.
 */
@Composable
fun QuakeMap(
    quake: Quake,
    onMapReady: (GoogleMap) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val mapView = rememberMapViewWithLifecycle()
    val animators = remember { mutableListOf<ValueAnimator>() }

    DisposableEffect(Unit) {
        onDispose { animators.forEach { it.cancel() } }
    }

    // card_view_mapview.xml root was MaterialCardView — wrap to replicate elevation.
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        AndroidView(
            factory = {
                mapView.apply {
                    getMapAsync { googleMap ->
                        val quakeMagColor = context.getColor(getMagnitudeColor(quake.magnitude, true))
                        val greyAlpha = context.getColor(cl.figonzal.lastquakechile.R.color.grey_dark_alpha)
                        val latLng = LatLng(quake.coordinate.latitude, quake.coordinate.longitude)

                        googleMap.apply {
                            googleMap.setNightMode(context)
                            mapType = context.configMapType()

                            setMinZoomPreference(5.0f)
                            uiSettings.isZoomGesturesEnabled = false
                            uiSettings.isZoomControlsEnabled = true
                            uiSettings.isTiltGesturesEnabled = false
                            uiSettings.isScrollGesturesEnabled = false
                            uiSettings.isMapToolbarEnabled = false
                            uiSettings.isRotateGesturesEnabled = false
                            uiSettings.isCompassEnabled = false

                            addCircle {
                                center(latLng)
                                radius(90000.0)
                                fillColor(quakeMagColor)
                                strokeColor(greyAlpha)
                            }

                            addCircle {
                                center(latLng)
                                radius(3000.0)
                                fillColor(greyAlpha)
                                strokeColor(Color.TRANSPARENT)
                            }

                            addCircle {
                                center(latLng)
                                radius(90000.0)
                                strokeWidth(1f)
                                strokeColor(greyAlpha)
                            }.animate {
                                val anim = ValueAnimator.ofInt(0, 90000).apply {
                                    repeatMode = ValueAnimator.RESTART
                                    repeatCount = ValueAnimator.INFINITE
                                    duration = 4000
                                    setEvaluator(IntEvaluator())
                                    interpolator = AccelerateDecelerateInterpolator()
                                    addUpdateListener { animator ->
                                        this@animate.radius = (animator.animatedFraction * 140000).toDouble()
                                    }
                                    start()
                                }
                                animators += anim
                            }

                            addCircle {
                                center(latLng)
                                radius(90000.0)
                                strokeWidth(1f)
                                strokeColor(greyAlpha)
                            }.animate {
                                val anim = ValueAnimator.ofInt(0, 90000).apply {
                                    repeatMode = ValueAnimator.RESTART
                                    repeatCount = ValueAnimator.INFINITE
                                    duration = 4000
                                    startDelay = 1000
                                    setEvaluator(IntEvaluator())
                                    interpolator = AccelerateDecelerateInterpolator()
                                    addUpdateListener { animator ->
                                        this@animate.radius = (animator.animatedFraction * 140000).toDouble()
                                    }
                                    start()
                                }
                                animators += anim
                            }

                            moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 6.0f))
                        }

                        onMapReady(googleMap)
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}
