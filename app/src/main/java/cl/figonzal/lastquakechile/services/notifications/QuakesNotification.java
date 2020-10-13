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
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
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

/**
 * Notificaciones de sismos con implementacion de Firebase
 */
public class QuakesNotification extends FirebaseMessagingService implements NotiService {

    private final Context context;
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
     */
    public boolean checkSuscriptionQuakes() {

        boolean mSuscrito = (boolean) sharedPrefService.getData(context.getString(R.string.FIREBASE_PREF_KEY), true);

        //Suscribir a tema quakes
        if (mSuscrito) {

            FirebaseMessaging.getInstance().subscribeToTopic(context.getString(R.string.FIREBASE_TOPIC_NAME))
                    .addOnCompleteListener(task -> {

                        if (task.isSuccessful()) {

                            Timber.i(context.getString(R.string.TAG_FIREBASE_SUSCRIPTION_OK));
                            crashlytics.setCustomKey(context.getString(R.string.FIREBASE_PREF_KEY), true);
                        }
                    });

            return true;
        }

        //Desuscribir a tema quakes
        else {

            //Eliminacion de la suscripcion
            FirebaseMessaging.getInstance().unsubscribeFromTopic(context.getString(R.string.FIREBASE_TOPIC_NAME))
                    .addOnCompleteListener(task -> {

                        //Modificar valor en sharepref de settings
                        sharedPrefService.saveData(context.getString(R.string.FIREBASE_PREF_KEY), false);

                        //LOG ZONE
                        Timber.i(context.getString(R.string.TAG_FIREBASE_SUSCRIPTION_DELETE));
                        crashlytics.setCustomKey(context.getString(R.string.FIREBASE_PREF_KEY), false);
                    })
                    .addOnFailureListener(e -> Timber.i(context.getString(R.string.TAG_FIREBASE_SUSCRIPTION_ALREADY)));

            return false;
        }
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        this.remoteMessage = remoteMessage;

        Timber.tag(getString(R.string.TAG_FIREBASE_MESSAGE)).i("From: %s", remoteMessage.getFrom());

        //Si es notificacion con datos de sismos
        if (remoteMessage.getData().size() > 0) {

            Timber.tag(getString(R.string.TAG_FIREBASE_MESSAGE)).i("Message data payload: %s", remoteMessage.getData());
            crashlytics.setCustomKey(getString(R.string.FIREBASE_MESSAGE_DATA_STATUS), true);

            showNotification();
        }

        //Si es notificacion desde consola FCM
        if (remoteMessage.getNotification() != null) {

            Timber.tag(getString(R.string.TAG_FIREBASE_MESSAGE)).i("Message notification: " + remoteMessage.getNotification().getTitle() + " - " + remoteMessage.getNotification().getBody());
            crashlytics.setCustomKey(getString(R.string.FIREBASE_MESSAGE_NOTIFICATION_STATUS), true);

            showNotificationGeneric();
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
        boolean sensible;
        String referencia;
        String imagen_url;


        try {

            titulo = mObject.getString(getString(R.string.INTENT_TITULO));
            descripcion = mObject.getString(getString(R.string.INTENT_DESCRIPCION));
            fecha_utc = mObject.getString(getString(R.string.INTENT_FECHA_UTC));
            ciudad = mObject.getString(getString(R.string.INTENT_CIUDAD));
            referencia = mObject.getString(getString(R.string.INTENT_REFERENCIA));
            latitud = mObject.getString(getString(R.string.INTENT_LATITUD));
            longitud = mObject.getString(getString(R.string.INTENT_LONGITUD));
            magnitud = mObject.getDouble(getString(R.string.INTENT_MAGNITUD));
            escala = mObject.getString(getString(R.string.INTENT_ESCALA));
            profundidad = mObject.getDouble(getString(R.string.INTENT_PROFUNDIDAD));
            estado = mObject.getString(getString(R.string.INTENT_ESTADO));
            sensible = mObject.getInt(getString(R.string.INTENT_SENSIBLE)) == 1;

            imagen_url = mObject.getString(getString(R.string.INTENT_LINK_FOTO));

             /*
            PREPARACION DE INTENT DESDE INFO EN PHP
            */
            Intent mIntent = new Intent(this, QuakeDetailsActivity.class);
            mIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            Bundle mBundle = new Bundle();

            mBundle.putString(getString(R.string.INTENT_TITULO), titulo);
            mBundle.putString(getString(R.string.INTENT_DESCRIPCION), descripcion);

            mBundle.putString(getString(R.string.INTENT_CIUDAD), ciudad);
            mBundle.putString(getString(R.string.INTENT_FECHA_UTC), fecha_utc);
            mBundle.putString(getString(R.string.INTENT_LATITUD), latitud);
            mBundle.putString(getString(R.string.INTENT_LONGITUD), longitud);
            mBundle.putDouble(getString(R.string.INTENT_MAGNITUD), magnitud);
            mBundle.putBoolean(getString(R.string.INTENT_SENSIBLE), sensible);
            mBundle.putDouble(getString(R.string.INTENT_PROFUNDIDAD), profundidad);
            mBundle.putString(getString(R.string.INTENT_ESCALA), escala);
            mBundle.putString(getString(R.string.INTENT_REFERENCIA), referencia);
            mBundle.putString(getString(R.string.INTENT_LINK_FOTO), imagen_url);
            mBundle.putString(getString(R.string.INTENT_ESTADO), estado);

            mIntent.putExtras(mBundle);

            PendingIntent mPendingIntent = PendingIntent.getActivity(this, 0, mIntent, PendingIntent.FLAG_ONE_SHOT);

            Timber.i(getString(R.string.TRY_INTENT_NOTIFICATION_1));
            crashlytics.setCustomKey(getString(R.string.TRY_INTENT_NOTIFICATION), true);

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this,
                    getString(R.string.FIREBASE_CHANNEL_ID_QUAKES))
                    .setSmallIcon(R.drawable.ic_lastquakechile_400)
                    .setContentTitle(titulo)
                    .setContentText(descripcion)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(descripcion))
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setAutoCancel(true)
                    .setContentIntent(mPendingIntent);

            NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            mNotificationManager.notify(new Random().nextInt(60000), mBuilder.build());

        } catch (JSONException e) {
            Timber.e(e, "JSon object exception error: %s", e.getMessage());
        }
    }

    /**
     * Funcion que muestra notificacion generica
     */
    private void showNotificationGeneric() {

        //Maneja la notificacion cuando esta en foreground
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, getString(R.string.FIREBASE_CHANNEL_ID_QUAKES))
                .setContentTitle(Objects.requireNonNull(remoteMessage.getNotification()).getTitle())
                .setContentText(remoteMessage.getNotification().getBody())
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(remoteMessage.getNotification().getBody()))
                .setSmallIcon(R.drawable.ic_lastquakechile_400)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(Integer.parseInt(getString(R.string.FIREBASE_CHANNEL_ID_QUAKES)), mBuilder.build());
    }

    /**
     * Funcion que muestra notificacion desde PHP
     */
    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);

        Timber.tag(getString(R.string.TAG_FIREBASE_TOKEN)).i("Refreshed Token: %s", s);
        crashlytics.setUserId(s);
    }

    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();
    }
}
