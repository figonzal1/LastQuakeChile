package cl.figonzal.lastquakechile.core.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.widget.AppCompatRatingBar
import androidx.fragment.app.Fragment
import cl.figonzal.lastquakechile.core.utils.configOptionsMenu
import cl.figonzal.lastquakechile.databinding.FragmentAdMobBinding
import com.appodeal.ads.*
import timber.log.Timber

class AdFragment : Fragment() {

    private var nativeAdView: NativeAdView? = null

    private var _binding: FragmentAdMobBinding? = null
    private val binding get() = _binding!!

    @SuppressLint("MissingPermission")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentAdMobBinding.inflate(inflater, container, false)

        loadNativeAd()

        configOptionsMenu {}

        return binding.root
    }

    private fun loadNativeAd() {

        Appodeal.setRequiredNativeMediaAssetType(Native.MediaAssetType.ALL)
        Appodeal.setNativeAdType(Native.NativeAdType.NoVideo)
        Appodeal.setNativeCallbacks(object : NativeCallbacks {
            override fun onNativeLoaded() {
                Timber.d("Native was loaded")
                showNativeAd()
            }

            override fun onNativeFailedToLoad() {
                Timber.d("Native failed to load")

                if (isAdded) {
                    with(binding) {
                        progressBar.visibility = View.GONE
                        adIncludeOffline.root.visibility = View.VISIBLE
                    }
                }
            }

            override fun onNativeClicked(nativeAd: NativeAd?) {
                Timber.d("Native was clicked")
            }

            override fun onNativeShowFailed(nativeAd: NativeAd?) {
                Timber.d("Native failed to show")
            }

            override fun onNativeShown(nativeAd: NativeAd?) {
                Timber.d("Native was shown")
            }

            override fun onNativeExpired() {
                Timber.d("Native was expired")
            }
        })
    }

    private fun showNativeAd() {

        val adNativeAdList: MutableList<NativeAd> = Appodeal.getNativeAds(1)

        if (isAdded && adNativeAdList.isNotEmpty()) {

            val nativeAd = adNativeAdList.first()

            binding.progressBar.visibility = View.GONE
            binding.adIncludeOffline.root.visibility = View.GONE

            nativeAdView = binding.adInclude.root

            //Setters
            nativeAdView?.setNativeIconView(binding.adInclude.adAppIcon)
            nativeAdView?.titleView = binding.adInclude.adTitle
            nativeAdView?.ratingView = binding.adInclude.adRatingBar
            nativeAdView?.descriptionView = binding.adInclude.adBody
            nativeAdView?.callToActionView = binding.adInclude.adCallToAction
            nativeAdView?.nativeMediaView = binding.adInclude.adMedia


            //Complete with data
            (nativeAdView?.titleView as TextView).text = nativeAd.title

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

            (nativeAdView?.callToActionView as Button).text = nativeAd.callToAction

            nativeAdView?.unregisterViewForInteraction()
            nativeAdView?.registerView(nativeAd)
            nativeAdView?.visibility = View.VISIBLE
        }
    }

    companion object {
        fun newInstance() = AdFragment()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        nativeAdView?.destroy()
    }

    override fun onDestroy() {
        super.onDestroy()
        nativeAdView?.destroy()
    }
}


