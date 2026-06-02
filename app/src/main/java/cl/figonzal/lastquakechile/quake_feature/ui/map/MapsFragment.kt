package cl.figonzal.lastquakechile.quake_feature.ui.map

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import cl.figonzal.lastquakechile.R
import cl.figonzal.lastquakechile.core.ui.dialog.MapTerrainDialogFragment
import cl.figonzal.lastquakechile.core.utils.calculateMeanCords
import cl.figonzal.lastquakechile.core.utils.configMapType
import cl.figonzal.lastquakechile.core.utils.setBottomSheetQuakeData
import cl.figonzal.lastquakechile.core.utils.setNightMode
import cl.figonzal.lastquakechile.core.utils.views.configBottomSheetCallback
import cl.figonzal.lastquakechile.core.utils.views.configOptionsMenu
import cl.figonzal.lastquakechile.core.utils.views.getMagnitudeColor
import cl.figonzal.lastquakechile.core.utils.views.getViewBottomHeight
import cl.figonzal.lastquakechile.core.utils.views.handleBottomSheetState
import cl.figonzal.lastquakechile.databinding.FragmentMapsBinding
import cl.figonzal.lastquakechile.quake_feature.domain.model.Quake
import cl.figonzal.lastquakechile.quake_feature.ui.QuakeViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.card.MaterialCardView
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.google.maps.android.ktx.addCircle
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import timber.log.Timber

private const val mapViewKey = "MapViewBundleKey"

class MapsFragment : Fragment(), OnMapReadyCallback {

    private val viewModel: QuakeViewModel by activityViewModel()

    private var _binding: FragmentMapsBinding? = null
    private val binding get() = _binding!!

    private var quakeList: List<Quake> = listOf()
    private var sheetBehavior: BottomSheetBehavior<MaterialCardView>? = null

    private var googleMap: GoogleMap? = null
    private var lastMarker: Marker? = null
    private var isFirstInit = true

    private var clusterManager: ClusterManager<QuakeClusterItem>? = null
    private val circles = mutableListOf<Circle>()
    private var loadedQuakesCount = 0

    companion object {
        private const val MAX_MAP_PINS = 50
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentMapsBinding.inflate(inflater, container, false)

        parentFragmentManager.setFragmentResultListener(
            MapTerrainDialogFragment.REQUEST_KEY, viewLifecycleOwner
        ) { _, bundle ->
            googleMap?.mapType = bundle.getInt(MapTerrainDialogFragment.RESULT_MAP_TYPE)
        }

        binding.mapView.onCreate(savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {

            viewModel.uiState
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .collectLatest {

                    when {
                        !it.isLoading && it.quakes.isNotEmpty() -> {
                            if (it.quakes.size < loadedQuakesCount) {
                                clearMapOverlays()
                                googleMap?.moveCamera(
                                    CameraUpdateFactory.newLatLngZoom(
                                        calculateMeanCords(it.quakes), 5.0f
                                    )
                                )
                            }
                            quakeList = it.quakes
                            Timber.d("List loaded in fragment")

                            if (googleMap == null) {
                                binding.mapView.getMapAsync(this@MapsFragment)
                            } else {
                                addIncrementalPins()
                            }
                        }
                    }
                }
        }

        with(binding.include.cvBottomSheet) {
            sheetBehavior = BottomSheetBehavior.from(this).also {
                it.isHideable = true
                it.state = BottomSheetBehavior.STATE_HIDDEN

                getViewBottomHeight(R.id.sheet_content, it)
            }
        }

        return binding.root
    }

    @SuppressLint("PotentialBehaviorOverride")
    override fun onMapReady(p0: GoogleMap) {

        googleMap = p0

        if (isFirstInit) {
            configOptionsMenu(fragmentIndex = 2) {
                when (it.itemId) {
                    R.id.layers_menu -> {
                        MapTerrainDialogFragment.newInstance()
                            .show(parentFragmentManager, "map_terrain")
                    }
                }
            }
            isFirstInit = false
        }
        p0.apply {

            sheetBehavior?.apply {
                addBottomSheetCallback(configBottomSheetCallback(p0, binding))
            }

            val mChile = LatLngBounds(LatLng(-60.15, -78.06), LatLng(-15.6, -66.5))
            setLatLngBoundsForCameraTarget(mChile)

            setNightMode(requireContext())

            mapType = requireContext().configMapType()
            setMinZoomPreference(4.0f)

            with(uiSettings) {
                isZoomControlsEnabled = true
                isTiltGesturesEnabled = true
                isMapToolbarEnabled = true
                isZoomGesturesEnabled = true
                isRotateGesturesEnabled = false
                isCompassEnabled = false
            }

            moveCamera(
                CameraUpdateFactory.newLatLngZoom(calculateMeanCords(quakeList), 5.0f)
            )

            clusterManager = ClusterManager<QuakeClusterItem>(requireContext(), this).also { cm ->
                setOnCameraIdleListener(cm)
                setOnMarkerClickListener(cm)

                cm.setOnClusterItemClickListener { item ->
                    lastMarker?.setIcon(BitmapDescriptorFactory.defaultMarker())
                    val marker = (cm.renderer as? DefaultClusterRenderer<QuakeClusterItem>)?.getMarker(item)
                    marker?.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                    lastMarker = marker

                    sheetBehavior?.handleBottomSheetState()
                    requireContext().setBottomSheetQuakeData(item.quake, binding.include)
                    true
                }
            }

            setOnMapClickListener {
                sheetBehavior?.state = BottomSheetBehavior.STATE_HIDDEN
            }
        }

        addIncrementalPins()

        Timber.d("Map ready")
    }

    private fun addIncrementalPins() {
        val cm = clusterManager ?: return
        if (loadedQuakesCount >= MAX_MAP_PINS) return

        val newQuakes = quakeList.subList(
            loadedQuakesCount,
            minOf(quakeList.size, MAX_MAP_PINS)
        )
        if (newQuakes.isEmpty()) return

        newQuakes.forEach { quake ->
            cm.addItem(QuakeClusterItem(quake))
            googleMap?.addCircle {
                center(LatLng(quake.coordinate.latitude, quake.coordinate.longitude))
                radius(10000 * quake.magnitude)
                fillColor(requireContext().getColor(getMagnitudeColor(quake.magnitude, true)))
                strokeColor(requireContext().getColor(R.color.grey_dark_alpha))
            }?.also { circles.add(it) }
        }
        cm.cluster()
        loadedQuakesCount = minOf(quakeList.size, MAX_MAP_PINS)
    }

    private fun clearMapOverlays() {
        circles.forEach { it.remove() }
        circles.clear()
        clusterManager?.clearItems()
        clusterManager?.cluster()
        loadedQuakesCount = 0
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        var mMapViewBundle = outState.getBundle(mapViewKey)
        if (mMapViewBundle == null) {
            mMapViewBundle = Bundle()
            outState.putBundle(mapViewKey, mMapViewBundle)
        }
        binding.mapView.onSaveInstanceState(mMapViewBundle)
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        binding.mapView.onStop()
    }

    override fun onPause() {
        binding.mapView.onPause()
        super.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.mapView.onDestroy()
        _binding = null
    }
}
