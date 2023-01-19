package cl.figonzal.lastquakechile.quake_feature.ui.map

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import cl.figonzal.lastquakechile.R
import cl.figonzal.lastquakechile.core.services.notifications.utils.QUAKE
import cl.figonzal.lastquakechile.core.ui.dialog.MapTerrainDialogFragment
import cl.figonzal.lastquakechile.core.utils.*
import cl.figonzal.lastquakechile.core.utils.views.QUAKE_DETAILS_MAGNITUDE_FORMAT
import cl.figonzal.lastquakechile.databinding.FragmentMapsBinding
import cl.figonzal.lastquakechile.databinding.InfoWindowsBinding
import cl.figonzal.lastquakechile.quake_feature.domain.model.Quake
import cl.figonzal.lastquakechile.quake_feature.ui.QuakeDetailsActivity
import cl.figonzal.lastquakechile.quake_feature.ui.QuakeViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.*
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import timber.log.Timber

private const val mapViewKey = "MapViewBundleKey"

class MapsFragment : Fragment(), InfoWindowAdapter, OnInfoWindowClickListener, OnMapReadyCallback {

    private val viewModel: QuakeViewModel by activityViewModel()
    private var _binding: FragmentMapsBinding? = null
    private val binding get() = _binding!!

    private var quakeList: List<Quake> = listOf()

    private var sheetBehavior: BottomSheetBehavior<MaterialCardView>? = null

    private var isFirstInit = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentMapsBinding.inflate(inflater, container, false)

        binding.mapView.onCreate(savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {

            viewModel.firstPageState
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .collectLatest {

                    when {
                        !it.isLoading && it.quakes.isNotEmpty() -> {
                            quakeList = it.quakes
                            Timber.d("List loaded in fragment")

                            binding.mapView.getMapAsync(this@MapsFragment)
                        }
                    }
                }
        }

        //Initialization of bottomSheetBehavior
        sheetBehavior = BottomSheetBehavior.from(binding.include.bottomSheet).also {
            it.isHideable = true
            it.state = BottomSheetBehavior.STATE_HIDDEN
        }

        return binding.root
    }

    @SuppressLint("PotentialBehaviorOverride")
    override fun onMapReady(p0: GoogleMap) {

        if (isFirstInit) {
            configOptionsMenu(fragmentIndex = 2) {
                when (it.itemId) {
                    R.id.layers_menu -> {
                        MapTerrainDialogFragment(p0).show(
                            parentFragmentManager,
                            "Dialogo mapType"
                        )
                    }
                }
            }
            isFirstInit = false
        }
        p0.apply {

            sheetBehavior?.apply {
                addBottomSheetCallback(configBottomSheetCallback(p0, binding))
            }

            //Set limits for map
            val mChile = LatLngBounds(LatLng(-60.15, -78.06), LatLng(-15.6, -66.5))
            setLatLngBoundsForCameraTarget(mChile)

            //Night mode
            setNightMode(requireContext())

            //Map configs
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

            loadPins(quakeList, requireContext())

            setInfoWindowAdapter(this@MapsFragment)

            setOnInfoWindowClickListener(this@MapsFragment)
        }

        //Log zone
        Timber.d("Map ready")
    }

    override fun onInfoWindowClick(p0: Marker) {
        val quake = p0.tag as Quake
        Intent(context, QuakeDetailsActivity::class.java).apply {
            putExtra(QUAKE, quake)
            startActivity(this)
        }
    }

    override fun getInfoWindow(p0: Marker): Nothing? = null

    override fun getInfoContents(p0: Marker): View {

        sheetBehavior?.handleBottomSheetBehaviorState()

        val quake = p0.tag as Quake

        val infoBinding = InfoWindowsBinding.inflate(layoutInflater)

        with(infoBinding) {

            tvIwReference.text = quake.reference

            tvIwMagnitude.text = String.format(
                QUAKE_DETAILS_MAGNITUDE_FORMAT,
                quake.magnitude
            )

            tvIwDepth.text =
                String.format(getString(R.string.profundidad_info_windows), quake.depth)

            ivIwMagColor.setColorFilter(
                resources.getColor(
                    getMagnitudeColor(quake.magnitude, false), requireActivity().theme
                )
            )

            tvIwHour.timeToText(quake)

            ivIwIsVerified.setStatusImage(quake.isVerified, tvIwIsVerified)
        }

        //Log zone
        Timber.d("Info windows open")

        return infoBinding.root
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