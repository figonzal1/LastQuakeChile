package cl.figonzal.lastquakechile.core.ui.compose

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import cl.figonzal.lastquakechile.R
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.VideoController
import com.google.android.gms.ads.VideoOptions
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import timber.log.Timber

/**
 * Compose replacement for the old `AdFragment` (fragment_ad_mob.xml).
 * Loads a large AdMob native ad (ad_fragment_template.xml) via [AndroidView] interop.
 * While loading shows a centered spinner; on failure shows the offline placeholder.
 * The [NativeAd] is destroyed automatically in [DisposableEffect.onDispose].
 */
@SuppressLint("MissingPermission")
@Composable
fun AdScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var adState by remember { mutableStateOf<AdState>(AdState.Loading) }

    DisposableEffect(Unit) {
        var current: NativeAd? = null
        AdLoader.Builder(context, context.getString(R.string.ADMOB_ID_NATIVE_FRAGMENT))
            .forNativeAd { nativeAd ->
                current?.destroy()
                current = nativeAd
                adState = AdState.Loaded(nativeAd)
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    adState = AdState.Offline
                    Timber.e("Native failed to load $error")
                }
            })
            .withNativeAdOptions(
                NativeAdOptions.Builder()
                    .setVideoOptions(VideoOptions.Builder().setStartMuted(false).build())
                    .build()
            )
            .build()
            .loadAd(AdRequest.Builder().build())

        onDispose { current?.destroy() }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(bottom = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        when (val state = adState) {
            AdState.Loading -> CircularProgressIndicator()
            AdState.Offline -> OfflineAdContent()
            is AdState.Loaded -> NativeAdContent(state.ad)
        }
    }
}

private sealed interface AdState {
    data object Loading : AdState
    data object Offline : AdState
    data class Loaded(val ad: NativeAd) : AdState
}

@Composable
private fun NativeAdContent(nativeAd: NativeAd) {
    AndroidView(
        factory = { ctx ->
            (LayoutInflater.from(ctx)
                .inflate(R.layout.ad_fragment_template, null, false) as NativeAdView)
                .also { populateNativeAdView(nativeAd, it) }
        },
        update = { adView -> populateNativeAdView(nativeAd, adView) },
        modifier = Modifier.fillMaxSize()
    )
}

/** Compose port of ad_fragment_offline.xml — icon + title + description. */
@Composable
private fun OfflineAdContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.quakes_24dp),
            contentDescription = stringResource(R.string.cd_ad_app_icon),
            tint = Color.Unspecified,
            modifier = Modifier.size(70.dp)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.ad_title_offline),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.ad_description_offline),
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center
        )
    }
}

private fun populateNativeAdView(nativeAd: NativeAd, adView: NativeAdView) {
    with(adView) {
        iconView = findViewById<ImageView>(R.id.ad_app_icon)
        headlineView = findViewById<TextView>(R.id.ad_title)
        starRatingView = findViewById<RatingBar>(R.id.ad_rating_bar)
        mediaView = findViewById(R.id.ad_media)
        bodyView = findViewById<TextView>(R.id.ad_body)
        callToActionView = findViewById(R.id.ad_call_to_action)

        // Asset guaranteed
        (headlineView as TextView).text = nativeAd.headline
        nativeAd.mediaContent?.let { mediaView?.setMediaContent(it) }

        iconView?.visibility = when (nativeAd.icon) {
            null -> android.view.View.INVISIBLE
            else -> {
                (iconView as ImageView).setImageDrawable(nativeAd.icon?.drawable)
                android.view.View.VISIBLE
            }
        }

        bodyView?.visibility = when (nativeAd.body) {
            null -> android.view.View.INVISIBLE
            else -> {
                (bodyView as TextView).text = nativeAd.body
                android.view.View.VISIBLE
            }
        }

        starRatingView?.visibility = when (nativeAd.starRating) {
            null -> android.view.View.INVISIBLE
            else -> {
                (starRatingView as RatingBar).rating = nativeAd.starRating!!.toFloat()
                android.view.View.VISIBLE
            }
        }

        callToActionView?.visibility = when (nativeAd.callToAction) {
            null -> android.view.View.INVISIBLE
            else -> {
                (callToActionView as Button).text = nativeAd.callToAction
                android.view.View.VISIBLE
            }
        }

        setNativeAd(nativeAd)
    }

    val vc = nativeAd.mediaContent?.videoController
    if (vc?.hasVideoContent() == true) {
        vc.videoLifecycleCallbacks = object : VideoController.VideoLifecycleCallbacks() {}
    }
}
