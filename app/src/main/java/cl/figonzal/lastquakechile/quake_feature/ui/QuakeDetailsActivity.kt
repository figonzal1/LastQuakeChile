package cl.figonzal.lastquakechile.quake_feature.ui

import android.animation.IntEvaluator
import android.animation.ValueAnimator
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import cl.figonzal.lastquakechile.R
import cl.figonzal.lastquakechile.core.utils.*
import cl.figonzal.lastquakechile.databinding.ActivityQuakeDetailsBinding
import cl.figonzal.lastquakechile.quake_feature.domain.model.Quake
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.MAP_TYPE_NORMAL
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.ktx.addCircle
import timber.log.Timber
import java.time.format.DateTimeFormatter

class QuakeDetailsActivity : AppCompatActivity(), OnMapReadyCallback {

    private val MAPVIEW_BUNDLE_KEY = "MapViewBundleKey"
    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap
    private var mBitmapUri: Uri? = null
    private lateinit var quake: Quake

    private lateinit var binding: ActivityQuakeDetailsBinding


    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuakeDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mapView = binding.includeMapview.mapView
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        bindingResources()
    }

    private fun bindingResources() {

        setSupportActionBar(binding.includeToolbar.materialToolbar)

        //Muestra la flecha en toolbar para volver atras
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
        }

        handleBundles()
    }

    private fun handleBundles() {

        //Obtener datos desde intent
        quake = intent.extras?.get("quake") as Quake

        //Seteo de textview
        setTextViews()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        // Respond to the action bar's Up/Home button
        if (item.itemId == android.R.id.home) {
            Timber.i(getString(R.string.TAG_INTENT_DETALLE_HOME_UP_RESPONSE))
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setTextViews() {


        supportActionBar?.title = quake.city

        with(binding.includeCvQuakeDetail) {

            //Set city name
            tvCiudadDetail.text = quake.city

            //Setear mReferencia
            tvReferenciaDetail.text = quake.reference

            //Setear mMagnitud en en circulo de color
            tvMagnitudDetail.text =
                String.format(getString(R.string.magnitud), quake.magnitude)

            //Setear el color de background dependiendo de mMagnitud del sismo
            ivMagColorDetail.setColorFilter(getColor(getMagnitudeColor(quake.magnitude, false)))

            //Setear mProfundidad
            tvEpicentro.text =
                String.format(getString(R.string.quake_details_profundidad), quake.depth)

            //Setear fecha
            tvFecha.text =
                quake.localDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

            //Calculo de Grados,Minutos y segundos
            tvGms.formatDMS(quake.coordinates)

            //Calcular hora
            tvHoraDetail.setTimeToTextView(quake.localDate.localDateToDHMS())

            //Scale
            tvEscala.text = quake.scale

            //sensitive
            ivSensibleDetail.visibility = when {
                quake.isSensitive -> View.VISIBLE
                else -> View.GONE
            }
        }

        //Calculo de estado
        binding.ivEstado.setStatusImage(quake.isVerified, binding.tvEstado)
    }

    override fun onMapReady(p0: GoogleMap) {

        val quakeMagColor: Int = getColor(getMagnitudeColor(quake.magnitude, true))
        val greyAlpha = getColor(R.color.grey_dark_alpha)

        val latLong = LatLng(quake.coordinates.latitude, quake.coordinates.longitude)

        //Circulo grande con color segun magnitud
        p0.apply {

            //NIght mode
            setNightMode(this@QuakeDetailsActivity)
            //configuraciones mapa
            mapType = MAP_TYPE_NORMAL

            addCircle {
                center(latLong)
                radius(90000.0)
                fillColor(quakeMagColor)
                strokeColor(greyAlpha)
            }

            addCircle {
                center(latLong)
                radius(3000.0)
                fillColor(greyAlpha)
                strokeColor(Color.TRANSPARENT)
            }

            addCircle {
                center(latLong)
                radius(90000.0)
                strokeWidth(1f)
                strokeColor(greyAlpha)
            }.animate {
                ValueAnimator.ofInt(0, 90000).apply {
                    repeatMode = ValueAnimator.RESTART
                    repeatCount = ValueAnimator.INFINITE
                    duration = 4000
                    setEvaluator(IntEvaluator())
                    interpolator = AccelerateDecelerateInterpolator()
                    addUpdateListener {
                        this@animate.radius = (it.animatedFraction * 140000).toDouble()
                    }
                    start()
                }
            }

            addCircle {
                center(latLong)
                radius(90000.0)
                strokeWidth(1f)
                strokeColor(greyAlpha)
            }.animate {
                ValueAnimator.ofInt(0, 90000).apply {
                    repeatMode = ValueAnimator.RESTART
                    repeatCount = ValueAnimator.INFINITE
                    duration = 4000
                    startDelay = 1000
                    setEvaluator(IntEvaluator())
                    interpolator = AccelerateDecelerateInterpolator()
                    addUpdateListener {
                        this@animate.radius = (it.animatedFraction * 140000).toDouble()
                    }
                    start()
                }
            }

            moveCamera(CameraUpdateFactory.newLatLngZoom(latLong, 6.0f))

            //Log zone
            Timber.i(getString(R.string.TAG_MAP_READY_RESPONSE))

            //Seteo de floating buttons
            binding.fabShare.setOnClickListener {
                Timber.i(getString(R.string.TAG_FAB_SHARE_STATUS) + ": " + getString(R.string.TAG_FAB_SHARE_STATUS_CLICKED))
                this@QuakeDetailsActivity.makeSnapshot(p0, quake)
            }
        }

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
