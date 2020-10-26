package cl.figonzal.lastquakechile.services;

import android.app.Activity;
import android.content.IntentSender;

import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.tasks.Task;

import cl.figonzal.lastquakechile.R;
import timber.log.Timber;

public class UpdaterService {

    public static final int UPDATE_CODE = 305;

    private final Activity activity;
    private final AppUpdateManager appUpdateManager;
    private final Task<AppUpdateInfo> appUpdateInfoTask;

    public UpdaterService(Activity activity, AppUpdateManager appUpdateManager) {
        this.activity = activity;
        this.appUpdateManager = appUpdateManager;

        appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();
    }

    public void checkAvailability() {

        appUpdateInfoTask.addOnSuccessListener(result -> {


            if (result.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE && result.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {

                Timber.i(activity.getString(R.string.UPDATE_AVAILABLE));

                try {
                    appUpdateManager.startUpdateFlowForResult(
                            result,
                            AppUpdateType.IMMEDIATE,
                            activity,
                            UPDATE_CODE);
                } catch (IntentSender.SendIntentException e) {
                    Timber.e(e, activity.getString(R.string.UPDATE_INTENT_FAILED));
                }
            } else {
                Timber.i(activity.getString(R.string.UPDATE_NOT_AVAILABLE));
            }
        });
    }

    public void resumeUpdater() {
        appUpdateManager
                .getAppUpdateInfo()
                .addOnSuccessListener(
                        appUpdateInfo -> {

                            if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                                // If an in-app update is already running, resume the update.
                                try {
                                    appUpdateManager.startUpdateFlowForResult(
                                            appUpdateInfo,
                                            AppUpdateType.IMMEDIATE,
                                            activity,
                                            UPDATE_CODE);
                                } catch (IntentSender.SendIntentException e) {
                                    Timber.e(e, "onResume updater manager failed");
                                }
                            }
                        });
    }
}
