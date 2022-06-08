package cl.figonzal.lastquakechile.core.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Build
import android.util.DisplayMetrics
import android.view.View
import android.widget.FrameLayout
import cl.figonzal.lastquakechile.R
import com.google.android.gms.ads.*
import timber.log.Timber

/*
 * BANNER SECTION
 */
@SuppressLint("MissingPermission")
fun Activity.startAds(frameLayout: FrameLayout): AdView {

    var initialLayoutComplete = false

    val adView = AdView(this)

    with(frameLayout) {
        addView(adView)
        viewTreeObserver.addOnGlobalLayoutListener {

            if (!initialLayoutComplete) {
                initialLayoutComplete = true
                adView.loadAnchored(this@startAds)
            }
        }
    }
    return adView
}

@SuppressLint("MissingPermission")
fun AdView.loadAnchored(activity: Activity) {

    //getAdSize
    setAdSize(anchoredAddSize(this, activity))

    adUnitId = activity.getString(R.string.ADMOB_ID_BANNER)

    adListener = object : AdListener() {

        override fun onAdFailedToLoad(loadAdError: LoadAdError) {
            Timber.w(activity.getString(R.string.ADMOB_AD_FAILED))
            visibility = View.GONE
        }

        override fun onAdLoaded() {
            Timber.d(activity.getString(R.string.ADMOB_AD_LOADED))
            visibility = View.VISIBLE
        }

        override fun onAdOpened() {
            Timber.d(activity.getString(R.string.ADMOB_AD_OPEN))
        }
    }
    loadAd(AdRequest.Builder().build())
}

private fun anchoredAddSize(adView: AdView, activity: Activity): AdSize {

    @Suppress("DEPRECATION")
    val display = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> activity.display
        else -> activity.windowManager.defaultDisplay
    }

    with(DisplayMetrics()) {
        @Suppress("DEPRECATION")
        display?.getMetrics(this)

        val density = this.density

        var adWidthPixels = adView.width.toFloat()
        if (adWidthPixels == 0f) {
            adWidthPixels = this.widthPixels.toFloat()
        }

        val adWidth = (adWidthPixels / density).toInt()
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, adWidth)
    }
}

