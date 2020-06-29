package cl.figonzal.lastquakechile.services.notifications;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
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
import cl.figonzal.lastquakechile.views.activities.QuakeDetailsActivity;

public class FCMNotification extends FirebaseMessagingService {


    /**
     * Funcion encargada de crear canal de notificaciones de sismos
     *
     * @param context Contexto para utilizar strings
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    static void createQuakeChannel(Context context) {

        FirebaseCrashlytics crashlytics = FirebaseCrashlytics.getInstance();


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

        assert mNotificationManager != null;
        mNotificationManager.createNotificationChannel(mNotificationChannel);

        Log.d(context.getString(R.string.TAG_FIREBASE_CHANNEL),
                context.getString(R.string.FIREBASE_CHANNEL_CREATED_MESSAGE));

        //CRASH ANALYTICS & LOGS
        crashlytics.log(context.getString(R.string.TAG_FIREBASE_CHANNEL) + context.getString(R.string.FIREBASE_CHANNEL_CREATED_MESSAGE));
        crashlytics.setCustomKey(context.getString(R.string.FIREBASE_CHANNEL_STATUS), true);
    }

    /**
     * Funcion encargada de checkear la suscripcion del usuario al canal de alertas de sismos
     *
     * @param activity Necesario para el uso de recursos de string
     */
    public static boolean checkSuscriptionQuakes(final Activity activity) {

        final FirebaseCrashlytics crashlytics = FirebaseCrashlytics.getInstance();

        final SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(activity);

        boolean mSuscrito = sharedPreferences.getBoolean(activity.getString(R.string.FIREBASE_PREF_KEY), true);

        //Suscribir a tema quakes
        if (mSuscrito) {
            FirebaseMessaging.getInstance().subscribeToTopic(activity.getString(R.string.FIREBASE_TOPIC_NAME))
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {

                                Log.d(activity.getString(R.string.TAG_FIREBASE_SUSCRIPTION),
                                        activity.getString(R.string.TAG_FIREBASE_SUSCRIPTION_OK));

                                //CRASH ANALYTIC LOG
                                crashlytics.setCustomKey(activity.getString(R.string.FIREBASE_PREF_KEY), true);
                                crashlytics.log(activity.getString(R.string.TAG_FIREBASE_SUSCRIPTION) + activity.getString(R.string.TAG_FIREBASE_SUSCRIPTION_OK));
                            }
                        }
                    });

            return true;

        }

        //Desuscribir a tema quakes
        else {
            //Eliminacion de la suscripcion
            FirebaseMessaging.getInstance().unsubscribeFromTopic(activity.getString(R.string.FIREBASE_TOPIC_NAME))
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            //Modificar valor en sharepref de settings
                            sharedPreferences.edit().putBoolean(activity.getString(R.string.FIREBASE_PREF_KEY),
                                    false).apply();

                            //LOG ZONE
                            Log.d(activity.getString(R.string.TAG_FIREBASE_SUSCRIPTION),
                                    activity.getString(R.string.TAG_FIREBASE_SUSCRIPTION_DELETE));
                            crashlytics.log(activity.getString(R.string.TAG_FIREBASE_SUSCRIPTION) + activity.getString(R.string.TAG_FIREBASE_SUSCRIPTION_DELETE));
                            crashlytics.setCustomKey(activity.getString(R.string.FIREBASE_PREF_KEY), false);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(activity.getString(R.string.TAG_FIREBASE_SUSCRIPTION),
                                    activity.getString(R.string.TAG_FIREBASE_SUSCRIPTION_ALREADY));
                            crashlytics.log(activity.getString(R.string.TAG_FIREBASE_SUSCRIPTION) + activity.getString(R.string.TAG_FIREBASE_SUSCRIPTION_ALREADY));
                        }
                    });

            return false;

        }
    }

    /**
     * Funcion utilizada para debuguear mensajes FCM y Servidor LastQuakeChile
     *
     * @param remoteMessage Mensaje de notificacion con los datos de sismos
     */
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        FirebaseCrashlytics crashlytics = FirebaseCrashlytics.getInstance();

        Log.d(getString(R.string.TAG_FIREBASE_MESSAGE), "From: " + remoteMessage.getFrom());
        crashlytics.log(getString(R.string.TAG_FIREBASE_MESSAGE) + "From: " + remoteMessage.getFrom());

        //Si es notificacion con datos de sismos
        if (remoteMessage.getData().size() > 0) {
            Log.d(getString(R.string.TAG_FIREBASE_MESSAGE),
                    "Message data payload: " + remoteMessage.getData());

            crashlytics.log(getString(R.string.TAG_FIREBASE_MESSAGE) + getString(R.string.TAG_FIREBASE_MESSAGE_DATA_INCOMING));
            crashlytics.setCustomKey(getString(R.string.FIREBASE_MESSAGE_DATA_STATUS), true);
            showNotificationQuakesData(remoteMessage);
        }

        //Si es notificacion desde consola FCM
        if (remoteMessage.getNotification() != null) {

            showNotificationGeneric(remoteMessage);
            Log.d(getString(R.string.TAG_FIREBASE_MESSAGE),
                    "Message notification: " + remoteMessage.getNotification().getTitle() + " - " + remoteMessage.getNotification().getBody());

            crashlytics.log(getString(R.string.TAG_FIREBASE_MESSAGE) + getString(R.string.TAG_FIREBASE_MESSAGE_INCOMING));
            crashlytics.setCustomKey(getString(R.string.FIREBASE_MESSAGE_NOTIFICATION_STATUS), true);
        }
    }

    /**
     * Funcion que muestra notificacion
     *
     * @param remoteMessage mensaje fcm
     */
    private void showNotificationGeneric(RemoteMessage remoteMessage) {

        //Maneja la notificacion cuando esta en foreground
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, getString(R.string.FIREBASE_CHANNEL_ID_QUAKES))
                .setContentTitle(Objects.requireNonNull(remoteMessage.getNotification()).getTitle())
                .setContentText(remoteMessage.getNotification().getBody())
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(remoteMessage.getNotification().getBody()))
                .setSmallIcon(R.drawable.ic_lastquakechile_1200)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        assert notificationManager != null;

        //Notificar a sistema
        notificationManager.notify(Integer.parseInt(getString(R.string.FIREBASE_CHANNEL_ID_QUAKES)), mBuilder.build());
    }

    /**
     * Funcion que muestra notificacion
     *
     * @param remoteMessage mensaje desde servidor
     */
    private void showNotificationQuakesData(RemoteMessage remoteMessage) {

        FirebaseCrashlytics crashlytics = FirebaseCrashlytics.getInstance();

        //Obtener datos desde send_notification.php en servidor
        Map<String, String> mParams = remoteMessage.getData();
        JSONObject mObject = new JSONObject(mParams);

        String titulo = null;
        String descripcion = null;

        String ciudad = null;
        String fecha_utc = null;
        String estado = null;
        String latitud = null;
        String longitud = null;
        Double magnitud = null;
        String escala = null;
        Double profundidad = null;
        Boolean sensible = null;
        String referencia = null;
        String imagen_url = null;


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

            switch (mObject.getInt(getString(R.string.INTENT_SENSIBLE))) {
                case 0:
                    sensible = false;
                    break;
                case 1:
                    sensible = true;
                    break;
            }

            imagen_url = mObject.getString(getString(R.string.INTENT_LINK_FOTO));
        } catch (JSONException e) {
            e.printStackTrace();
        }



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

        assert magnitud != null;
        mBundle.putDouble(getString(R.string.INTENT_MAGNITUD), magnitud);
        assert sensible != null;
        mBundle.putBoolean(getString(R.string.INTENT_SENSIBLE), sensible);
        mBundle.putDouble(getString(R.string.INTENT_PROFUNDIDAD), profundidad);
        mBundle.putString(getString(R.string.INTENT_ESCALA), escala);
        mBundle.putString(getString(R.string.INTENT_REFERENCIA), referencia);
        mBundle.putString(getString(R.string.INTENT_LINK_FOTO), imagen_url);
        mBundle.putString(getString(R.string.INTENT_ESTADO), estado);

        mIntent.putExtras(mBundle);

        PendingIntent mPendingIntent = PendingIntent.getActivity(this, 0, mIntent,
                PendingIntent.FLAG_ONE_SHOT);

        Log.d(getString(R.string.TAG_INTENT), getString(R.string.TRY_INTENT_NOTIFICATION_1));
        crashlytics.log(getString(R.string.TAG_INTENT) + getString(R.string.TRY_INTENT_NOTIFICATION_1));
        crashlytics.setCustomKey(getString(R.string.TRY_INTENT_NOTIFICATION), true);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this,
                getString(R.string.FIREBASE_CHANNEL_ID_QUAKES))
                .setSmallIcon(R.drawable.ic_lastquakechile_1200)
                .setContentTitle(titulo)
                .setContentText(descripcion)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(descripcion))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setAutoCancel(true)
                .setContentIntent(mPendingIntent);


        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        //Id necesario para que las notificaciones no se reemplacen
        assert mNotificationManager != null;
        mNotificationManager.notify(new Random().nextInt(60000), mBuilder.build());


    }

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);

        FirebaseCrashlytics firebaseCrashlytics = FirebaseCrashlytics.getInstance();

        Log.d(getString(R.string.TAG_FIREBASE_TOKEN), "Refreshed Token:" + s);
        firebaseCrashlytics.setUserId(s);
    }

    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();
    }
}
