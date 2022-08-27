package cl.figonzal.lastquakechile.core.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import cl.figonzal.lastquakechile.R
import cl.figonzal.lastquakechile.core.utils.configOptionsMenu
import cl.figonzal.lastquakechile.databinding.FragmentAdMobBinding
import com.google.android.gms.ads.*
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import timber.log.Timber

class AdMobFragment : Fragment() {

    private var currentNativeAd: NativeAd? = null

    private var _binding: FragmentAdMobBinding? = null
    private val binding get() = _binding!!

    @SuppressLint("MissingPermission")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentAdMobBinding.inflate(inflater, container, false)

        refreshAd(container)

        configOptionsMenu() {}

        return binding.root
    }

    @SuppressLint("MissingPermission")
    private fun refreshAd(container: ViewGroup?) {
        AdLoader.Builder(requireContext(), getString(R.string.ADMOB_ID_NATIVE_FRAGMENT))
            .forNativeAd { nativeAd ->

                if (isDetached || isRemoving) {
                    nativeAd.destroy()
                    return@forNativeAd
                }

                currentNativeAd?.destroy()
                currentNativeAd = nativeAd

                if (isAdded) {
                    val adView = layoutInflater.inflate(
                        R.layout.ad_mob_fragment_template,
                        container
                    ) as NativeAdView

                    populateNativeAdView(nativeAd, adView)

                    binding.admobContainer.apply {
                        removeAllViews()
                        addView(adView)
                    }
                }
            }
            .withAdListener(object : AdListener() {

                override fun onAdLoaded() {
                    binding.progressBar.visibility = View.GONE
                }

                override fun onAdFailedToLoad(p0: LoadAdError) {
                    binding.progressBar.visibility = View.GONE
                    binding.includeNoWifi.root.visibility = View.VISIBLE
                    binding.includeNoWifi.btnRetry.visibility = View.GONE
                    Timber.e("Failed to load native ad with error $p0")
                }

            })
            .withNativeAdOptions(
                NativeAdOptions.Builder()
                    .setVideoOptions(
                        VideoOptions.Builder().setStartMuted(false).build()
                    )
                    .build()
            )
            .build().loadAd(AdRequest.Builder().build())
    }

    private fun populateNativeAdView(nativeAd: NativeAd, adView: NativeAdView) {

        with(adView) {
            iconView = findViewById<ImageView>(R.id.ad_app_icon)
            headlineView = findViewById<TextView>(R.id.ad_title)
            starRatingView = findViewById<RatingBar>(R.id.ad_rating_bar)
            mediaView = findViewById(R.id.ad_media)
            bodyView = findViewById<TextView>(R.id.ad_body)
            callToActionView = findViewById(R.id.ad_call_to_action)

            //Asset guaranteed
            (headlineView as TextView).text = nativeAd.headline
            nativeAd.mediaContent?.let { mediaView?.setMediaContent(it) }

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

            //End population ad
            adView.setNativeAd(nativeAd)
        }


        val vc = nativeAd.mediaContent?.videoController

        when {
            vc?.hasVideoContent() == true -> vc.videoLifecycleCallbacks =
                object : VideoController.VideoLifecycleCallbacks() {
                }
            else -> {
                //refreshAd()
            }
        }
    }

    companion object {
        fun newInstance() = AdMobFragment()
    }

    override fun onDestroy() {
        currentNativeAd?.destroy()
        super.onDestroy()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


