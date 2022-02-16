package cl.figonzal.lastquakechile.quake_feature.ui

import android.animation.IntEvaluator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import cl.figonzal.lastquakechile.R
import cl.figonzal.lastquakechile.core.utils.*
import cl.figonzal.lastquakechile.databinding.ActivityQuakeDetailsBinding
import cl.figonzal.lastquakechile.quake_feature.domain.model.Quake
import com.google.android.gms.ads.*
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.ktx.addCircle
import timber.log.Timber
import java.time.format.DateTimeFormatter

class QuakeDetailsActivity : AppCompatActivity(), OnMapReadyCallback {

    //MAP
    private val mapViewKey = "MapViewBundleKey"
    private lateinit var mapView: MapView

    //NativeAd
    private var currentNativeAd: NativeAd? = null

    private lateinit var quake: Quake
    private lateinit var binding: ActivityQuakeDetailsBinding

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        binding = ActivityQuakeDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        with(binding.includeMapview.mapView) {
            mapView = this
            onCreate(savedInstanceState)
            getMapAsync(this@QuakeDetailsActivity)
        }

        refreshAd()

        bindingResources()

        MobileAds.openAdInspector(this) { }
    }

    @SuppressLint("MissingPermission")
    private fun refreshAd() {
        AdLoader.Builder(this, getString(R.string.ADMOB_ID_NATIVE))
            .forNativeAd { nativeAd ->

                if (isDestroyed || isFinishing || isChangingConfigurations) {
                    nativeAd.destroy()
                    return@forNativeAd
                }

                currentNativeAd?.destroy()
                currentNativeAd = nativeAd

                val adView =
                    layoutInflater.inflate(R.layout.ad_mob_small_template, null) as NativeAdView

                populateNativeAdView(nativeAd, adView)

                binding.includeCvAdmob.admobContainer.apply {
                    removeAllViews()
                    addView(adView)
                }
            }
            .withAdListener(object : AdListener() {

                override fun onAdFailedToLoad(p0: LoadAdError?) {
                    Timber.e("Failed to load native ad with error $p0")
                }

            })
            .build().loadAd(AdRequest.Builder().build())
    }

    private fun populateNativeAdView(nativeAd: NativeAd, adView: NativeAdView) {

        with(adView) {

            iconView = findViewById<ImageView>(R.id.ad_app_icon)
            headlineView = findViewById<TextView>(R.id.ad_title)
            storeView = findViewById<TextView>(R.id.ad_store)
            starRatingView = findViewById<RatingBar>(R.id.ad_rating_bar)
            bodyView = findViewById<TextView>(R.id.ad_body)
            callToActionView = findViewById(R.id.ad_call_to_action)

            //Asset guaranteed
            (headlineView as TextView).text = nativeAd.headline

            //app icon
            adView.iconView?.visibility = when (nativeAd.icon) {
                null -> View.INVISIBLE
                else -> {
                    (adView.iconView as ImageView).setImageDrawable(nativeAd.icon?.drawable)
                    View.VISIBLE
                }
            }

            //body text
            adView.bodyView?.visibility = when (nativeAd.body) {
                null -> View.INVISIBLE
                else -> {
                    (adView.bodyView as TextView).text = nativeAd.body
                    View.VISIBLE
                }
            }

            //start rating
            adView.starRatingView?.visibility = when (nativeAd.starRating) {
                null -> {
                    View.INVISIBLE
                }
                else -> {
                    nativeAd.starRating?.let {
                        (adView.starRatingView as RatingBar).rating = it.toFloat()
                    }
                    View.VISIBLE
                }
            }

            adView.callToActionView?.visibility = when (nativeAd.callToAction) {
                null -> View.INVISIBLE
                else -> {
                    (adView.callToActionView as Button).text = nativeAd.callToAction
                    View.VISIBLE
                }
            }

            //store text
            adView.storeView?.visibility = when (nativeAd.store) {
                null -> View.INVISIBLE
                else -> {
                    (adView.storeView as TextView).text = nativeAd.store
                    View.VISIBLE
                }
            }

            //End population ad
            adView.setNativeAd(nativeAd)
        }


        val vc = nativeAd.mediaContent?.videoController

        when {
            vc?.hasVideoContent() == true -> vc.videoLifecycleCallbacks =
                object : VideoController.VideoLifecycleCallbacks() {
                    override fun onVideoEnd() {
                        //refreshAd()
                        super.onVideoEnd()
                    }
                }
            else -> {
                //refreshAd()
            }
        }
    }

    private fun bindingResources() {

        setSupportActionBar(binding.includeToolbar.materialToolbar)

        //Muestra la flecha en toolbar para volver atras
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24)
        }

        handleBundles()
    }

    private fun handleBundles() {

        //Obtener datos desde intent
        quake = intent.extras?.get(getString(R.string.INTENT_QUAKE)) as Quake

        //Seteo de textview
        setTextViews()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        // Respond to the action bar's Up/Home button
        if (item.itemId == android.R.id.home) {
            Timber.d(getString(R.string.TAG_INTENT_DETALLE_HOME_UP_RESPONSE))
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
            tvEscala.setScale(quake.scale)

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

            mapType = GoogleMap.MAP_TYPE_NORMAL
            setMinZoomPreference(5.0f)
            uiSettings.isZoomGesturesEnabled = false
            uiSettings.isZoomControlsEnabled = true

            uiSettings.isTiltGesturesEnabled = false
            uiSettings.isScrollGesturesEnabled = false

            uiSettings.isMapToolbarEnabled = false
            uiSettings.isRotateGesturesEnabled = false
            uiSettings.isCompassEnabled = false



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
            Timber.d(getString(R.string.TAG_MAP_READY_RESPONSE))

            //Seteo de floating buttons
            binding.fabShare.setOnClickListener {
                Timber.d(getString(R.string.TAG_FAB_SHARE_STATUS) + ": " + getString(R.string.TAG_FAB_SHARE_STATUS_CLICKED))
                this@QuakeDetailsActivity.makeSnapshot(p0, quake)
            }
        }

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
        currentNativeAd?.destroy()
        super.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }
}
