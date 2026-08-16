package cl.figonzal.lastquakechile.quake_feature.ui

import android.animation.IntEvaluator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.NotificationManager
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.BundleCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.MenuProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import cl.figonzal.lastquakechile.R
import cl.figonzal.lastquakechile.core.services.notifications.utils.IS_SNAPSHOT_REQUEST_FROM_BOTTOM_SHEET
import cl.figonzal.lastquakechile.core.services.notifications.utils.QUAKE
import cl.figonzal.lastquakechile.core.ui.dialog.MapTerrainDialogFragment
import cl.figonzal.lastquakechile.core.utils.cacheImageUri
import cl.figonzal.lastquakechile.core.utils.clearShareImageCache
import cl.figonzal.lastquakechile.core.utils.configMapType
import cl.figonzal.lastquakechile.core.utils.populate
import cl.figonzal.lastquakechile.core.utils.setNightMode
import cl.figonzal.lastquakechile.core.utils.views.QUAKE_DETAILS_DEPTH_FORMAT
import cl.figonzal.lastquakechile.core.utils.views.QUAKE_DETAILS_MAGNITUDE_FORMAT
import cl.figonzal.lastquakechile.core.utils.views.formatDMS
import cl.figonzal.lastquakechile.core.utils.views.getMagnitudeColor
import cl.figonzal.lastquakechile.core.utils.views.setDebouncedClickListener
import cl.figonzal.lastquakechile.core.utils.views.setScale
import cl.figonzal.lastquakechile.core.utils.views.timeToText
import cl.figonzal.lastquakechile.core.utils.views.toast
import cl.figonzal.lastquakechile.databinding.ActivityQuakeDetailsBinding
import cl.figonzal.lastquakechile.quake_feature.domain.model.Quake
import cl.figonzal.lastquakechile.quake_feature.ui.share.QuakeStoryRenderer
import cl.figonzal.lastquakechile.quake_feature.ui.share.ShareQuakeBottomSheet
import cl.figonzal.lastquakechile.quake_feature.ui.share.StickerDesign
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.ktx.addCircle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.util.Locale

private const val mapViewKey = "MapViewBundleKey"
private const val PULSE_CIRCLE_FROZEN_RADIUS = 90000.0

/**
 * Adds a pulsing radar circle centered on [latLng] and returns it along with the animator driving
 * it, so the caller can pause/resume/cancel the animation and freeze the radius before taking a
 * map snapshot. Two of these overlap with a [startDelay] offset to create the staggered pulse
 * effect.
 */
private fun GoogleMap.addPulseCircle(
    latLng: LatLng,
    color: Int,
    startDelay: Long
): Pair<Circle, ValueAnimator> {
    val circle = addCircle {
        center(latLng)
        radius(90000.0)
        strokeWidth(1f)
        strokeColor(color)
    }

    val animator = ValueAnimator.ofInt(0, 90000).apply {
        repeatMode = ValueAnimator.RESTART
        repeatCount = ValueAnimator.INFINITE
        duration = 4000
        this.startDelay = startDelay
        setEvaluator(IntEvaluator())
        interpolator = AccelerateDecelerateInterpolator()
        addUpdateListener { animator ->
            circle.radius = (animator.animatedFraction * 140000).toDouble()
        }
        start()
    }

    return circle to animator
}

class QuakeDetailsActivity : AppCompatActivity(), OnMapReadyCallback {

    private var circleAnimator2: ValueAnimator? = null
    private var circleAnimator: ValueAnimator? = null
    private var pulseCircleA: Circle? = null
    private var pulseCircleB: Circle? = null
    private var currentNativeAd: NativeAd? = null

    private var googleMap: GoogleMap? = null

    private var quake: Quake? = null
    private var isSnapshotRequest: Boolean? = null

    private val quakeStoryRenderer: QuakeStoryRenderer by inject()

