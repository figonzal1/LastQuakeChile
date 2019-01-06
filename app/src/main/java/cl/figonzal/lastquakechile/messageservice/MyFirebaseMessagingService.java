package cl.figonzal.lastquakechile.messageservice;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

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
            showNotificationData(remoteMessage);
        }

        if (remoteMessage.getNotification() != null) {

            Log.d(getString(R.string.TAG_FIREBASE_MESSAGE), "Message notification: " + remoteMessage.getNotification().getTitle() + " - " + remoteMessage.getNotification().getBody());
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, getString(R.string.FIREBASE_CHANNEL_ID))
                    .setSmallIcon(R.mipmap.ic_launcher_chile)
                    .setContentTitle(remoteMessage.getNotification().getTitle())
                    .setContentText(remoteMessage.getNotification().getBody())
                    .setAutoCancel(true);


            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.notify(Integer.parseInt(getString(R.string.FIREBASE_CHANNEL_ID)), builder.build());

        }
    }

    private void showNotificationData(RemoteMessage remoteMessage) {

        /*
            Obtener datos desde send_notification.php en servidor
         */
        Map<String, String> params = remoteMessage.getData();
        JSONObject object = new JSONObject(params);

        String titulo = null;
        String descripcion = null;

        String ciudad = null;
        String fecha_local = null;
        Double magnitud = null;
        String escala = null;
        Double profundidad = null;
        Boolean sensible = null;
        String referencia = null;
        String imagen_url = null;


        try {
            titulo = object.getString("titulo");
            descripcion = object.getString("descripcion");
            fecha_local = object.getString("fecha_local");
            magnitud = object.getDouble("magnitud");
            escala = object.getString("escala");
            profundidad = object.getDouble("profundidad");

            switch (object.getInt("sensible")) {
                case 0:
                    sensible = false;
                    break;
                case 1:
                    sensible = true;
                    break;
            }
            referencia = object.getString("referencia");
            //Guarda despues de 'DE' en la ciudad
            int inicio = referencia.indexOf("de") + 3;
            ciudad = referencia.substring(inicio, referencia.length());

            imagen_url = object.getString("imagen_url");
        } catch (JSONException e) {
            e.printStackTrace();
        }



        /*
            PREPARACION DE INTENT DESDE INFO EN PHP
         */
        Intent intent = new Intent(this, QuakeDetailsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        Bundle b = new Bundle();

        b.putString("titulo", titulo);
        b.putString("descripcion", descripcion);

        b.putString("ciudad", ciudad);
        b.putString("fecha_local", fecha_local);
        b.putDouble("magnitud", magnitud);
        b.putString("escala", escala);
        b.putBoolean("sensible", sensible);
        b.putDouble("profundidad", profundidad);
        b.putString("referencia", referencia);
        b.putString("imagen_url", imagen_url);

        intent.putExtras(b);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, getString(R.string.FIREBASE_CHANNEL_ID))
                .setSmallIcon(R.mipmap.ic_launcher_chile)
                .setContentTitle(titulo)
                .setContentText(descripcion)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);


        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(Integer.parseInt(getString(R.string.FIREBASE_CHANNEL_ID)), builder.build());


    }

    public static void createNotificationChannel(Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            //Definicion de atributos de canal de notificacion
            String name = context.getString(R.string.FIREBASE_CHANNEL_NAME);
            String description = context.getString(R.string.FIREBASE_CHANNEL_DESCRIPTION);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel notificationChannel = new NotificationChannel(context.getString(R.string.FIREBASE_CHANNEL_ID), name, importance);
            notificationChannel.setDescription(description);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(R.color.colorAccent);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel);

            Log.d(context.getString(R.string.TAG_FIREBASE_CHANNEL), context.getString(R.string.FIREBASE_CHANNEL_CREATED_MESSAGE));
        }
    }

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        Log.d(getString(R.string.TAG_FIREBASE_TOKEN), "Refreshed Token:" + s);
    }

    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();
    }
}
