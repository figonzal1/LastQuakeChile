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
import cl.figonzal.lastquakechile.core.services.AppoDealService
import cl.figonzal.lastquakechile.core.utils.configOptionsMenu
import cl.figonzal.lastquakechile.databinding.FragmentAdMobBinding
import com.appodeal.ads.NativeAdView

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

        setUpNativeAds()

        configOptionsMenu {}

        return binding.root
    }

    private fun setUpNativeAds() {
        AppoDealService.showNativeAds(requireActivity(), { nativeAd ->

            binding.progressBar.visibility = View.GONE

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

        }) {
            with(binding) {
                progressBar.visibility = View.GONE
                includeErrorMessage.root.visibility = View.VISIBLE
                includeErrorMessage.btnRetry.visibility = View.GONE
            }
        }
    }

    companion object {
        fun newInstance() = AdFragment()
    }

    override fun onDestroy() {
        super.onDestroy()
        nativeAdView?.destroy()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        nativeAdView?.destroy()
    }
}


