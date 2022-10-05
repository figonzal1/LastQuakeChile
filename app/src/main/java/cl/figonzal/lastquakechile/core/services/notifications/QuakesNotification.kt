package cl.figonzal.lastquakechile.core.services.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat.*
import androidx.core.app.TaskStackBuilder
import cl.figonzal.lastquakechile.R
import cl.figonzal.lastquakechile.core.utils.*
import cl.figonzal.lastquakechile.quake_feature.domain.model.Coordinate
import cl.figonzal.lastquakechile.quake_feature.domain.model.Quake
import cl.figonzal.lastquakechile.quake_feature.ui.QuakeDetailsActivity
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.messaging.RemoteMessage
import timber.log.Timber

interface NotificationService {
    fun createChannel()
    fun deleteChannel()
    fun recreateChannel()
    fun handleQuakeNotification(remoteMessage: RemoteMessage)
}

/**
 * NotificationService implementation
 */
class QuakesNotification(
    private val context: Context,
    private val sharedPrefUtil: SharedPrefUtil
) : NotificationService {

    private val crashlytics = FirebaseCrashlytics.getInstance()

    @RequiresApi(api = Build.VERSION_CODES.O)
    override fun createChannel() {

        val randomChannel = context.generateRandomChannelId(sharedPrefUtil)

        val name = context.getString(R.string.firebase_channel_name_quakes)
        val description = context.getString(R.string.firebase_channel_description_quakes)

        val highPriority = sharedPrefUtil.getData(
            context.getString(R.string.high_priority_pref_key),
            true
        ) as Boolean

        val importance = when {
            highPriority -> NotificationManager.IMPORTANCE_HIGH
            else -> NotificationManager.IMPORTANCE_DEFAULT
        }

        context.getSystemService(NotificationManager::class.java).apply {

            createNotificationChannel(
                NotificationChannel(
                    randomChannel.toString(),
                    name,
                    importance
                ).apply {
                    this.description = description
                    this.importance = importance
                    this.enableLights(true)
                    this.lightColor = R.color.colorSecondary
                }
            )
        }

        Timber.d(context.getString(R.string.FIREBASE_CHANNEL_CREATED))
        crashlytics.setCustomKey(context.getString(R.string.firebase_channel_status), true)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun deleteChannel() {

        val randomChannel =
            sharedPrefUtil.getData(context.getString(R.string.random_channel_key), 1)

        context.getSystemService(NotificationManager::class.java).apply {
            deleteNotificationChannel(randomChannel.toString())
        }

        Timber.d(context.getString(R.string.FIREBASE_CHANNEL_DELETED))
        crashlytics.setCustomKey(context.getString(R.string.firebase_channel_status), false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun recreateChannel() {
        deleteChannel()
        createChannel()
    }

    /**
     * Show notification function from own server
     *
     * @remoteMessage: RemoteMessage with quake data
     */
    override fun handleQuakeNotification(remoteMessage: RemoteMessage) {

        //Get data from php file in lqch-server
        with(remoteMessage.data) {

            var title: String?
            val description: String?

            val quake: Quake = handleFcmData(this)

            when {
                quake.magnitude >= 5.0 -> {

                    title = context.getString(R.string.alert_title_notification)
                    description = String.format(
                        context.getString(R.string.alert_description_notification),
                        quake.magnitude,
                        quake.reference
                    )

                    title = when {
                        !quake.isVerified -> String.format(
                            context.getString(R.string.preliminary_format_notification),
                            title
                        )
                        else -> String.format(
                            context.getString(R.string.verified_format_notification),
                            title
                        )
                    }
                }
                else -> {
                    title = String.format(
                        context.getString(R.string.no_alert_title_notification),
                        quake.magnitude
                    )
                    description = String.format(
                        context.getString(R.string.no_alert_description_notification),
                        quake.reference
                    )

                    when {
                        !quake.isVerified -> title = String.format(
                            context.getString(R.string.preliminary_format_notification),
                            title
                        )
                    }
                }
            }


            val intent = Intent(context, QuakeDetailsActivity::class.java).apply {
                putExtra(context.getString(R.string.INTENT_QUAKE), quake)
            }

            TaskStackBuilder.create(context).run {
                addNextIntentWithParentStack(intent)
                getPendingIntent(
                    0,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            }?.also {
                showNotification(title, description, quake, it)
            }

            Timber.d(context.getString(R.string.TRY_INTENT_NOTIFICATION_1))
            crashlytics.setCustomKey(context.getString(R.string.try_intent_notification), true)
        }
    }

    private fun showNotification(
        title: String,
        description: String,
        quake: Quake,
        pendingIntent: PendingIntent
    ) {

        val highPriority = sharedPrefUtil.getData(
            context.getString(R.string.high_priority_pref_key),
            true
        ) as Boolean

        val randomChannel =
            sharedPrefUtil.getData(context.getString(R.string.random_channel_key), 1) as Int

        Timber.d("high_priority_notifications: $highPriority")
        crashlytics.setCustomKey(context.getString(R.string.high_priority_pref_key), highPriority)

        val preliminaryNotifications: Boolean =
            sharedPrefUtil.getData(
                context.getString(R.string.quake_preliminary_pref),
                true
            ) as Boolean

        Builder(
            context,
            randomChannel.toString()
        ).setSmallIcon(R.drawable.ic_lastquakechile_400)
            .setContentTitle(title)
            .setContentText(description)
            .setStyle(BigTextStyle().bigText(description))
            .setPriority(
                when (highPriority) {
                    true -> PRIORITY_MAX
                    else -> PRIORITY_DEFAULT
                }
            )
            .setAutoCancel(true)
            .setVisibility(VISIBILITY_PUBLIC)
            .setContentIntent(pendingIntent)
            .addAction(
                R.drawable.ic_quakes_24dp,
                context.getString(R.string.view_quake_notification_button),
                pendingIntent
            )
            .run {

                if (quake.isVerified || preliminaryNotifications) {
                    //Notify
                    (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify(
                        quake.quakeCode,
                        build()
                    )
                }
            }
    }

    /**
     * Show notification function from FCM (Generic)
     *
     * @remoteMessage: RemoteMessage with FCM
     */
    fun handleNotificationGeneric(remoteMessage: RemoteMessage) {

        val randomChannel =
            sharedPrefUtil.getData(context.getString(R.string.random_channel_key), 1) as Int

        Builder(
            context,
            randomChannel.toString()
        ).setContentTitle(remoteMessage.notification?.title)
            .setContentText(remoteMessage.notification?.body)
            .setStyle(BigTextStyle().bigText(remoteMessage.notification?.body))
            .setSmallIcon(R.drawable.ic_lastquakechile_400)
            .setAutoCancel(true)
            .setVisibility(VISIBILITY_PUBLIC)
            .also {
                (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify(
                    randomChannel,
                    it.build()
                )
            }
    }

    /**
     * Fill data to objects with data from notification
     */
    private fun handleFcmData(map: Map<String, String>): Quake {

        with(map) {

            val coordinate = Coordinate(
                latitude = getValue(context.getString(R.string.INTENT_LATITUD)).toDouble(),
                longitude = getValue(context.getString(R.string.INTENT_LONGITUD)).toDouble()
            )

            val localDate = getValue(context.getString(R.string.INTENT_FECHA_UTC))
                .stringToLocalDateTime()
                .utcToLocalDate()
                .localDateTimeToString()


            return Quake(
                quakeCode = getValue(context.getString(R.string.INTENT_QUAKE_CODE)).toInt(),
                localDate = localDate,
                city = getValue(context.getString(R.string.INTENT_CIUDAD)),
                reference = getValue(context.getString(R.string.INTENT_REFERENCIA)),
                magnitude = getValue(context.getString(R.string.INTENT_MAGNITUD)).toDouble(),
                scale = getValue(context.getString(R.string.INTENT_ESCALA)),
                depth = getValue(context.getString(R.string.INTENT_PROFUNDIDAD)).toDouble(),
                isVerified = getValue(context.getString(R.string.INTENT_ESTADO)).toBoolean(),
                isSensitive = getValue(context.getString(R.string.INTENT_SENSIBLE)).toBoolean(),
                coordinate = coordinate
            )
        }
    }
}