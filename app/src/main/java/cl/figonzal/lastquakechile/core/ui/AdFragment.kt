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

class AdFragment : Fragment() {

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

        configOptionsMenu {}

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
                        R.layout.ad_fragment_template,
                        container
                    ) as NativeAdView

                    populateNativeAdView(nativeAd, adView)

                    binding.adInclude.root.apply {
                        removeAllViews()
                        addView(adView)
                    }
                }
            }
            .withAdListener(object : AdListener() {

                override fun onAdLoaded() {
                    binding.progressBar.visibility = View.GONE
                    binding.adIncludeOffline.root.visibility = View.GONE
                    binding.adInclude.root.visibility = View.VISIBLE

                    Timber.d("Native loaded successfully")
                }

                override fun onAdFailedToLoad(p0: LoadAdError) {
                    if (isAdded) {
                        with(binding) {
                            progressBar.visibility = View.GONE
                            adIncludeOffline.root.visibility = View.VISIBLE
                        }
                    }
                    Timber.e("Native failed to load $p0")
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
        fun newInstance() = AdFragment()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        currentNativeAd?.destroy()
    }

    override fun onDestroy() {
        super.onDestroy()
        currentNativeAd?.destroy()
    }
}


