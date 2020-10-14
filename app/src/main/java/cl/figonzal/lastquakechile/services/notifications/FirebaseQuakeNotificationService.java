package cl.figonzal.lastquakechile.services.notifications;

import androidx.annotation.NonNull;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import cl.figonzal.lastquakechile.R;
import cl.figonzal.lastquakechile.services.SharedPrefService;
import timber.log.Timber;

public class FirebaseQuakeNotificationService extends FirebaseMessagingService {

    private FirebaseCrashlytics crashlytics;
    private QuakesNotification quakesNotification;

    //Esta clase no puede tener contructor
    //REF: https://firebase.google.com/docs/cloud-messaging?hl=es

    @Override
    public void onCreate() {
        super.onCreate();

        crashlytics = FirebaseCrashlytics.getInstance();
        quakesNotification = new QuakesNotification(getApplicationContext(), new SharedPrefService(getApplicationContext()));
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Timber.i("From: %s", remoteMessage.getFrom());

        //Si es notificacion con datos de sismos
        if (remoteMessage.getData().size() > 0) {

            Timber.i("Message data payload: %s", remoteMessage.getData());
            crashlytics.setCustomKey(getString(R.string.FIREBASE_MESSAGE_DATA_STATUS), true);

            quakesNotification.setRemoteMsg(remoteMessage);
            quakesNotification.showNotification();
        }

        //Si es notificacion desde consola FCM
        if (remoteMessage.getNotification() != null) {

            Timber.i("Message notification: " + remoteMessage.getNotification().getTitle() + " - " + remoteMessage.getNotification().getBody());
            crashlytics.setCustomKey(getString(R.string.FIREBASE_MESSAGE_NOTIFICATION_STATUS), true);

            quakesNotification.showNotificationGeneric(remoteMessage);
        }
    }

    /**
     * Funcion que muestra notificacion desde PHP
     */
    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);

        Timber.i("Refresh token: %s", s);
        crashlytics.setUserId(s);
    }

    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();
    }
}

