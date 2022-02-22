package cl.figonzal.lastquakechile.core.services.notifications

import cl.figonzal.lastquakechile.R
import cl.figonzal.lastquakechile.core.utils.SharedPrefUtil
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import timber.log.Timber

class FirebaseQuakeNotificationService : FirebaseMessagingService() {

    private lateinit var crashlytics: FirebaseCrashlytics
    private var quakesNotification: QuakesNotification? = null

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
        Timber.d("From: %s", remoteMessage.from)

        //Si es notificacion con datos de sismos
        if (remoteMessage.data.isNotEmpty()) {

            Timber.d("Message data payload: %s", remoteMessage.data)
            crashlytics.setCustomKey(getString(R.string.firebase_msg_data_status), true)

            quakesNotification?.showNotification(remoteMessage)
        }

        //Si es notificacion desde consola FCM
        if (remoteMessage.notification != null) {
            Timber.d(
                "Message notification: " + remoteMessage.notification!!
                    .title + " - " + remoteMessage.notification!!.body
            )
            crashlytics.setCustomKey(
                getString(R.string.firebase_msg_notification_status),
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
        Timber.d("Refresh token: %s", s)
        crashlytics.setUserId(s)
    }

    override fun onDeletedMessages() {
        super.onDeletedMessages()
    }
}