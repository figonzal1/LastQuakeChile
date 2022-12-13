package cl.figonzal.lastquakechile.core.services

import android.app.Activity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber

private const val FIREBASE_GOOGLE_PLAY_SERVICE_STATE = "google_play_service_state"
private const val PLAY_SERVICES_RESOLUTION_REQUEST = 9000

class GooglePlayService(
    private val activity: Activity,
    private val crashlytics: FirebaseCrashlytics
) : DefaultLifecycleObserver {

    private val googlePlay = GoogleApiAvailability.getInstance()

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)

        checkPlayServices()
    }

    private fun checkPlayServices() {

        val resultCode = googlePlay.isGooglePlayServicesAvailable(activity)

        //If some problem occurred
        if (resultCode != ConnectionResult.SUCCESS) {


            when {
                googlePlay.isUserResolvableError(resultCode) -> {

                    Timber.e("Request update")
                    crashlytics.setCustomKey(FIREBASE_GOOGLE_PLAY_SERVICE_STATE, "Request update")

                    //Tell user that need to update Google play
                    googlePlay.getErrorDialog(
                        activity,
                        resultCode,
                        PLAY_SERVICES_RESOLUTION_REQUEST
                    )?.apply {
                        setCanceledOnTouchOutside(false)
                        show()
                    }
                }
                else -> {
                    //The problem cannot be handle & the app close
                    Timber.e("Not supported")
                    crashlytics.setCustomKey(FIREBASE_GOOGLE_PLAY_SERVICE_STATE, "Not supported")
                    activity.finish()
                }
            }

        } else {
            Timber.d("Updated")
            crashlytics.setCustomKey(FIREBASE_GOOGLE_PLAY_SERVICE_STATE, "Updated")
        }
    }
}