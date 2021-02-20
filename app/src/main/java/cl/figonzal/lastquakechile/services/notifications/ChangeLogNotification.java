package cl.figonzal.lastquakechile.services.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.util.Random;

import cl.figonzal.lastquakechile.R;
import cl.figonzal.lastquakechile.services.SharedPrefService;
import timber.log.Timber;

import static android.content.Context.NOTIFICATION_SERVICE;

public class ChangeLogNotification implements NotificationService {

    public static boolean TEST_MODE = false;
    private final Context context;
    private final SharedPrefService sharedPrefService;
    @NonNull
    private final FirebaseCrashlytics crashlytics;

    public ChangeLogNotification(Context context, SharedPrefService sharedPrefService) {
        this.context = context;
        this.sharedPrefService = sharedPrefService;

        crashlytics = FirebaseCrashlytics.getInstance();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void createChannel() {

        String name = context.getString(R.string.FIREBASE_CHANNEL_NAME_CHANGELOG);
        String description = context.getString(R.string.FIREBASE_CHANNEL_DESCRIPTION_CHANGELOG);

        int importance = NotificationManager.IMPORTANCE_LOW;

        NotificationChannel notificationChannel = new NotificationChannel(context.getString(R.string.FIREBASE_CHANNEL_ID_CHANGELOG), name, importance);
        notificationChannel.setDescription(description);
        notificationChannel.setImportance(importance);
        notificationChannel.enableLights(true);
        notificationChannel.setLightColor(R.color.colorSecondary);

        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(notificationChannel);

        Timber.i(context.getString(R.string.FIREBASE_CHANNEL_CREATED_MESSAGE));
        crashlytics.setCustomKey(context.getString(R.string.FIREBASE_CHANNEL_STATUS), true);
    }

    @Override
    public void showNotification() {

        //TODO: LLENAR CHANGE LOG PARA CADA DEPLOY (SI HAY)
        String changelog = context.getString(R.string.LAST_CHANGE_LOG);

        try {
            //Maneja la notificacion cuando esta en foreground
            NotificationCompat.Builder mBuilder;

            mBuilder = new NotificationCompat.Builder(
                    context,
                    context.getString(R.string.FIREBASE_CHANNEL_ID_CHANGELOG))
                    .setContentTitle(context.getString(R.string.NOTIFICATION_CHANGE_LOG_TITLE) + context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName)
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText(changelog))
                    .setSmallIcon(R.drawable.ic_lastquakechile_400)
                    .setAutoCancel(true);

            if (!changelog.isEmpty() || TEST_MODE) {

                NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
                notificationManager.notify(new Random().nextInt(60000), mBuilder.build());

                //Logs
                Timber.i(context.getString(R.string.TAG_NOTIFICATION_CHANGELOG_STATUS_RESPONSE_SHOWED));

            } else {

                //Logs
                Timber.i(context.getString(R.string.TAG_NOTIFICATION_CHANGELOG_STATUS_RESPONSE_EMPTY));
            }
        } catch (PackageManager.NameNotFoundException e) {
            Timber.e(e, "Package name not found: %s", e.getMessage());
        }
    }

    /**
     * Funcion encargada de definir si muestra o no el changeLog
     */
    public void configNotificationChangeLog() {
        try {

            //GET PACKAGE VERSION
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            long versionCode = packageInfo.versionCode;

            //GET SHARED PREF VERSION SAVED
            long shared_version_code = (long) sharedPrefService.getData(context.getString(R.string.SHARED_PREF_ACTUAL_VERSION_CODE), 0L);

            Timber.i(context.getString(R.string.TAG_SHARED_VERSION_CODE_APP) + ": " + shared_version_code);
            Timber.i(context.getString(R.string.TAG_VERSION_CODE_APP) + ": " + versionCode);

            if (!ChangeLogNotification.TEST_MODE) {

                if (shared_version_code < versionCode) {

                    showNotification();
                    sharedPrefService.saveData(context.getString(R.string.SHARED_PREF_ACTUAL_VERSION_CODE), versionCode);

                    //Logs
                    Timber.i(context.getString(R.string.TAG_NOTIFICATION_CHANGELOG_STATUS_RESPONSE_SENDED));
                }

            } else {
                showNotification();
            }

        } catch (PackageManager.NameNotFoundException e) {

            Timber.e(e, "Nombre del paquete no encontrado: %s", e.getMessage());
        }
    }
}
