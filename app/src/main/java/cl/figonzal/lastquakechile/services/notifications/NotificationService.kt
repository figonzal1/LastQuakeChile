package cl.figonzal.lastquakechile.services.notifications

import com.google.firebase.messaging.RemoteMessage

interface NotificationService {
    fun createChannel()
    fun showNotification(remoteMessage: RemoteMessage)
}