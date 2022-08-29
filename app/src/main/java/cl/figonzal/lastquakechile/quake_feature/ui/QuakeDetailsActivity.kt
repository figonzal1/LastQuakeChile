package cl.figonzal.lastquakechile.quake_feature.ui

import android.animation.IntEvaluator
import android.animation.ValueAnimator
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatRatingBar
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import cl.figonzal.lastquakechile.R
import cl.figonzal.lastquakechile.core.services.AppoDealService
import cl.figonzal.lastquakechile.core.ui.dialog.MapTerrainDialogFragment
import cl.figonzal.lastquakechile.core.utils.*
import cl.figonzal.lastquakechile.databinding.ActivityQuakeDetailsBinding
import cl.figonzal.lastquakechile.quake_feature.domain.model.Quake
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.ktx.addCircle
import timber.log.Timber
import java.time.format.DateTimeFormatter

private const val mapViewKey = "MapViewBundleKey"

class QuakeDetailsActivity : AppCompatActivity(), OnMapReadyCallback {

    private var circleAnimator2: ValueAnimator? = null
    private var circleAnimator: ValueAnimator? = null

    private var nativeAdView: com.appodeal.ads.NativeAdView? = null

    private var googleMap: GoogleMap? = null

    private var quake: Quake? = null

    private lateinit var binding: ActivityQuakeDetailsBinding

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        binding = ActivityQuakeDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        with(binding.includeMapview.mapView) {
            onCreate(savedInstanceState)
            getMapAsync(this@QuakeDetailsActivity)
        }

        AppoDealService.showNativeAds(this, { nativeAd ->

            nativeAdView = binding.admobTemplate.root

            //Setters
            nativeAdView?.setNativeIconView(binding.admobTemplate.adAppIcon)
            nativeAdView?.titleView = binding.admobTemplate.adTitle
            nativeAdView?.providerView = binding.admobTemplate.adStore
            nativeAdView?.ratingView = binding.admobTemplate.adRatingBar
            nativeAdView?.descriptionView = binding.admobTemplate.adBody
            nativeAdView?.callToActionView = binding.admobTemplate.adCallToAction

            //Complete with data
            (nativeAdView?.titleView as TextView).text = nativeAd.title

            nativeAdView?.providerView?.visibility = when (nativeAd.adProvider) {
                null -> View.INVISIBLE
                else -> {
                    (nativeAdView?.providerView as TextView).text = nativeAd.adProvider
                    View.VISIBLE
                }
            }

            nativeAdView?.ratingView?.visibility = when (nativeAd.rating) {
                0f -> View.INVISIBLE
                else -> {
                    (nativeAdView?.ratingView as AppCompatRatingBar).rating = nativeAd.rating
                    View.VISIBLE
                }
            }

            nativeAdView?.descriptionView?.visibility = when (nativeAd.description) {
                null -> View.INVISIBLE
                else -> {
                    (nativeAdView?.descriptionView as TextView).text = nativeAd.description
                    View.VISIBLE
                }
            }

            nativeAdView?.callToActionView?.visibility = when (nativeAd.callToAction) {
                null -> View.INVISIBLE
                else -> {
                    (nativeAdView?.callToActionView as Button).text = nativeAd.callToAction
                    View.VISIBLE
                }
            }

            nativeAdView?.registerView(nativeAd)
            nativeAdView?.visibility = View.VISIBLE

        }, {})

        bindingResources()
    }

    private fun bindingResources() {

        setSupportActionBar(binding.includeToolbar.materialToolbar)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_round_arrow_back_24)
        }

        quake = intent.extras?.get(getString(R.string.INTENT_QUAKE)) as Quake

        setTextViews()
    }

    private fun setTextViews() {

        quake?.let {

            supportActionBar?.title = it.city

            with(binding.includeCvQuakeDetail) {

                tvCity.text = it.city

                tvReference.text = it.reference

                tvMagnitude.text =
                    String.format(getString(R.string.magnitud), it.magnitude)

                ivMagColor.setColorFilter(getColor(getMagnitudeColor(it.magnitude, false)))

                tvEpicentro.text =
                    String.format(getString(R.string.quake_details_profundidad), it.depth)

                tvFecha.text =
                    it.localDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

                tvGms.formatDMS(it.coordinate)

                tvHour.timeToText(it, true)

                tvEscala.setScale(it.scale)

                ivSensitive.visibility = when {
                    it.isSensitive -> View.VISIBLE
                    else -> View.GONE
                }
            }

            binding.ivEstado.setStatusImage(it.isVerified, binding.tvEstado)
        }
    }

    override fun onMapReady(p0: GoogleMap) {

        googleMap = p0

        quake?.let {

            val quakeMagColor = getColor(getMagnitudeColor(it.magnitude, true))
            val greyAlpha = getColor(R.color.grey_dark_alpha)

            val latLong = LatLng(it.coordinate.latitude, it.coordinate.longitude)

            p0.apply {

                configOptionsMenu()

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
                    circleAnimator2 = ValueAnimator.ofInt(0, 90000).apply {
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
                }

                addCircle {
                    center(latLong)
                    radius(90000.0)
                    strokeWidth(1f)
                    strokeColor(greyAlpha)
                }.animate {
                    circleAnimator = ValueAnimator.ofInt(0, 90000).apply {
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
                }

                moveCamera(CameraUpdateFactory.newLatLngZoom(latLong, 6.0f))

                //Log zone
                Timber.d(getString(R.string.MAP_READY_RESPONSE))

                //Seteo de floating buttons
                binding.fabShare.setOnClickListener { _ ->
                    Timber.d(getString(R.string.FAB_SHARE) + ": " + getString(R.string.CLICKED))
                    this@QuakeDetailsActivity.makeSnapshot(p0, it)
                }
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
        binding.includeMapview.mapView.onSaveInstanceState(mMapViewBundle)
    }

    override fun onResume() {
        super.onResume()
        binding.includeMapview.mapView.onResume()
    }

    override fun onStart() {
        super.onStart()
        binding.includeMapview.mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        binding.includeMapview.mapView.onStop()
    }

    override fun onPause() {
        binding.includeMapview.mapView.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        binding.includeMapview.mapView.onDestroy()
        nativeAdView?.destroy()

        circleAnimator?.cancel()
        circleAnimator2?.cancel()

        super.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.includeMapview.mapView.onLowMemory()
    }

    private fun configOptionsMenu() {

        addMenuProvider(object : MenuProvider {

            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_quake_details, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when (menuItem.itemId) {
                    android.R.id.home -> {
                        Timber.d(getString(R.string.INTENT_DETAIL_HOME_UP))
                        finish()
                    }
                    R.id.layers_menu -> {

                        googleMap?.let {

                            MapTerrainDialogFragment(it).show(
                                supportFragmentManager,
                                "Dialogo mapType"
                            )
                        }

                    }
                }
                return true
            }
        }, this, Lifecycle.State.RESUMED)
    }
}
