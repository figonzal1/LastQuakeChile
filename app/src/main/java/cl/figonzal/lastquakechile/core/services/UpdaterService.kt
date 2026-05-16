package cl.figonzal.lastquakechile.core.services

import android.app.Activity
import android.content.IntentSender.SendIntentException
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics
import timber.log.Timber

private const val FIREBASE_LQCH_UPDATER_STATUS = "lqch_updater_status"

class UpdaterService(
    private val activity: Activity,
    private val activityResultLauncher: ActivityResultLauncher<IntentSenderRequest>
) {

    private var manager = AppUpdateManagerFactory.create(activity)
    private var crashlytics = Firebase.crashlytics

    /**
     * The Play Core appUpdateInfo task is async; by the time it resolves the Activity may have
     * been destroyed/recreated, which unregisters the ActivityResultLauncher. Launching it then
     * throws IllegalStateException. Only launch while the Activity is alive and at least STARTED.
     */
    private fun canLaunch(): Boolean {
        if (activity.isFinishing || activity.isDestroyed) return false
        val owner = activity as? LifecycleOwner ?: return true
        return owner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
    }

    private fun launchUpdate(appUpdateInfo: AppUpdateInfo) {
        if (!canLaunch()) {
            Timber.w("Update flow skipped: activity not in valid state")
            crashlytics.setCustomKey(FIREBASE_LQCH_UPDATER_STATUS, "Skipped: invalid lifecycle")
            return
        }
        try {
            manager.startUpdateFlowForResult(
                appUpdateInfo,
                activityResultLauncher,
                AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build()
            )
        } catch (e: SendIntentException) {
            Timber.e(e, "Update intent failed")
            crashlytics.setCustomKey(FIREBASE_LQCH_UPDATER_STATUS, "Update intent failed")
        } catch (e: IllegalStateException) {
            Timber.e(e, "Launcher unregistered when starting update flow")
            crashlytics.setCustomKey(FIREBASE_LQCH_UPDATER_STATUS, "Launcher unregistered")
        }
    }

    fun checkAvailability() {

        val appUpdateInfoTask = manager.appUpdateInfo

        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            when {
                appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                        && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE) -> {
                    Timber.d("Update available")
                    crashlytics.setCustomKey(FIREBASE_LQCH_UPDATER_STATUS, "Update available")

                    launchUpdate(appUpdateInfo)
                }

                else -> {
                    Timber.d("No new updates available")
                    crashlytics.setCustomKey(
                        FIREBASE_LQCH_UPDATER_STATUS,
                        "No new updates available"
                    )
                }
            }
        }
    }


    fun resumeUpdater() {

        manager
            .appUpdateInfo
            .addOnSuccessListener { appUpdateInfo ->

                if (appUpdateInfo.updateAvailability()
                    == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
                ) {
                    launchUpdate(appUpdateInfo)
                }
            }
    }

}