package cl.figonzal.lastquakechile.views.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import cl.figonzal.lastquakechile.R
import cl.figonzal.lastquakechile.core.utils.getMagnitudeColor
import cl.figonzal.lastquakechile.databinding.FragmentMapBinding
import cl.figonzal.lastquakechile.quake_feature.domain.model.Quake
import cl.figonzal.lastquakechile.views.activities.QuakeDetailsActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import timber.log.Timber

class MapFragment : Fragment(), OnMapReadyCallback, InfoWindowAdapter, OnInfoWindowClickListener {

    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap
    private var mPromLat = 0.0
    private var mPromLong = 0.0
    private lateinit var mListQuake: List<Quake>

    private lateinit var binding: FragmentMapBinding

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mapView.onCreate(savedInstanceState)
        mapView.onResume()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMapBinding.inflate(inflater, container, false)
        //val mQuakeListViewModel =
        //    ViewModelProvider(requireActivity())[QuakeListViewModel::class.java]

        mapView = binding.mapView
        //mQuakeListViewModel.showQuakeList()
        //    .observe(requireActivity(), { quakes: List<Quake> ->
        //        mListQuake = quakes
        //        mapView.getMapAsync(this@MapFragment)
        //    })
        return binding.root
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
        this.googleMap.clear()

