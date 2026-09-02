package cl.figonzal.lastquakechile.core.services.notifications

import cl.figonzal.lastquakechile.core.services.notifications.utils.FIREBASE_MSG_GENERIC
import cl.figonzal.lastquakechile.core.services.notifications.utils.FIREBASE_MSG_QUAKE_DATA
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.koin.android.ext.android.inject
import timber.log.Timber


class FCMService : FirebaseMessagingService() {

    private val notificationServiceImpl: QuakeNotificationImpl by inject()
    private val crashlytics = FirebaseCrashlytics.getInstance()

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Timber.d("From: ${remoteMessage.from}")

        if (remoteMessage.data.isNotEmpty()) {
            Timber.d("Message quake data payload: ${remoteMessage.data}")
            crashlytics.setCustomKey(FIREBASE_MSG_QUAKE_DATA, "Received")
            notificationServiceImpl.handleQuakeNotification(remoteMessage)
        }

        if (remoteMessage.notification != null) {
            Timber.d("Message notification: ${remoteMessage.notification?.title} - ${remoteMessage.notification?.body}")
            crashlytics.setCustomKey(FIREBASE_MSG_GENERIC, "Received")
            notificationServiceImpl.handleNotificationGeneric(remoteMessage)
        }
    }

    override fun onRegistered(installationId: String) {
        super.onRegistered(installationId)
        Timber.d("Registered with FID: %s", installationId)
        crashlytics.setUserId(installationId)
    }
}
