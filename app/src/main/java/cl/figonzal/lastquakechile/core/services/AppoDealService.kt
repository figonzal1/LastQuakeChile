package cl.figonzal.lastquakechile.core.services

import android.app.Activity
import cl.figonzal.lastquakechile.BuildConfig
import com.appodeal.ads.*
import com.appodeal.ads.initializing.ApdInitializationCallback
import com.appodeal.ads.initializing.ApdInitializationError
import com.appodeal.ads.utils.Log
import timber.log.Timber

object AppoDealService {

    fun setUpSdk(activity: Activity) {
        Appodeal.setLogLevel(Log.LogLevel.verbose)
        Appodeal.setTesting(true)
        Appodeal.initialize(
            activity,
            BuildConfig.APPO_DEAL_KEY,
            Appodeal.BANNER or Appodeal.NATIVE,
            object : ApdInitializationCallback {
                override fun onInitializationFinished(errors: List<ApdInitializationError>?) {

                    val initResult = when {
                        errors.isNullOrEmpty() -> "successfully"
                        else -> "with ${errors.size} errors"
                    }

                    Timber.e("Appodeal initResult: $initResult , errors: $errors")
                }
            })
    }

    fun showBanner(activity: Activity, viewId: Int) {

        Appodeal.setBannerViewId(viewId)
        Appodeal.show(activity, Appodeal.BANNER_VIEW)
        Appodeal.setBannerCallbacks(object : BannerCallbacks {
            override fun onBannerLoaded(height: Int, isPrecache: Boolean) {
                // Called when banner is loaded
                Timber.d("Banner loaded: height: $height, isPrecache: $isPrecache")
            }

            override fun onBannerFailedToLoad() {
                // Called when banner failed to load
                Timber.d("Banner failed to load")
            }

            override fun onBannerShown() {
                // Called when banner is shown
                Timber.d("Banner shown")
            }

            override fun onBannerShowFailed() {
                // Called when banner show failed
                Timber.d("Banner show failed")
            }

            override fun onBannerClicked() {
                // Called when banner is clicked
                Timber.d("Banner clicked")
            }

            override fun onBannerExpired() {
                // Called when banner is expired
                Timber.d("Banner expired")
            }
        })
    }

    fun hideBanner(activity: Activity) = Appodeal.hide(activity, Appodeal.BANNER_VIEW)

}