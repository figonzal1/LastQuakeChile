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

fun Context.notification(data: List<Any>, pendingIntent: PendingIntent) {

    NotificationCompat.Builder(
        this,
        this.getString(R.string.firebase_channel_id_quakes)
    ).setSmallIcon(R.drawable.ic_lastquakechile_400)
        .setContentTitle(data[0].toString())
        .setContentText(data[1].toString())
        .setStyle(NotificationCompat.BigTextStyle().bigText(data[1].toString()))
        .setPriority(NotificationCompat.PRIORITY_MAX)
        .setAutoCancel(true)
        .setContentIntent(pendingIntent)
        .run {

            val quake = data[2] as Quake

            //Notify
            (this@notification.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify(
                quake.quakeCode.toInt(),
                build()
            )
        }
}