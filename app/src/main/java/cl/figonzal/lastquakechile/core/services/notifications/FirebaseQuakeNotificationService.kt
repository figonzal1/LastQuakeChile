package cl.figonzal.lastquakechile.core.services.notifications

import cl.figonzal.lastquakechile.core.utils.SharedPrefUtil
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import timber.log.Timber

private const val FIREBASE_MSG_QUAKE_DATA = "data_msg_received"
private const val FIREBASE_MSG_GENERIC = "generic_msg_received"

class FirebaseQuakeNotificationService : FirebaseMessagingService() {

    private lateinit var crashlytics: FirebaseCrashlytics
    private var quakesNotification: QuakesNotification? = null

    override fun onCreate() {
        super.onCreate()
        crashlytics = FirebaseCrashlytics.getInstance()
        quakesNotification = QuakesNotification(
            applicationContext, SharedPrefUtil(applicationContext)
        )
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Timber.d("From: ${remoteMessage.from}")

        //Notification with quake data
        if (remoteMessage.data.isNotEmpty()) {

            Timber.d("Message quake data payload: ${remoteMessage.data}")
            crashlytics.setCustomKey(FIREBASE_MSG_QUAKE_DATA, "Received")

            quakesNotification?.handleQuakeNotification(remoteMessage)
        }

        //Notification from FCM
        if (remoteMessage.notification != null) {
            Timber.d("Message notification: ${remoteMessage.notification?.title} - ${remoteMessage.notification?.body}")
            crashlytics.setCustomKey(FIREBASE_MSG_GENERIC, "Received")

            quakesNotification?.handleNotificationGeneric(remoteMessage)
        }
    }

    override fun onNewToken(s: String) {
        super.onNewToken(s)
        Timber.d("Refresh token: %s", s)
        crashlytics.setUserId(s)
    }

}