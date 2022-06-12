package cl.figonzal.lastquakechile.core.services

import android.app.Activity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import cl.figonzal.lastquakechile.R
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import timber.log.Timber

class GooglePlayService(
    private val activity: Activity
) : DefaultLifecycleObserver {

    private val googlePlay = GoogleApiAvailability.getInstance()

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)

        checkPlayServices()
    }

    private fun checkPlayServices() {

        val resultCode = googlePlay.isGooglePlayServicesAvailable(activity)

        when {
            //If some problem occurred
            resultCode != ConnectionResult.SUCCESS -> when {
                googlePlay.isUserResolvableError(resultCode) -> {

                    Timber.e(activity.getString(R.string.GP_REQUEST))

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
                    Timber.e(activity.getString(R.string.GP_NOT_SUPPORTED))
                    activity.finish()
                }
            }
            else -> Timber.d(activity.getString(R.string.GP_UPDATED))
        }
    }

    companion object {
        private const val PLAY_SERVICES_RESOLUTION_REQUEST = 9000
    }
}