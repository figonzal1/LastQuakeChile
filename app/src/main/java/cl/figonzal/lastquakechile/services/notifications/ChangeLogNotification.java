package cl.figonzal.lastquakechile.services.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.crashlytics.android.Crashlytics;

import java.util.Random;

import cl.figonzal.lastquakechile.R;

import static android.content.Context.NOTIFICATION_SERVICE;

public class ChangeLogNotification {

    /**
     * Funcion encargada de crear canal de notificaciones de cambios de version
     *
     * @param context Contexto para utilizar Strings
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    static void createChangeLogChannel(Context context) {

        String name = context.getString(R.string.FIREBASE_CHANNEL_NAME_CHANGELOG);
        String description = context.getString(R.string.FIREBASE_CHANNEL_DESCRIPTION_CHANGELOG);
        int importance = NotificationManager.IMPORTANCE_LOW;

        NotificationChannel notificationChannel = new NotificationChannel(context.getString(R.string.FIREBASE_CHANNEL_ID_CHANGELOG), name, importance);
        notificationChannel.setDescription(description);
        notificationChannel.setImportance(importance);
        notificationChannel.enableLights(true);
        notificationChannel.setLightColor(R.color.colorSecondary);

        NotificationManager mNotificationManager =
                context.getSystemService(NotificationManager.class);
        assert mNotificationManager != null;
        mNotificationManager.createNotificationChannel(notificationChannel);

        Log.d(context.getString(R.string.TAG_FIREBASE_CHANNEL),
                context.getString(R.string.FIREBASE_CHANNEL_CREATED_MESSAGE));

        //CRASH ANALYTICS & LOGS
        Crashlytics.log(Log.DEBUG, context.getString(R.string.TAG_FIREBASE_CHANNEL),
                context.getString(R.string.FIREBASE_CHANNEL_CREATED_MESSAGE));
        Crashlytics.setBool(context.getString(R.string.FIREBASE_CHANNEL_STATUS), true);
    }

    /**
     * Funcion encargada de definir si muestra o no el changeLog
     *
     * @param test Parametro para testear notificacion
     */
    public void configNotificationChangeLog(boolean test, Context context) {

        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.MAIN_SHARED_PREF_KEY), Context.MODE_PRIVATE);
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            long versionCode = packageInfo.versionCode;

            long shared_version_code = sharedPreferences.getLong(context.getString(R.string.SHARED_PREF_ACTUAL_VERSION_CODE), 0);

            //Logs post actualizacion
            Log.d(context.getString(R.string.TAG_SHARED_VERSION_CODE_APP),
                    String.valueOf(shared_version_code));
            Log.d(context.getString(R.string.TAG_VERSION_CODE_APP), String.valueOf(versionCode));

            Crashlytics.log(Log.DEBUG, context.getString(R.string.TAG_SHARED_VERSION_CODE_APP),
                    String.valueOf(shared_version_code));
            Crashlytics.log(Log.DEBUG, context.getString(R.string.TAG_VERSION_CODE_APP),
                    String.valueOf(versionCode));


            if (!test) {
                //Si variable shared no exite, actualizar dato
                if (shared_version_code == 0) {
                    //Actualizar version
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putLong(context.getString(R.string.SHARED_PREF_ACTUAL_VERSION_CODE),
                            versionCode);
                    editor.apply();
                }
                if (shared_version_code < versionCode) {

                    showNotificationChangeLog(context);

                    //Actualizar version en shared
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putLong(context.getString(R.string.SHARED_PREF_ACTUAL_VERSION_CODE),
                            versionCode);
                    editor.apply();

                    //Logs
                    Log.d(context.getString(R.string.TAG_NOTIFICATION_CHANGELOG_STATUS),
                            context.getString(R.string.TAG_NOTIFICATION_CHANGELOG_STATUS_RESPONSE));
                    Crashlytics.log(Log.DEBUG,
                            context.getString(R.string.TAG_NOTIFICATION_CHANGELOG_STATUS),
                            context.getString(R.string.TAG_NOTIFICATION_CHANGELOG_STATUS_RESPONSE));
                }
            } else {
                showNotificationChangeLog(context);
            }

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Funcion encargada de enviar la notificacion al celular sobre changelog
     */
    private void showNotificationChangeLog(Context context) throws PackageManager.NameNotFoundException {
        //Maneja la notificacion cuando esta en foreground
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                context,
                context.getString(R.string.FIREBASE_CHANNEL_ID_CHANGELOG))
                .setContentTitle(context.getString(R.string.NOTIFICATION_CHANGE_LOG_TITLE) + context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("- Ahora modo noche automático se activa con ahorro de energía\n" +
                                "- Se agregan reportes sismológicos mensuales \n" +
                                "- Actualizaciones internas\n" +
                                "- Publicidad no invasiva"))
                .setSmallIcon(R.drawable.ic_lastquakechile_1200)
                .setAutoCancel(true);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        assert notificationManager != null;
        notificationManager.notify(new Random().nextInt(60000), mBuilder.build());
    }
}
