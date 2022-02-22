package cl.figonzal.lastquakechile.core.services

import android.app.Activity
import android.content.IntentSender.SendIntentException
import cl.figonzal.lastquakechile.R
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.tasks.Task
import timber.log.Timber

class UpdaterService(
    private val activity: Activity,
    private val appUpdateManager: AppUpdateManager
) {

    private val appUpdateInfoTask: Task<AppUpdateInfo> = appUpdateManager.appUpdateInfo

    fun checkAvailability() {

        appUpdateInfoTask.addOnSuccessListener { result: AppUpdateInfo ->
            when {
                result.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE && result.isUpdateTypeAllowed(
                    AppUpdateType.IMMEDIATE
                ) -> {

                    Timber.d(activity.getString(R.string.UPDATE_AVAILABLE))
                    try {
                        appUpdateManager.startUpdateFlowForResult(
                            result,
                            AppUpdateType.IMMEDIATE,
                            activity,
                            UPDATE_CODE
                        )
                    } catch (e: SendIntentException) {
                        Timber.e(e, activity.getString(R.string.UPDATE_INTENT_FAILED))
                    }
                }
                else -> {
                    Timber.d(activity.getString(R.string.UPDATE_NOT_AVAILABLE))
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
                        Timber.e(e, activity.getString(R.string.UPDATE_MANAGER_FAILED))
                    }
                }
            }
    }

    companion object {
        const val UPDATE_CODE = 305
    }

}