package cl.figonzal.lastquakechile.core.utils

import android.view.View
import cl.figonzal.lastquakechile.R
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import timber.log.Timber


/**
 * Funcion encargada de cargar la publicidad presente en el listado
 */
fun loadBanner(mAdView: AdView) {

    mAdView.apply {

        adListener = object : AdListener() {

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                Timber.w(context.getString(R.string.TAG_ADMOB_AD_STATUS_FAILED))
                visibility = View.GONE
                super.onAdFailedToLoad(loadAdError)
            }

            override fun onAdLoaded() {
                Timber.i(context.getString(R.string.TAG_ADMOB_AD_STATUS_LOADED))
                visibility = View.VISIBLE
                super.onAdLoaded()
            }

            override fun onAdOpened() {
                Timber.i(context.getString(R.string.TAG_ADMOB_AD_STATUS_OPEN))
                super.onAdOpened()
            }
        }
        loadAd(AdRequest.Builder().build())
    }
}
