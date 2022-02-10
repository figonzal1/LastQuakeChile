package cl.figonzal.lastquakechile.views.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import cl.figonzal.lastquakechile.R
import cl.figonzal.lastquakechile.core.ViewModelFactory
import cl.figonzal.lastquakechile.core.utils.calculateHours
import cl.figonzal.lastquakechile.core.utils.getMagnitudeColor
import cl.figonzal.lastquakechile.core.utils.setStatusImage
import cl.figonzal.lastquakechile.databinding.FragmentMapsNewBinding
import cl.figonzal.lastquakechile.databinding.InfoWindowsBinding
import cl.figonzal.lastquakechile.quake_feature.domain.model.Quake
import cl.figonzal.lastquakechile.quake_feature.ui.QuakeViewModel
import cl.figonzal.lastquakechile.views.activities.QuakeDetailsActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.*
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.maps.android.ktx.addCircle
import com.google.maps.android.ktx.addMarker
import kotlinx.coroutines.launch
import timber.log.Timber


class MapsFragmentNew : Fragment(), InfoWindowAdapter, OnInfoWindowClickListener,
    OnMapReadyCallback {

    private val MAPVIEW_BUNDLE_KEY = "MapViewBundleKey"

    private lateinit var quakeList: List<Quake>
    private lateinit var mapView: MapView
    private lateinit var binding: FragmentMapsNewBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentMapsNewBinding.inflate(inflater, container, false)

        mapView = binding.mapView
        mapView.onCreate(savedInstanceState)

        val viewModel: QuakeViewModel = ViewModelProvider(
            requireActivity(),
            ViewModelFactory(
                requireActivity().application
            )
        )[QuakeViewModel::class.java]

        lifecycleScope.launch {

            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    viewModel.quakeState.collect {
                        quakeList = it.quakes

                        Timber.i(getString(R.string.FRAGMENT_LOAD_LIST))
                    }
                }
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        //mapFragment?.getMapAsync(callback)
        mapView.getMapAsync(this)
    }

    @SuppressLint("PotentialBehaviorOverride")
    override fun onMapReady(p0: GoogleMap) {

        p0.apply {
            //Setear limites del mapa
            val mChile = LatLngBounds(LatLng(-60.15, -78.06), LatLng(-15.6, -66.5))
            setLatLngBoundsForCameraTarget(mChile)

            //NIght mode
            setNightMode()

            //configuraciones mapa
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
                CameraUpdateFactory.newLatLngZoom(
                    calculatePromCords(), 5.0f
                )
            )

            //Cargar pines
            cargarPines()

            setInfoWindowAdapter(this@MapsFragmentNew)

            setOnInfoWindowClickListener(this@MapsFragmentNew)
        }

        //Log zone
        Timber.i(getString(R.string.TAG_MAP_READY_RESPONSE))
    }

    private fun GoogleMap.setNightMode() {
        //NIGHT MODE MAPA
        val nightModeFlags =
            requireContext().resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK

        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
            setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireContext(),
                    R.raw.map_night_mode
                )
            )
        }
    }

    private fun GoogleMap.cargarPines() {

        for (quake in quakeList) {

            //LatLong of quake
            val epicentre = LatLng(quake.coordinates.latitude, quake.coordinates.longitude)

            //Search magnitude color
            val quakeColor = getMagnitudeColor(quake.magnitude, true)

            apply {

                addMarker {
                    position(epicentre)
                    alpha(0.9f)
                }?.tag = quake

                addCircle {
                    center(epicentre)
                    radius(10000 * quake.magnitude)
                    fillColor(requireContext().getColor(quakeColor))
                    strokeColor(requireContext().getColor(R.color.grey_dark_alpha))
                }


            }
        }
    }

    private fun calculatePromCords(): LatLng {
        var promLat = 0.0
        var promLong = 0.0

        quakeList.onEach {
            promLat += it.coordinates.latitude
            promLong += it.coordinates.longitude
        }

        promLat /= quakeList.size.toDouble()
        promLong /= quakeList.size.toDouble()

        return LatLng(promLat, promLong)
    }

    companion object {
        @JvmStatic
        fun newInstance(): MapsFragmentNew {
            return MapsFragmentNew()
        }
    }

    override fun onInfoWindowClick(p0: Marker) {
        val mObject = p0.tag
        val mModel = mObject as Quake?
        val mIntent = Intent(context, QuakeDetailsActivity::class.java)
        val mBundle = Bundle()

        /*
        if (mModel != null) {
            mBundle.putString(getString(R.string.INTENT_CIUDAD), mModel.city)
            mBundle.putString(getString(R.string.INTENT_REFERENCIA), mModel.reference)
            mBundle.putString(getString(R.string.INTENT_LATITUD), mModel.latitud)
            mBundle.putString(getString(R.string.INTENT_LONGITUD), mModel.longitud)

            //Cambiar la fecha local a string
            val mFormat = SimpleDateFormat(
                getString(R.string.DATETIME_FORMAT),
                Locale.US
            )
            mBundle.putString(
                getString(R.string.INTENT_FECHA_LOCAL),
                mFormat.format(mModel.localDate)
            )
            mBundle.putDouble(getString(R.string.INTENT_MAGNITUD), mModel.magnitude)
            mBundle.putDouble(getString(R.string.INTENT_PROFUNDIDAD), mModel.depth)
            mBundle.putString(getString(R.string.INTENT_ESCALA), mModel.scale)
            mBundle.putString(getString(R.string.INTENT_SENSIBLE), mModel.isSensitive)
            mBundle.putString(getString(R.string.INTENT_LINK_FOTO), mModel.imagen_url)
            mBundle.putString(getString(R.string.INTENT_ESTADO), mModel.estado)
            mIntent.putExtras(mBundle)
            Timber.i(getString(R.string.TAG_INTENT) + ": " + getString(R.string.TAG_INTENT_INFO_WINDOWS))
            startActivity(mIntent)
        }*/
    }

    override fun getInfoWindow(p0: Marker): View? {
        return null
    }

    override fun getInfoContents(p0: Marker): View {

        val quake = p0.tag as Quake

        val infoBinding = InfoWindowsBinding.inflate(layoutInflater)

        with(infoBinding) {

            tvIwReferencia.text = quake.reference

            tvIwMagnitud.text = String.format(
                requireContext().getString(R.string.magnitud),
                quake.magnitude
            )

            tvIwProfundidad.text =
                String.format(getString(R.string.profundidad_info_windows), quake.depth)

            ivIwMagColor.setColorFilter(
                requireContext().getColor(
                    getMagnitudeColor(quake.magnitude, false)
                )
            )

            tvIwHora.calculateHours(quake, requireContext())

            ivIwEstado.setStatusImage(quake.isVerified, tvIwEstado)
        }

        //Log zone
        Timber.i(getString(R.string.TAG_INFO_WINDOWS_RESPONSE))

        return infoBinding.root
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        var mMapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY)
        if (mMapViewBundle == null) {
            mMapViewBundle = Bundle()
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mMapViewBundle)
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
}