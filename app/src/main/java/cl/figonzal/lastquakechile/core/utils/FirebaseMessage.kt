package cl.figonzal.lastquakechile.core.utils

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import androidx.core.app.NotificationCompat
import cl.figonzal.lastquakechile.R
import cl.figonzal.lastquakechile.quake_feature.domain.model.Quake
import com.google.android.gms.tasks.Task
import com.google.firebase.messaging.FirebaseMessaging
import timber.log.Timber


fun getFirebaseToken() {

    //FIREBASE SECTION
    FirebaseMessaging.getInstance().token
        .addOnCompleteListener { task: Task<String?> ->
            if (!task.isSuccessful) {
                Timber.w("Fetching FCM registration token failed")
                return@addOnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result
            Timber.d("Token %s", token)
        }
}

fun Context.showNotification(
    title: String,
    description: String,
    quake: Quake,
    pendingIntent: PendingIntent
) {

    NotificationCompat.Builder(
        this,
        this.getString(R.string.firebase_channel_id_quakes)
    ).setSmallIcon(R.drawable.ic_lastquakechile_400)
        .setContentTitle(title)
        .setContentText(description)
        .setStyle(NotificationCompat.BigTextStyle().bigText(description))
        .setPriority(NotificationCompat.PRIORITY_MAX)
        .setAutoCancel(true)
        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        .setContentIntent(pendingIntent)
        .addAction(
            R.drawable.ic_quakes_24dp,
            getString(R.string.view_quake_notification_button),
            pendingIntent
        )
        .run {

            //Notify
            (this@showNotification.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify(
                quake.quakeCode,
                build()
            )
        }
}