        //NIGHT MODE MAPA
        val nightModeFlags =
            requireContext().resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
            googleMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireContext(),
                    R.raw.map_night_mode
                )
            )
        }

        //Setear info windows
        this.googleMap.setInfoWindowAdapter(this@MapFragment)

        //Setear info windows click listener
        this.googleMap.setOnInfoWindowClickListener(this@MapFragment)

        //Setear limites del mapa
        val mChile = LatLngBounds(LatLng(-60.15, -78.06), LatLng(-15.6, -66.5))
        this.googleMap.setLatLngBoundsForCameraTarget(mChile)

        //Configuraciones de mapa
        this.googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        this.googleMap.setMinZoomPreference(4.0f)
        this.googleMap.uiSettings.isZoomControlsEnabled = true
        this.googleMap.uiSettings.isTiltGesturesEnabled = true
        this.googleMap.uiSettings.isMapToolbarEnabled = true
        this.googleMap.uiSettings.isZoomGesturesEnabled = true
        this.googleMap.uiSettings.isRotateGesturesEnabled = false
        this.googleMap.uiSettings.isCompassEnabled = false
        mPromLat = 0.0
        mPromLong = 0.0

        //Cargar pines y circulos
        cargarPins(mListQuake)

        //Calculo de promedios
        mPromLat /= mListQuake.size.toDouble()
        mPromLong /= mListQuake.size.toDouble()

        //Calcular punto central de pines y ubicar camara en posicion promedio
        val mPuntoPromedio = LatLng(mPromLat, mPromLong)
        this.googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mPuntoPromedio, 5.0f))

        //Log zone
        Timber.i(getString(R.string.TAG_MAP_READY_RESPONSE))
    }

    /**
     * Funcion encargada de dibujar los pins y marcas circulares para cada sismos obtenido de la
     * lista.
     *
     * @param quakes Listado de sismos proveniente de viewModel
     */
    private fun cargarPins(quakes: List<Quake>) {
        for (i in quakes.indices) {

            /*
            //Obtener sismos i-esimo
            val mModel = quakes[i]

            //Obtener clase latlong del sismo
            val mLatLong = LatLng(mModel.latitud.toDouble(), mModel.longitud.toDouble())

            //Suma de lat y long
            mPromLat += mModel.latitud.toDouble()
            mPromLong += mModel.longitud.toDouble()

            //Buscar color
            val mIdColor = viewsManager.getMagnitudeColor(mModel.magnitude, true)

            //Marcador de epicentro
            googleMap.addMarker(
                MarkerOptions()
                    .position(mLatLong)
                    .alpha(0.9f)
            )!!.tag = mModel

            //Circulo en pin
            val mCircleOptions = CircleOptions()
                .center(mLatLong)
                .radius(10000 * mModel.magnitude)
                .fillColor(requireContext().getColor(mIdColor))
                .strokeColor(requireContext().getColor(R.color.grey_dark_alpha))
            googleMap.addCircle(mCircleOptions)*/
        }
    }

    override fun getInfoWindow(marker: Marker): View? {
        return null
    }

    override fun getInfoContents(marker: Marker): View? {

        @SuppressLint("InflateParams") val mView =
            layoutInflater.inflate(R.layout.info_windows, null)
        val mObject = marker.tag
        val mModel = mObject as Quake
        val mTvMagnitud = mView.findViewById<TextView>(R.id.tv_iw_magnitud)
        val mTvReferencia = mView.findViewById<TextView>(R.id.tv_iw_referencia)
        val mIvMagColor = mView.findViewById<ImageView>(R.id.iv_iw_mag_color)
        val mTvProfundidad = mView.findViewById<TextView>(R.id.tv_iw_profundidad)
        val mTvHora = mView.findViewById<TextView>(R.id.tv_iw_hora)
        val mTvEstado = mView.findViewById<TextView>(R.id.tv_iw_estado)
        val mIvEstado = mView.findViewById<ImageView>(R.id.iv_iw_estado)

        //SECCION ESTADO
        //val mEstado = mModel.estado

        //Setear estado e imagen del estado (Preliminar o verificado)
        //viewsManager.setStatusImage(requireContext(), mEstado, mTvEstado, mIvEstado)

        //Setear referencia del sismo en infoWindow
        mTvReferencia.text = mModel.reference

        //Setear magnitud del sismo dentro de circulo coloreado
        mTvMagnitud.text =
            String.format(requireContext().getString(R.string.magnitud), mModel.magnitude)

        //Colorear circulo según la magnitud del sismo
        val idColor = getMagnitudeColor(mModel.magnitude, false)
        mIvMagColor.setColorFilter(requireContext().getColor(idColor))

        //Setear la profundidad del sismo
        mTvProfundidad.text =
            String.format(getString(R.string.profundidad_info_windows), mModel.depth)

        //Calcular tiempos (Dates a DHMS)
        /*val mTiempos = dateHandler.dateToDHMS(
            mModel.localDate
        )*/

        //Separar mapeo de tiempos en dias, horas,minutos,segundos.
        /*val mDias = mTiempos[getString(R.string.UTILS_TIEMPO_DIAS)]
        val mMinutos = mTiempos[getString(R.string.UTILS_TIEMPO_MINUTOS)]
        val mHoras = mTiempos[getString(R.string.UTILS_TIEMPO_HORAS)]
        val mSegundos = mTiempos[getString(R.string.UTILS_TIEMPO_SEGUNDOS)]*/

        /*
        //Condiciones días.
        if (mDias != null && mDias == 0L) {
            if (mHoras != null && mHoras >= 1) {
                mTvHora.text =
                    String.format(getString(R.string.quake_time_hour_info_windows), mHoras)
            } else {
                mTvHora.text =
                    String.format(getString(R.string.quake_time_minute_info_windows), mMinutos)
                if (mMinutos != null && mMinutos < 1) {
                    mTvHora.text =
                        String.format(getString(R.string.quake_time_second_info_windows), mSegundos)
                }
            }
        } else if (mDias != null && mDias > 0) {
            when {
                mHoras != null && mHoras == 0L -> {
                    mTvHora.text =
                        String.format(getString(R.string.quake_time_day_info_windows), mDias)
                }
                mHoras != null && mHoras >= 1 -> {
                    mTvHora.text = String.format(
                        getString(R.string.quake_time_day_hour_info_windows), mDias, mHoras / 24
                    )
                }
            }
        }*/

        //Log zone
        Timber.i(getString(R.string.TAG_INFO_WINDOWS_RESPONSE))
        return mView
    }

    override fun onInfoWindowClick(marker: Marker) {
        val mObject = marker.tag
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

    companion object {
        private const val MAPVIEW_BUNDLE_KEY = "MapViewBundleKey"

        @JvmStatic
        fun newInstance(): MapFragment {
            return MapFragment()
        }
    }
}