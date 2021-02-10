package cl.figonzal.lastquakechile.services.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;
import java.util.Objects;
import java.util.Random;

import cl.figonzal.lastquakechile.R;
import cl.figonzal.lastquakechile.services.SharedPrefService;
import cl.figonzal.lastquakechile.views.activities.QuakeDetailsActivity;
import timber.log.Timber;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Notificaciones de sismos con implementacion de Firebase
 */
public class QuakesNotification implements NotificationService {

    private final Context context;
    @NonNull
    private final FirebaseCrashlytics crashlytics;
    private final SharedPrefService sharedPrefService;
    private RemoteMessage remoteMessage;

    public QuakesNotification(Context context, SharedPrefService sharedPrefService) {
        this.context = context;
        this.sharedPrefService = sharedPrefService;

        crashlytics = FirebaseCrashlytics.getInstance();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void createChannel() {

        //Definicion de atributos de canal de notificacion
        String name = context.getString(R.string.FIREBASE_CHANNEL_NAME_QUAKES);
        String description = context.getString(R.string.FIREBASE_CHANNEL_DESCRIPTION_QUAKES);

        int importance = NotificationManager.IMPORTANCE_HIGH;

        NotificationChannel mNotificationChannel = new NotificationChannel(context.getString(R.string.FIREBASE_CHANNEL_ID_QUAKES), name, importance);
        mNotificationChannel.setDescription(description);
        mNotificationChannel.setImportance(NotificationManager.IMPORTANCE_HIGH);
        mNotificationChannel.enableLights(true);
        mNotificationChannel.setLightColor(R.color.colorSecondary);

        NotificationManager mNotificationManager = context.getSystemService(NotificationManager.class);
        mNotificationManager.createNotificationChannel(mNotificationChannel);

        Timber.i(context.getString(R.string.FIREBASE_CHANNEL_CREATED_MESSAGE));
        crashlytics.setCustomKey(context.getString(R.string.FIREBASE_CHANNEL_STATUS), true);
    }

    /**
     * Funcion encargada de checkear la suscripcion del usuario al canal de alertas de sismos
     *
     * @param subcribed Boleano para determinar suscripcion a tema
     */
    public void suscribedToQuakes(boolean subcribed) {

        //Suscribir a tema quakes
        if (subcribed) {

            com.google.firebase.messaging.FirebaseMessaging.getInstance().subscribeToTopic(context.getString(R.string.FIREBASE_TOPIC_NAME))
                    .addOnCompleteListener(task -> {

                        if (task.isSuccessful()) {

                            //Modificar valor en sharepref de settings
                            sharedPrefService.saveData(context.getString(R.string.FIREBASE_PREF_KEY), true);

                            Timber.i(context.getString(R.string.TAG_FIREBASE_SUSCRIPTION_OK));
                            crashlytics.setCustomKey(context.getString(R.string.SUSCRITO_QUAKE), true);
                        }
                    });
        }

        //Desuscribir a tema quakes
        else {

            //Eliminacion de la suscripcion
            com.google.firebase.messaging.FirebaseMessaging.getInstance().unsubscribeFromTopic(context.getString(R.string.FIREBASE_TOPIC_NAME))
                    .addOnCompleteListener(task -> {

                        //Modificar valor en sharepref de settings
                        sharedPrefService.saveData(context.getString(R.string.FIREBASE_PREF_KEY), false);

                        //LOG ZONE
                        Timber.i(context.getString(R.string.TAG_FIREBASE_SUSCRIPTION_DELETE));
                        crashlytics.setCustomKey(context.getString(R.string.SUSCRITO_QUAKE), false);
                    })
                    .addOnFailureListener(e -> Timber.i(context.getString(R.string.TAG_FIREBASE_SUSCRIPTION_ALREADY)));
        }
    }

    /**
     * Funcion que muestra notificacion de FCM
     */
    @Override
    public void showNotification() {
        //Obtener datos desde send_notification.php en servidor
        Map<String, String> mParams = remoteMessage.getData();
        JSONObject mObject = new JSONObject(mParams);

        String titulo;
        String descripcion;

        String ciudad;
        String fecha_utc;
        String estado;
        String latitud;
        String longitud;
        double magnitud;
        String escala;
        double profundidad;
        String sensible;
        String referencia;
        String imagen_url;


        try {

            titulo = mObject.getString(context.getString(R.string.INTENT_TITULO));
            descripcion = mObject.getString(context.getString(R.string.INTENT_DESCRIPCION));
            fecha_utc = mObject.getString(context.getString(R.string.INTENT_FECHA_UTC));
            ciudad = mObject.getString(context.getString(R.string.INTENT_CIUDAD));
            referencia = mObject.getString(context.getString(R.string.INTENT_REFERENCIA));
            latitud = mObject.getString(context.getString(R.string.INTENT_LATITUD));
            longitud = mObject.getString(context.getString(R.string.INTENT_LONGITUD));
            magnitud = mObject.getDouble(context.getString(R.string.INTENT_MAGNITUD));
            escala = mObject.getString(context.getString(R.string.INTENT_ESCALA));
            profundidad = mObject.getDouble(context.getString(R.string.INTENT_PROFUNDIDAD));
            estado = mObject.getString(context.getString(R.string.INTENT_ESTADO));
            sensible = mObject.getString(context.getString(R.string.INTENT_SENSIBLE));

            imagen_url = mObject.getString(context.getString(R.string.INTENT_LINK_FOTO));

             /*
            PREPARACION DE INTENT DESDE INFO EN PHP
            */
            Intent mIntent = new Intent(context, QuakeDetailsActivity.class);
            mIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            Bundle mBundle = new Bundle();

            mBundle.putString(context.getString(R.string.INTENT_TITULO), titulo);
            mBundle.putString(context.getString(R.string.INTENT_DESCRIPCION), descripcion);

            mBundle.putString(context.getString(R.string.INTENT_CIUDAD), ciudad);
            mBundle.putString(context.getString(R.string.INTENT_FECHA_UTC), fecha_utc);
            mBundle.putString(context.getString(R.string.INTENT_LATITUD), latitud);
            mBundle.putString(context.getString(R.string.INTENT_LONGITUD), longitud);
            mBundle.putDouble(context.getString(R.string.INTENT_MAGNITUD), magnitud);
            mBundle.putString(context.getString(R.string.INTENT_SENSIBLE), sensible);
            mBundle.putDouble(context.getString(R.string.INTENT_PROFUNDIDAD), profundidad);
            mBundle.putString(context.getString(R.string.INTENT_ESCALA), escala);
            mBundle.putString(context.getString(R.string.INTENT_REFERENCIA), referencia);
            mBundle.putString(context.getString(R.string.INTENT_LINK_FOTO), imagen_url);
            mBundle.putString(context.getString(R.string.INTENT_ESTADO), estado);

            mIntent.putExtras(mBundle);

            PendingIntent mPendingIntent = PendingIntent.getActivity(context, 0, mIntent, PendingIntent.FLAG_ONE_SHOT);

            Timber.i(context.getString(R.string.TRY_INTENT_NOTIFICATION_1));
            crashlytics.setCustomKey(context.getString(R.string.TRY_INTENT_NOTIFICATION), true);

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context,
                    context.getString(R.string.FIREBASE_CHANNEL_ID_QUAKES))
                    .setSmallIcon(R.drawable.ic_lastquakechile_400)
                    .setContentTitle(titulo)
                    .setContentText(descripcion)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(descripcion))
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setAutoCancel(true)
                    .setContentIntent(mPendingIntent);

            NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
            mNotificationManager.notify(new Random().nextInt(60000), mBuilder.build());

        } catch (
                JSONException e) {
            Timber.e(e, "JSon object exception error: %s", e.getMessage());
        }
    }

    /**
     * Funcion que muestra notificacion generica
     *
     * @param remoteMessage Mensaje desde FCM
     */
    public void showNotificationGeneric(@NonNull RemoteMessage remoteMessage) {

        //Maneja la notificacion cuando esta en foreground
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, context.getString(R.string.FIREBASE_CHANNEL_ID_QUAKES))
                .setContentTitle(Objects.requireNonNull(remoteMessage.getNotification()).getTitle())
                .setContentText(remoteMessage.getNotification().getBody())
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(remoteMessage.getNotification().getBody()))
                .setSmallIcon(R.drawable.ic_lastquakechile_400)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(Integer.parseInt(context.getString(R.string.FIREBASE_CHANNEL_ID_QUAKES)), mBuilder.build());
    }

    public void setRemoteMsg(RemoteMessage remoteMessage) {
        this.remoteMessage = remoteMessage;
    }
}

