package cl.figonzal.lastquakechile.core.services.notifications

import cl.figonzal.lastquakechile.core.services.notifications.utils.FIREBASE_MSG_GENERIC
import cl.figonzal.lastquakechile.core.services.notifications.utils.FIREBASE_MSG_QUAKE_DATA
import cl.figonzal.lastquakechile.core.utils.SharedPrefUtil
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import timber.log.Timber


class FCMService : FirebaseMessagingService() {

    private lateinit var crashlytics: FirebaseCrashlytics
    private var notificationServiceImpl: QuakeNotificationImpl? = null

    override fun onCreate() {
        super.onCreate()
        crashlytics = FirebaseCrashlytics.getInstance()
        notificationServiceImpl = QuakeNotificationImpl(
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

            notificationServiceImpl?.handleQuakeNotification(remoteMessage)
        }

        //Notification from FCM
        if (remoteMessage.notification != null) {
            Timber.d("Message notification: ${remoteMessage.notification?.title} - ${remoteMessage.notification?.body}")
            crashlytics.setCustomKey(FIREBASE_MSG_GENERIC, "Received")

            notificationServiceImpl?.handleNotificationGeneric(remoteMessage)
        }
    }

    override fun onNewToken(s: String) {
        super.onNewToken(s)
        Timber.d("Refresh token: %s", s)
        crashlytics.setUserId(s)
    }

}