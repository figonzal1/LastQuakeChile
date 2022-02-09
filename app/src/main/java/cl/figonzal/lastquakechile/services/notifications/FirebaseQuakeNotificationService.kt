package cl.figonzal.lastquakechile.services.notifications

import cl.figonzal.lastquakechile.R
import cl.figonzal.lastquakechile.core.utils.SharedPrefUtil
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import timber.log.Timber

class FirebaseQuakeNotificationService : FirebaseMessagingService() {

    private lateinit var crashlytics: FirebaseCrashlytics
    private var quakesNotification: QuakesNotification? = null

    //Esta clase no puede tener contructor
    //REF: https://firebase.google.com/docs/cloud-messaging?hl=es
    override fun onCreate() {
        super.onCreate()
        crashlytics = FirebaseCrashlytics.getInstance()
        quakesNotification = QuakesNotification(
            applicationContext, SharedPrefUtil(
                applicationContext
            )
        )
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Timber.i("From: %s", remoteMessage.from)

        //Si es notificacion con datos de sismos
        if (remoteMessage.data.isNotEmpty()) {

            Timber.i("Message data payload: %s", remoteMessage.data)
            crashlytics.setCustomKey(getString(R.string.FIREBASE_MESSAGE_DATA_STATUS), true)

            quakesNotification?.showNotification(remoteMessage)
        }

        //Si es notificacion desde consola FCM
        if (remoteMessage.notification != null) {
            Timber.i(
                "Message notification: " + remoteMessage.notification!!
                    .title + " - " + remoteMessage.notification!!.body
            )
            crashlytics.setCustomKey(
                getString(R.string.FIREBASE_MESSAGE_NOTIFICATION_STATUS),
                true
            )
            quakesNotification?.showNotificationGeneric(remoteMessage)
        }
    }

    /**
     * Funcion que muestra notificacion desde PHP
     */
    override fun onNewToken(s: String) {
        super.onNewToken(s)
        Timber.i("Refresh token: %s", s)
        crashlytics.setUserId(s)
    }
}