package cl.figonzal.lastquakechile.core.services.notifications

import com.google.firebase.messaging.RemoteMessage

interface QuakeNotification {
    fun createChannel()
    fun handleQuakeNotification(remoteMessage: RemoteMessage)
}
