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
            applicationContext, SharedPrefUtil(applicationContext)
        )
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Timber.d("From: ${remoteMessage.from}")

        //Notification with quake data
        if (remoteMessage.data.isNotEmpty()) {

            Timber.d("Message data payload: ${remoteMessage.data}")
            crashlytics.setCustomKey(getString(R.string.firebase_msg_data_status), true)

            quakesNotification?.handleQuakeNotification(remoteMessage)
        }

        //Notification from FCM
        if (remoteMessage.notification != null) {
            Timber.d("Message notification: ${remoteMessage.notification!!.title} - ${remoteMessage.notification!!.body}")
            crashlytics.setCustomKey(getString(R.string.firebase_msg_notification_status), true)
            quakesNotification?.handleNotificationGeneric(remoteMessage)
        }
    }

    override fun onNewToken(s: String) {
        super.onNewToken(s)
        Timber.d("Refresh token: %s", s)
        crashlytics.setUserId(s)
    }

}