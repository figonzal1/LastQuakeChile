package cl.figonzal.lastquakechile.quake_feature.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import cl.figonzal.lastquakechile.R
import cl.figonzal.lastquakechile.core.ui.dialog.MapTerrainDialogFragment
import cl.figonzal.lastquakechile.core.utils.*
import cl.figonzal.lastquakechile.databinding.FragmentMapsBinding
import cl.figonzal.lastquakechile.databinding.InfoWindowsBinding
import cl.figonzal.lastquakechile.quake_feature.domain.model.Quake
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.*
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import timber.log.Timber


class MapsFragment : Fragment(), InfoWindowAdapter, OnInfoWindowClickListener,
    OnMapReadyCallback {

    private val viewModel: QuakeViewModel by sharedViewModel()
    private val mapViewKey = "MapViewBundleKey"

    private lateinit var quakeList: List<Quake>
    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap
    private lateinit var binding: FragmentMapsBinding


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentMapsBinding.inflate(inflater, container, false)

        mapView = binding.mapView
        mapView.onCreate(savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {

            viewModel.quakeState
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .collect {
                    quakeList = it.quakes
                    Timber.d(getString(R.string.FRAGMENT_LOAD_LIST))
                }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView.getMapAsync(this)
        setHasOptionsMenu(true)
    }

    @SuppressLint("PotentialBehaviorOverride")
    override fun onMapReady(p0: GoogleMap) {

        p0.apply {

            googleMap = this

            //Set limits for map
            val mChile = LatLngBounds(LatLng(-60.15, -78.06), LatLng(-15.6, -66.5))
            setLatLngBoundsForCameraTarget(mChile)

            //NIght mode
            setNightMode(requireContext())

            //Map configs
            mapType = MAP_TYPE_NORMAL
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
        Timber.d(getString(R.string.MAP_READY_RESPONSE))
    }

    override fun onInfoWindowClick(p0: Marker) {
        val quake = p0.tag as Quake
        Intent(context, QuakeDetailsActivity::class.java).apply {
            putExtra(getString(R.string.INTENT_QUAKE), quake)
            startActivity(this)
        }
    }

    override fun getInfoWindow(p0: Marker) = null

    override fun getInfoContents(p0: Marker): View {

        val quake = p0.tag as Quake

        val infoBinding = InfoWindowsBinding.inflate(layoutInflater)

        with(infoBinding) {

            tvIwReference.text = quake.reference

            tvIwMagnitude.text = String.format(
                requireContext().getString(R.string.magnitud),
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
        Timber.d(getString(R.string.INFO_WINDOWS_RESPONSE))

        return infoBinding.root
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        var mMapViewBundle = outState.getBundle(mapViewKey)
        if (mMapViewBundle == null) {
            mMapViewBundle = Bundle()
            outState.putBundle(mapViewKey, mMapViewBundle)
        }
        mapView.onSaveInstanceState(mMapViewBundle)
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onPause() {
        mapView.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        mapView.onDestroy()
        super.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.fragment_map_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {
            R.id.layers_menu -> {

                MapTerrainDialogFragment(googleMap).show(parentFragmentManager, "Dialogo mapType")
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}