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

@SuppressLint("MissingPermission")
fun AdView.load(activity: Activity) {

    adSize = adSize(this, activity)

    adUnitId = activity.getString(R.string.ADMOB_ID_BANNER)

    adListener = object : AdListener() {

        override fun onAdFailedToLoad(loadAdError: LoadAdError) {
            Timber.w(activity.getString(R.string.TAG_ADMOB_AD_STATUS_FAILED))
            visibility = View.GONE
        }

        override fun onAdLoaded() {
            Timber.i(activity.getString(R.string.TAG_ADMOB_AD_STATUS_LOADED))
            visibility = View.VISIBLE
        }

        override fun onAdOpened() {
            Timber.i(activity.getString(R.string.TAG_ADMOB_AD_STATUS_OPEN))
        }
    }
    loadAd(AdRequest.Builder().build())
}


@SuppressLint("MissingPermission")
fun Activity.startAds(container: FrameLayout): AdView {

    MobileAds.initialize(this)

    var initialLayoutComplete = false

    val adView = AdView(this)
    container.addView(adView)
    container.viewTreeObserver.addOnGlobalLayoutListener {

        if (!initialLayoutComplete) {
            initialLayoutComplete = true
            adView.load(this)
        }
    }

    return adView
}

private fun adSize(adView: AdView, activity: Activity): AdSize {

    val display = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        activity.display
    } else {
        activity.windowManager.defaultDisplay
    }
    val outMetrics = DisplayMetrics()
    display?.getMetrics(outMetrics)

    val density = outMetrics.density

    var adWidthPixels = adView.width.toFloat()
    if (adWidthPixels == 0f) {
        adWidthPixels = outMetrics.widthPixels.toFloat()
    }

    val adWidth = (adWidthPixels / density).toInt()
    return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, adWidth)
}