    private lateinit var binding: ActivityQuakeDetailsBinding

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        binding = ActivityQuakeDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(view.paddingLeft, view.paddingTop, view.paddingRight, systemBars.bottom)
            insets
        }

        with(binding.includeMapview.mapView) {
            onCreate(savedInstanceState)
            getMapAsync(this@QuakeDetailsActivity)
        }

        refreshAd()

        bindingResources()
    }

    private fun bindingResources() {

        setSupportActionBar(binding.includeToolbar.materialToolbar)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.round_arrow_back_24)
        }

        @Suppress("DEPRECATION")
        with(intent.extras) {

            quake = when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                    this?.let { BundleCompat.getParcelable(it, QUAKE, Quake::class.java) }
                }

                else -> this?.get(QUAKE) as Quake
            }
            isSnapshotRequest =
                this?.getBoolean(IS_SNAPSHOT_REQUEST_FROM_BOTTOM_SHEET, false) ?: false
        }

        quake?.let {
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).cancel(it.quakeCode)
        }

        setTextViews()
    }

    private fun refreshAd() {
        lifecycleScope.launch(Dispatchers.IO) {
            MobileAds.initialize(this@QuakeDetailsActivity)
            withContext(Dispatchers.Main) {
                loadNativeAd()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun loadNativeAd() {
        AdLoader.Builder(this, getString(R.string.ADMOB_ID_NATIVE_DETAILS))
            .forNativeAd { nativeAd ->

                if (isDestroyed || isFinishing || isChangingConfigurations) {
                    nativeAd.destroy()
                    return@forNativeAd
                }

                currentNativeAd?.destroy()
                currentNativeAd = nativeAd

                val adView =
                    layoutInflater.inflate(
                        R.layout.ad_small_template,
                        binding.root,
                        false
                    ) as NativeAdView

                adView.populate(nativeAd)

                binding.admobTemplate.root.apply {
                    removeAllViews()
                    addView(adView)
                }
            }
            .withAdListener(object : AdListener() {

                override fun onAdLoaded() {
                    hideAdBanner(false)
                    Timber.d("Native loaded successfully")
                }

                override fun onAdFailedToLoad(p0: LoadAdError) {
                    hideAdBanner(true)
                    Timber.e("Native failed to load $p0")
                }

            })
            .build().loadAd(AdRequest.Builder().build())
    }

    private fun setTextViews() {

        quake?.let {

            supportActionBar?.title = it.city

            with(binding.includeCvQuakeDetail) {

                tvCity.text = it.city

                tvReference.text = it.reference

                tvMagnitude.text =
                    String.format(Locale.getDefault(), QUAKE_DETAILS_MAGNITUDE_FORMAT, it.magnitude)

                ivMagColor.setColorFilter(getColor(getMagnitudeColor(it.magnitude, false)))

                tvDepthValue.text =
                    String.format(Locale.getDefault(), QUAKE_DETAILS_DEPTH_FORMAT, it.depth)

                tvDatetimeValue.text = it.localDate

                tvGmsValue.formatDMS(it.coordinate)

                tvHour.timeToText(it, true)

                tvScaleValue.setScale(it.scale)

                ivSensitive.visibility = when {
                    it.isSensitive -> View.VISIBLE
                    else -> View.GONE
                }

                //Verified status
                ivVerified.visibility = when {
                    it.isVerified -> View.VISIBLE
                    else -> View.GONE
                }

                ivVerified.setOnClickListener {
                    toast(R.string.quake_verified_toast)
                }
            }
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

                mapType = configMapType()

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

                val (circleA, animatorA) = addPulseCircle(latLong, greyAlpha, startDelay = 0)
                pulseCircleA = circleA
                circleAnimator2 = animatorA

                val (circleB, animatorB) = addPulseCircle(latLong, greyAlpha, startDelay = 1000)
                pulseCircleB = circleB
                circleAnimator = animatorB

                moveCamera(CameraUpdateFactory.newLatLngZoom(latLong, 6.0f))

                //Log zone
                Timber.d("Map ready")

                //Seteo de floating buttons
                binding.fabShare.setDebouncedClickListener { _ ->
                    Timber.d("Share button clicked")
                    shareQuake(it)
                }

                if (isSnapshotRequest == true) {
                    Timber.d("Snapshot request from bottomSheetDialog")

                    lifecycleScope.launch {
                        delay(1000)
                        shareQuake(it)
                    }
                }
            }
        }

    }

    /**
     * Freezes the pulsing circle radii before taking the map snapshot so the share image is
     * deterministic, then resumes the animation. Skips the snapshot entirely if the activity
     * isn't at least STARTED - this is the fix for the #83 crash
     * (`IllegalStateException: Can't take a snapshot while executing in the background`),
     * which happened because `GoogleMap.snapshot()` was called after the map's GL surface
     * was paused.
     */
    private fun captureMapSnapshot(onReady: (Bitmap?) -> Unit) {
        val map = googleMap

        if (map == null || !lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            onReady(null)
            return
        }

        circleAnimator?.pause()
        circleAnimator2?.pause()
        pulseCircleA?.radius = PULSE_CIRCLE_FROZEN_RADIUS
        pulseCircleB?.radius = PULSE_CIRCLE_FROZEN_RADIUS

        map.snapshot { bitmap ->
            circleAnimator?.resume()
            circleAnimator2?.resume()
            onReady(bitmap)
        }
    }

    /**
     * Renders all [StickerDesign] variants (map snapshot may be null if it couldn't be
     * captured) and opens [ShareQuakeBottomSheet] with them. Rendering happens off the main
     * thread: the sticker views are never attached to a window, so it's safe to
     * measure/layout/draw them from any thread, and PNG compression to the cache dir is I/O.
     * Designs are rendered sequentially and each bitmap is recycled right after it's cached -
     * doing all three at once would keep ~18 MB of bitmaps alive simultaneously.
     */
    private fun shareQuake(quake: Quake) {
        captureMapSnapshot { mapSnapshot ->
            lifecycleScope.launch(Dispatchers.Default) {
                clearShareImageCache()

                val stickerUris = StickerDesign.entries.map { design ->
                    val sticker = quakeStoryRenderer.renderSticker(quake, mapSnapshot, design)
                    cacheImageUri(sticker, "sticker-${quake.quakeCode}-${design.name}", Bitmap.CompressFormat.PNG)
                        .also { sticker.recycle() }
                }
                val magnitudeColor = quakeStoryRenderer.magnitudeColor(quake)

                withContext(Dispatchers.Main) {
                    if (supportFragmentManager.isStateSaved) return@withContext

                    ShareQuakeBottomSheet.newInstance(quake, stickerUris, magnitudeColor)
                        .show(supportFragmentManager, ShareQuakeBottomSheet.TAG)
                }
            }
        }
    }

    private fun hideAdBanner(hide: Boolean) {
        binding.cvNativeAd.isVisible = !hide
        binding.admobTemplate.root.isVisible = !hide
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
        super.onPause()
        binding.includeMapview.mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.includeMapview.mapView.onDestroy()
        currentNativeAd?.destroy()

        circleAnimator?.cancel()
        circleAnimator2?.cancel()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.includeMapview.mapView.onLowMemory()
    }

    private fun configOptionsMenu() {

        supportFragmentManager.setFragmentResultListener(
            MapTerrainDialogFragment.REQUEST_KEY, this
        ) { _, bundle ->
            googleMap?.mapType = bundle.getInt(MapTerrainDialogFragment.RESULT_MAP_TYPE)
        }

        addMenuProvider(object : MenuProvider {

            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_quake_details, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when (menuItem.itemId) {
                    android.R.id.home -> {
                        Timber.d("Home up clicked")
                        finish()
                    }

                    R.id.layers_menu -> {
                        MapTerrainDialogFragment.newInstance().show(
                            supportFragmentManager,
                            "map_terrain"
                        )
                    }
                }
                return true
            }
        }, this, Lifecycle.State.RESUMED)
    }
}
