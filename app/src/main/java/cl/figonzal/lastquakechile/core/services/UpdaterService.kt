package cl.figonzal.lastquakechile.core.services

import android.app.Activity
import android.content.IntentSender.SendIntentException
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber

private const val FIREBASE_LQCH_UPDATER_STATUS = "lqch_updater_status"

class UpdaterService(
    private val activity: Activity,
    private val appUpdateManager: AppUpdateManager,
    private val crashlytics: FirebaseCrashlytics
) {

    private val appUpdateInfoTask = appUpdateManager.appUpdateInfo

    fun checkAvailability() {

        appUpdateInfoTask.addOnSuccessListener { result: AppUpdateInfo ->
            when {
                result.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE && result.isUpdateTypeAllowed(
                    AppUpdateType.IMMEDIATE
                ) -> {

                    Timber.d("Update available")
                    crashlytics.setCustomKey(FIREBASE_LQCH_UPDATER_STATUS, "Update available")

                    try {
                        appUpdateManager.startUpdateFlowForResult(
                            result,
                            AppUpdateType.IMMEDIATE,
                            activity,
                            UPDATE_CODE
                        )
                    } catch (e: SendIntentException) {
                        Timber.e("Update intent failed")
                        crashlytics.setCustomKey(
                            FIREBASE_LQCH_UPDATER_STATUS,
                            "Update intent failed"
                        )

                    }
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
        appUpdateManager
            .appUpdateInfo
            .addOnSuccessListener { appUpdateInfo: AppUpdateInfo ->
                if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                    // If an in-app update is already running, resume the update.
                    try {
                        appUpdateManager.startUpdateFlowForResult(
                            appUpdateInfo,
                            AppUpdateType.IMMEDIATE,
                            activity,
                            UPDATE_CODE
                        )
                    } catch (e: SendIntentException) {
                        Timber.e(e, "onResume updater failed")
                        crashlytics.setCustomKey(
                            FIREBASE_LQCH_UPDATER_STATUS,
                            "onResume updater failed"
                        )

                    }
                }
            }
    }

    companion object {
        const val UPDATE_CODE = 305
    }

}