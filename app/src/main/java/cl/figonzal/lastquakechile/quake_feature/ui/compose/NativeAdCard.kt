package cl.figonzal.lastquakechile.quake_feature.ui.compose

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import cl.figonzal.lastquakechile.R
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.VideoController
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * Loads and displays an AdMob native ad using [AndroidView] interop.
 * The ad is invisible until it loads successfully.
 * [NativeAd.destroy] is called automatically in [DisposableEffect.onDispose].
 */
@SuppressLint("MissingPermission")
@Composable
fun NativeAdCard(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var nativeAd by remember { mutableStateOf<NativeAd?>(null) }

    DisposableEffect(Unit) {
        val scope = CoroutineScope(Dispatchers.Main)
        scope.launch {
            withContext(Dispatchers.IO) { MobileAds.initialize(context) }
            AdLoader.Builder(context, context.getString(R.string.ADMOB_ID_NATIVE_DETAILS))
                .forNativeAd { ad ->
                    nativeAd?.destroy()
                    nativeAd = ad
                }
                .withAdListener(object : AdListener() {
                    override fun onAdFailedToLoad(error: LoadAdError) {
                        Timber.e("Native ad failed to load: $error")
                    }
                })
                .build()
                .loadAd(AdRequest.Builder().build())
        }
        onDispose {
            nativeAd?.destroy()
            scope.cancel()
        }
    }

    val ad = nativeAd
    if (ad != null) {
        // cv_native_ad in activity_quake_details.xml was MaterialCardView — wrap to replicate elevation.
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            AndroidView(
                factory = { ctx ->
                    (LayoutInflater.from(ctx)
                        .inflate(R.layout.ad_small_template, null, false) as NativeAdView)
                        .also { populateNativeAdView(ad, it) }
                },
                update = { adView -> populateNativeAdView(ad, adView) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

private fun populateNativeAdView(nativeAd: NativeAd, adView: NativeAdView) {
    with(adView) {
        iconView = findViewById<ImageView>(R.id.ad_app_icon)
        headlineView = findViewById<TextView>(R.id.ad_title)
        starRatingView = findViewById<RatingBar>(R.id.ad_rating_bar)
        bodyView = findViewById<TextView>(R.id.ad_body)

        (headlineView as TextView).text = nativeAd.headline

        iconView?.let { iv ->
            iv.visibility = when (nativeAd.icon) {
                null -> android.view.View.INVISIBLE
                else -> {
                    (iv as ImageView).setImageDrawable(nativeAd.icon?.drawable)
                    android.view.View.VISIBLE
                }
            }
        }

        bodyView?.let { bv ->
            bv.visibility = when (nativeAd.body) {
                null -> android.view.View.INVISIBLE
                else -> {
                    (bv as TextView).text = nativeAd.body
                    android.view.View.VISIBLE
                }
            }
        }

        starRatingView?.let { rv ->
            rv.visibility = when (nativeAd.starRating) {
                null -> android.view.View.INVISIBLE
                else -> {
                    (rv as RatingBar).rating = nativeAd.starRating!!.toFloat()
                    android.view.View.VISIBLE
                }
            }
        }

        setNativeAd(nativeAd)
    }

    val vc = nativeAd.mediaContent?.videoController
    if (vc?.hasVideoContent() == true) {
        vc.videoLifecycleCallbacks = object : VideoController.VideoLifecycleCallbacks() {}
    }
}
