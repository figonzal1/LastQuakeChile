package cl.figonzal.lastquakechile.messageservice;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;
import java.util.Objects;
import java.util.Random;

import cl.figonzal.lastquakechile.QuakeDetailsActivity;
import cl.figonzal.lastquakechile.R;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.d(getString(R.string.TAG_FIREBASE_MESSAGE), "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(getString(R.string.TAG_FIREBASE_MESSAGE), "Message data payload: " + remoteMessage.getData());

            Crashlytics.log(Log.DEBUG, getString(R.string.TAG_FIREBASE_MESSAGE), getString(R.string.TAG_FIREBASE_MESSAGE_DATA_INCOMING));
            Crashlytics.setBool(getString(R.string.FIREBASE_MESSAGE_DATA_STATUS), true);
            showNotificationData(remoteMessage);
        }

        if (remoteMessage.getNotification() != null) {

            showNotification(remoteMessage);
            Log.d(getString(R.string.TAG_FIREBASE_MESSAGE), "Message notification: " + remoteMessage.getNotification().getTitle() + " - " + remoteMessage.getNotification().getBody());

            Crashlytics.log(Log.DEBUG, getString(R.string.TAG_FIREBASE_MESSAGE), getString(R.string.TAG_FIREBASE_MESSAGE_INCOMING));
            Crashlytics.setBool(getString(R.string.FIREBASE_MESSAGE_NOTIFICATION_STATUS), true);


        }
    }

    /**
     * Funcion que procesa notificaciones provenientes de FCM
     *
     * @param remoteMessage mensaje fcm
     */
    private void showNotification(RemoteMessage remoteMessage) {

        //Maneja la notificacion cuando esta en foreground
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, getString(R.string.FIREBASE_CHANNEL_ID))
                .setContentTitle(Objects.requireNonNull(remoteMessage.getNotification()).getTitle())
                .setContentText(remoteMessage.getNotification().getBody())
                .setSmallIcon(R.drawable.ic_lastquakechile_1200)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(Integer.parseInt(getString(R.string.FIREBASE_CHANNEL_ID)), builder.build());
    }

    /**
     * Funcion encargada de procesar notificaciones desde servidor LastQuakeChile
     * @param remoteMessage mensaje desde servidor
     */
    private void showNotificationData(RemoteMessage remoteMessage) {

        /*
            Obtener datos desde send_notification.php en servidor
         */
        Map<String, String> params = remoteMessage.getData();
        JSONObject object = new JSONObject(params);

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
            titulo = object.getString(getString(R.string.INTENT_TITULO));
            descripcion = object.getString(getString(R.string.INTENT_DESCRIPCION));
            fecha_utc = object.getString(getString(R.string.INTENT_FECHA_UTC));
            latitud = object.getString(getString(R.string.INTENT_LATITUD));
            longitud = object.getString(getString(R.string.INTENT_LONGITUD));
            magnitud = object.getDouble(getString(R.string.INTENT_MAGNITUD));
            escala = object.getString(getString(R.string.INTENT_ESCALA));
            profundidad = object.getDouble(getString(R.string.INTENT_PROFUNDIDAD));
            estado = object.getString(getString(R.string.INTENT_ESTADO));

            switch (object.getInt(getString(R.string.INTENT_SENSIBLE))) {
                case 0:
                    sensible = false;
                    break;
                case 1:
                    sensible = true;
                    break;
            }
            referencia = object.getString(getString(R.string.INTENT_REFERENCIA));
            //Guarda despues de 'DE' en la ciudad
            int inicio = referencia.indexOf("de") + 3;
            ciudad = referencia.substring(inicio);

            imagen_url = object.getString(getString(R.string.INTENT_LINK_FOTO));
        } catch (JSONException e) {
            e.printStackTrace();
        }



        /*
            PREPARACION DE INTENT DESDE INFO EN PHP
         */
        Intent intent = new Intent(this, QuakeDetailsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        Bundle b = new Bundle();

        b.putString(getString(R.string.INTENT_TITULO), titulo);
        b.putString(getString(R.string.INTENT_DESCRIPCION), descripcion);

        b.putString(getString(R.string.INTENT_CIUDAD), ciudad);
        b.putString(getString(R.string.INTENT_FECHA_UTC), fecha_utc);
        b.putString(getString(R.string.INTENT_LATITUD), latitud);
        b.putString(getString(R.string.INTENT_LONGITUD), longitud);

        assert magnitud != null;
        b.putDouble(getString(R.string.INTENT_MAGNITUD), magnitud);
        assert sensible != null;
        b.putBoolean(getString(R.string.INTENT_SENSIBLE), sensible);
        b.putDouble(getString(R.string.INTENT_PROFUNDIDAD), profundidad);
        b.putString(getString(R.string.INTENT_ESCALA), escala);
        b.putString(getString(R.string.INTENT_REFERENCIA), referencia);
        b.putString(getString(R.string.INTENT_LINK_FOTO), imagen_url);
        b.putString(getString(R.string.INTENT_ESTADO), estado);

        intent.putExtras(b);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        Log.d(getString(R.string.TAG_INTENT), getString(R.string.TRY_INTENT_NOTIFICATION_1));
        Crashlytics.log(Log.DEBUG, getString(R.string.TAG_INTENT), getString(R.string.TRY_INTENT_NOTIFICATION_1));
        Crashlytics.setBool(getString(R.string.TRY_INTENT_NOTIFICATION), true);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, getString(R.string.FIREBASE_CHANNEL_ID))
                .setSmallIcon(R.drawable.ic_lastquakechile_1200)
                .setContentTitle(titulo)
                .setContentText(descripcion)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);


        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        //Id necesario para que las notificaciones no se reemplacen
        int notificationId = new Random().nextInt(60000);
        notificationManager.notify(notificationId, builder.build());


    }

    public static void createNotificationChannel(Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            //Definicion de atributos de canal de notificacion
            String name = context.getString(R.string.FIREBASE_CHANNEL_NAME);
            String description = context.getString(R.string.FIREBASE_CHANNEL_DESCRIPTION);
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel notificationChannel = new NotificationChannel(context.getString(R.string.FIREBASE_CHANNEL_ID), name, importance);
            notificationChannel.setDescription(description);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(R.color.colorAccent);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel);

            Log.d(context.getString(R.string.TAG_FIREBASE_CHANNEL), context.getString(R.string.FIREBASE_CHANNEL_CREATED_MESSAGE));

            //CRASH ANALYTICS & LOGS
            Crashlytics.log(Log.DEBUG, context.getString(R.string.TAG_FIREBASE_CHANNEL), context.getString(R.string.FIREBASE_CHANNEL_CREATED_MESSAGE));
            Crashlytics.setBool(context.getString(R.string.FIREBASE_CHANNEL_STATUS), true);
        }
    }

    public static void checkSuscription(final Activity activity) {

        final SharedPreferences sharedPreferences = activity.getPreferences(Context.MODE_PRIVATE);
        boolean suscrito = sharedPreferences.getBoolean(activity.getString(R.string.FIREBASE_SUSCRITO), false);

        if (!suscrito) {
            FirebaseMessaging.getInstance().subscribeToTopic(activity.getString(R.string.FIREBASE_TOPIC_NAME))
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {

                                Log.d(activity.getString(R.string.TAG_FIREBASE_SUSCRIPTION), activity.getString(R.string.TAG_FIREBASE_SUSCRIPTION_RESPONSE1));

                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putBoolean(activity.getString(R.string.FIREBASE_SUSCRITO), true);
                                editor.apply();

                                //CRASH ANALYTIC LOG
                                Crashlytics.setBool(activity.getString(R.string.FIREBASE_SUSCRITO), true);
                                Crashlytics.log(Log.DEBUG, activity.getString(R.string.TAG_FIREBASE_SUSCRIPTION), activity.getString(R.string.TAG_FIREBASE_SUSCRIPTION_RESPONSE1));
                            }
                        }
                    });

        } else {
            //FirebaseMessaging.getInstance().unsubscribeFromTopic(activity.getString(R.string.FIREBASE_TOPIC_NAME));
            //Toast.makeText(activity.getApplicationContext(), activity.getString(R.string.FIREBASE_SNACKBAR_SUBSCRIBE_TOPIC_DELETED), Toast.LENGTH_LONG).show();
            //Log.d(activity.getString(R.string.TAG_FIREBASE_SUSCRIPTION), "SUSCRIPCION ELIMINADA");
            Log.d(activity.getString(R.string.TAG_FIREBASE_SUSCRIPTION), activity.getString(R.string.TAG_FIREBASE_SUSCRIPTION_RESPONSE2));
            Crashlytics.log(Log.DEBUG, activity.getString(R.string.TAG_FIREBASE_SUSCRIPTION), activity.getString(R.string.TAG_FIREBASE_SUSCRIPTION_RESPONSE2));
        }
    }

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        Log.d(getString(R.string.TAG_FIREBASE_TOKEN), "Refreshed Token:" + s);
        Crashlytics.setUserIdentifier(s);
    }

    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();
    }
}
