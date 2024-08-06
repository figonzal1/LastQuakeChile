package cl.figonzal.lastquakechile.core.utils

import android.app.Activity
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean

private var isMobileAdsInitializeCalled = AtomicBoolean(false)


fun Activity.checkEULAConsentAds(initAdsCallback: () -> Unit) {

    val params = ConsentRequestParameters
        .Builder()
        .setTagForUnderAgeOfConsent(false)
        .build()

    val consentInformation = UserMessagingPlatform.getConsentInformation(this@checkEULAConsentAds)
    consentInformation.requestConsentInfoUpdate(
        this@checkEULAConsentAds,
        params,
        {
            UserMessagingPlatform.loadAndShowConsentFormIfRequired(
                this@checkEULAConsentAds
            ) { loadAndShowError ->

                when {
                    loadAndShowError != null -> {

                        // Consent gathering failed.
                        Timber.w(
                            String.format(
                                "%s: %s",
                                loadAndShowError.errorCode,
                                loadAndShowError.message
                            )
                        )
                    }

                    else -> {
                        // Consent has been gathered.
                        if (consentInformation.canRequestAds()) {
                            initializeMobileAdsSdk(initAdsCallback)
                        }
                    }
                }
            }
        },
        { requestConsentError ->
            // Consent gathering failed.
            Timber.w(
                String.format(
                    "%s: %s",
                    requestConsentError.errorCode,
                    requestConsentError.message
                )
            )
        })

    if (consentInformation.canRequestAds()) {
        initializeMobileAdsSdk(initAdsCallback)
    }
}

private fun initializeMobileAdsSdk(initAdsCallback: () -> Unit) {
    if (isMobileAdsInitializeCalled.getAndSet(true)) {
        return
    }

    initAdsCallback()
}