package cl.figonzal.lastquakechile.core.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import cl.figonzal.lastquakechile.R
import cl.figonzal.lastquakechile.core.utils.populate
import cl.figonzal.lastquakechile.core.utils.views.configOptionsMenu
import cl.figonzal.lastquakechile.databinding.FragmentAdMobBinding
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.VideoOptions
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

                    adView.populate(nativeAd)

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


