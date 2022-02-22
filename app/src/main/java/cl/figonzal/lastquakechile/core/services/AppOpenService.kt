package cl.figonzal.lastquakechile.core.services

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import cl.figonzal.lastquakechile.R
import cl.figonzal.lastquakechile.core.ApplicationController
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.appopen.AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT
import com.google.android.gms.ads.appopen.AppOpenAd.AppOpenAdLoadCallback
import timber.log.Timber
import java.time.LocalDateTime
import java.time.ZoneOffset


class AppOpenService(private val applicationController: ApplicationController) :
    Application.ActivityLifecycleCallbacks, DefaultLifecycleObserver {

    init {
        applicationController.registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    private var loadTime: Long = 0
    private val adUnitId = applicationController.getString(R.string.ADMOB_ID_APPOPEN)

    private var appOpenAd: AppOpenAd? = null
    private var currentActivity: Activity? = null

    private var isShowingAd = false

    private lateinit var loadCallback: AppOpenAdLoadCallback

    /** Request an ad  */
    fun fetchAd() {

        if (isAdAvailable()) {
            return
        }

        loadCallback = object : AppOpenAdLoadCallback() {

            override fun onAdLoaded(p0: AppOpenAd) {
                this@AppOpenService.appOpenAd = p0
                this@AppOpenService.loadTime =
                    LocalDateTime.now().atZone(ZoneOffset.UTC).toInstant().toEpochMilli()

            }

            override fun onAdFailedToLoad(p0: LoadAdError) {
                super.onAdFailedToLoad(p0)
            }
        }

        val adRequest = getAdRequest()
        AppOpenAd.load(
            applicationController.applicationContext,
            adUnitId,
            adRequest,
            APP_OPEN_AD_ORIENTATION_PORTRAIT,
            loadCallback
        )
    }

    /** Creates and returns ad request.  */
    private fun getAdRequest(): AdRequest {
        return AdRequest.Builder().build()
    }

    /** Utility method that checks if ad exists and can be shown.  */
    private fun isAdAvailable(): Boolean {
        return appOpenAd != null && wasLoadTimeLessThanNHoursAgo(4)
    }

    /** Check if ad was loaded more than n hours ago. */
    private fun wasLoadTimeLessThanNHoursAgo(numHours: Long): Boolean {
        val dateDifference: Long =
            LocalDateTime.now().atZone(ZoneOffset.UTC).toInstant().toEpochMilli() - loadTime
        val numMilliSecondsPerHour: Long = 3600000
        return dateDifference < numMilliSecondsPerHour * numHours
    }

    /** Shows the ad if one isn't already showing.  */
    private fun showAdIfAvailable() {

        // Only show ad if there is not already an app open ad currently showing
        // and an ad is available.
        if (!isShowingAd && isAdAvailable()) {

            Timber.d(applicationController.getString(R.string.APP_OPEN_SHOW_AD))
            val fullScreenContentCallback: FullScreenContentCallback =
                object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        // Set the reference to null so isAdAvailable() returns false.
                        appOpenAd = null
                        isShowingAd = false
                        fetchAd()
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {}
                    override fun onAdShowedFullScreenContent() {
                        isShowingAd = true
                    }
                }
            appOpenAd?.fullScreenContentCallback = fullScreenContentCallback
            appOpenAd?.show(currentActivity!!)
        } else {
            Timber.d(applicationController.getString(R.string.APP_OPEN_AD_NOT_SHOW))
            fetchAd()
        }
    }

    override fun onStart(owner: LifecycleOwner) {
        Timber.d(applicationController.getString(R.string.AD_OPEN_ON_START))

        showAdIfAvailable()
    }

    override fun onActivityCreated(p0: Activity, p1: Bundle?) {}

    override fun onActivityStarted(p0: Activity) {
        currentActivity = p0
    }

    override fun onActivityResumed(p0: Activity) {
        currentActivity = p0
    }

    override fun onActivityPaused(p0: Activity) {}

    override fun onActivityStopped(p0: Activity) {}

    override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {}

    override fun onActivityDestroyed(p0: Activity) {
        currentActivity = null
    }
}