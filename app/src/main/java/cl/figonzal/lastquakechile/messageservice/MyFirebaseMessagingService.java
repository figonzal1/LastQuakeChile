package cl.figonzal.lastquakechile.messageservice;

import android.app.NotificationManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Objects;

import cl.figonzal.lastquakechile.R;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.d(getString(R.string.TAG_FIREBASE_MESSAGE), "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(getString(R.string.TAG_FIREBASE_MESSAGE), "Message data payload: " + remoteMessage.getData());
        }

        if (remoteMessage.getNotification() != null) {
            Log.d(getString(R.string.TAG_FIREBASE_MESSAGE), "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }

        showNotification(remoteMessage);
    }

    private void showNotification(RemoteMessage remoteMessage) {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, getString(R.string.FIREBASE_CHANNEL_ID))
                .setSmallIcon(R.mipmap.ic_launcher_chile)
                .setContentTitle(Objects.requireNonNull(remoteMessage.getNotification()).getTitle())
                .setContentText(remoteMessage.getNotification().getBody())
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(Integer.parseInt(getString(R.string.FIREBASE_CHANNEL_ID)), builder.build());


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
