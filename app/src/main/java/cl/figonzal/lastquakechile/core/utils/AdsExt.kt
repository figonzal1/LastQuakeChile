package cl.figonzal.lastquakechile.core.utils

import android.app.Activity
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import cl.figonzal.lastquakechile.R
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean

private var isMobileAdsInitializeCalled = AtomicBoolean(false)

/**
 * Fills a [NativeAdView] from [nativeAd]. Shared by the fragment (full ad, with media +
 * call-to-action) and the details activity (small ad, without them) - `findViewById` returns
 * null for an id the smaller layout doesn't have, so those two optional views stay null and
 * their `?.visibility =` assignments are simply skipped.
 */
fun NativeAdView.populate(nativeAd: NativeAd) {
    iconView = findViewById<ImageView>(R.id.ad_app_icon)
    headlineView = findViewById<TextView>(R.id.ad_title)
    starRatingView = findViewById<RatingBar>(R.id.ad_rating_bar)
    bodyView = findViewById<TextView>(R.id.ad_body)
    mediaView = findViewById(R.id.ad_media)
    callToActionView = findViewById(R.id.ad_call_to_action)

    //Asset guaranteed
    (headlineView as TextView).text = nativeAd.headline
    nativeAd.mediaContent?.let { mediaView?.setMediaContent(it) }

    //app icon
    iconView?.visibility = when (nativeAd.icon) {
        null -> View.INVISIBLE
        else -> {
            (iconView as ImageView).setImageDrawable(nativeAd.icon?.drawable)
            View.VISIBLE
        }
    }

    //body text
    bodyView?.visibility = when (nativeAd.body) {
        null -> View.INVISIBLE
        else -> {
            (bodyView as TextView).text = nativeAd.body
            View.VISIBLE
        }
    }

    //start rating
    starRatingView?.visibility = when (nativeAd.starRating) {
        null -> View.INVISIBLE
        else -> {
            nativeAd.starRating?.let {
                (starRatingView as RatingBar).rating = it.toFloat()
            }
            View.VISIBLE
        }
    }

    //call to action
    callToActionView?.visibility = when (nativeAd.callToAction) {
        null -> View.INVISIBLE
        else -> {
            (callToActionView as Button).text = nativeAd.callToAction
            View.VISIBLE
        }
    }

    //End population ad
    setNativeAd(nativeAd)
}


fun Activity.checkEULAConsentAds(initAdsCallback: () -> Unit) {

    val params = ConsentRequestParameters
        .Builder()
        .setTagForUnderAgeOfConsent(false)
        .build()

    val consentInformation = UserMessagingPlatform.getConsentInformation(this@checkEULAConsentAds)
    consentInformation.requestConsentInfoUpdate(
        this@checkEULAConsentAds,
        params,
        {
            UserMessagingPlatform.loadAndShowConsentFormIfRequired(
                this@checkEULAConsentAds
            ) { loadAndShowError ->

                when {
                    loadAndShowError != null -> {

                        // Consent gathering failed.
                        Timber.w(
                            String.format(
                                "%s: %s",
                                loadAndShowError.errorCode,
                                loadAndShowError.message
                            )
                        )
                    }

                    else -> {
                        // Consent has been gathered.
                        if (consentInformation.canRequestAds()) {
                            initializeMobileAdsSdk(initAdsCallback)
                        }
                    }
                }
            }
        },
        { requestConsentError ->
            // Consent gathering failed.
            Timber.w(
                String.format(
                    "%s: %s",
                    requestConsentError.errorCode,
                    requestConsentError.message
                )
            )
        })

    if (consentInformation.canRequestAds()) {
        initializeMobileAdsSdk(initAdsCallback)
    }
}

private fun initializeMobileAdsSdk(initAdsCallback: () -> Unit) {
    if (isMobileAdsInitializeCalled.getAndSet(true)) {
        return
    }

    initAdsCallback()
}