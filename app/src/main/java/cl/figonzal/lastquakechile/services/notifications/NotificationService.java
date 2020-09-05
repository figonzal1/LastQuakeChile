package cl.figonzal.lastquakechile.services.notifications;

import android.app.Activity;
import android.content.Context;
import android.os.Build;

public class NotificationService {

    /**
     * Funcion que crea el canal para notificaciones necesario para celulares > a API 26
     *
     * @param context Contexto necesario para el uso de recursos
     */
    public static void createNotificationChannel(Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            QuakesNotification.createQuakeChannel(context);
            ChangeLogNotification.createChangeLogChannel(context);
        }
    }

    public static void checkSuscriptions(Activity activity) {
        QuakesNotification.checkSuscriptionQuakes(activity);
    }
}
