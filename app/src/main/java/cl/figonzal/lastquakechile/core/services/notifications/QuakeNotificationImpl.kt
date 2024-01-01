package cl.figonzal.lastquakechile.core.services.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat.BigTextStyle
import androidx.core.app.NotificationCompat.Builder
import androidx.core.app.NotificationCompat.VISIBILITY_PUBLIC
import androidx.core.app.TaskStackBuilder
import cl.figonzal.lastquakechile.R
import cl.figonzal.lastquakechile.core.services.notifications.utils.CITY
import cl.figonzal.lastquakechile.core.services.notifications.utils.DEPTH
import cl.figonzal.lastquakechile.core.services.notifications.utils.FIREBASE_CHANNEL_STATUS
import cl.figonzal.lastquakechile.core.services.notifications.utils.IS_SENSIBLE
import cl.figonzal.lastquakechile.core.services.notifications.utils.IS_UPDATE
import cl.figonzal.lastquakechile.core.services.notifications.utils.LATITUDE
import cl.figonzal.lastquakechile.core.services.notifications.utils.LONGITUDE
import cl.figonzal.lastquakechile.core.services.notifications.utils.MAGNITUDE
import cl.figonzal.lastquakechile.core.services.notifications.utils.QUAKE
import cl.figonzal.lastquakechile.core.services.notifications.utils.QUAKE_CODE
import cl.figonzal.lastquakechile.core.services.notifications.utils.RANDOM_CHANNEL_ID
import cl.figonzal.lastquakechile.core.services.notifications.utils.REFERENCE
import cl.figonzal.lastquakechile.core.services.notifications.utils.ROOT_PREF_HIGH_PRIORITY_NOTIFICATION
import cl.figonzal.lastquakechile.core.services.notifications.utils.ROOT_PREF_QUAKE_PRELIMINARY
import cl.figonzal.lastquakechile.core.services.notifications.utils.SCALE
import cl.figonzal.lastquakechile.core.services.notifications.utils.STATE
import cl.figonzal.lastquakechile.core.services.notifications.utils.UTC_DATE
import cl.figonzal.lastquakechile.core.services.notifications.utils.generateRandomChannelId
import cl.figonzal.lastquakechile.core.services.notifications.utils.getChannelImportance
import cl.figonzal.lastquakechile.core.services.notifications.utils.getMinMagnitude
import cl.figonzal.lastquakechile.core.services.notifications.utils.getNotificationPriority
import cl.figonzal.lastquakechile.core.services.notifications.utils.getPreliminaryAlertsStatus
import cl.figonzal.lastquakechile.core.services.notifications.utils.getRandomChannel
import cl.figonzal.lastquakechile.core.services.notifications.utils.greaterThan
import cl.figonzal.lastquakechile.core.utils.SharedPrefUtil
import cl.figonzal.lastquakechile.core.utils.localDateTimeToString
import cl.figonzal.lastquakechile.core.utils.stringToLocalDateTime
import cl.figonzal.lastquakechile.core.utils.utcToLocalDate
import cl.figonzal.lastquakechile.quake_feature.domain.model.Coordinate
import cl.figonzal.lastquakechile.quake_feature.domain.model.Quake
import cl.figonzal.lastquakechile.quake_feature.ui.QuakeDetailsActivity
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.messaging.RemoteMessage
import timber.log.Timber

/**
 * NotificationService implementation
 */
class QuakeNotificationImpl(
    private val context: Context,
    private val sharedPrefUtil: SharedPrefUtil
) : QuakeNotification {

    private val crashlytics = FirebaseCrashlytics.getInstance()

    @RequiresApi(api = Build.VERSION_CODES.O)
    override fun createChannel() {

        val randomChannel = generateRandomChannelId(sharedPrefUtil, RANDOM_CHANNEL_ID)

        val name = context.getString(R.string.firebase_channel_name_quakes)
        val description = context.getString(R.string.firebase_channel_description_quakes)

        val importance =
            getChannelImportance(sharedPrefUtil, ROOT_PREF_HIGH_PRIORITY_NOTIFICATION, crashlytics)

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

                    Timber.d("Notification channel created")
                    crashlytics.setCustomKey(FIREBASE_CHANNEL_STATUS, "Created")
                }
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun deleteChannel() {

        val randomChannel = getRandomChannel(sharedPrefUtil, RANDOM_CHANNEL_ID)

        context.getSystemService(NotificationManager::class.java).apply {
            deleteNotificationChannel(randomChannel.toString())

            Timber.d("Notification channel deleted")
            crashlytics.setCustomKey(FIREBASE_CHANNEL_STATUS, "Deleted")
        }
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

            val isUpdate = this.getValue(IS_UPDATE).toBoolean()

            val quake: Quake = handleFcmData(this)

            when {
                quake.magnitude >= 5.0 -> {

                    title = context.getString(R.string.alert_title_notification)
                    description = String.format(
                        context.getString(R.string.alert_description_notification),
                        quake.magnitude,
                        quake.reference
                    )
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
                }
            }

            title = when {
                !quake.isVerified -> String.format(
                    context.getString(R.string.preliminary_format_notification),
                    title
                )

                isUpdate -> String.format(
                    context.getString(R.string.verified_format_notification),
                    title
                )

                else -> title
            }

            val intent = Intent(context, QuakeDetailsActivity::class.java).apply {
                putExtra(QUAKE, quake)
            }

            //Create fake backStack
            TaskStackBuilder.create(context).run {
                addNextIntentWithParentStack(intent)
                getPendingIntent(
                    0,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            }?.also {
                showQuakeNotification(title, description, quake, it)
            }
        }
    }

    private fun showQuakeNotification(
        title: String,
        description: String,
        quake: Quake,
        pendingIntent: PendingIntent
    ) {

        val randomChannel = getRandomChannel(sharedPrefUtil, RANDOM_CHANNEL_ID)

        val preliminaryNotifications = getPreliminaryAlertsStatus(
            sharedPrefUtil, ROOT_PREF_QUAKE_PRELIMINARY, crashlytics
        )

        val priority =
            getNotificationPriority(
                sharedPrefUtil,
                ROOT_PREF_HIGH_PRIORITY_NOTIFICATION,
                crashlytics
            )

        val minMagnitude: String =
            getMinMagnitude(
                sharedPrefUtil,
                context.getString(R.string.min_magnitude_alert_key),
                crashlytics
            )

        Builder(
            context,
            randomChannel.toString()
        ).setSmallIcon(R.drawable.lastquakechile_400)
            .setContentTitle(title)
            .setContentText(description)
            .setStyle(BigTextStyle().bigText(description))
            .setPriority(priority)
            .setAutoCancel(true)
            .setVisibility(VISIBILITY_PUBLIC)
            .setContentIntent(pendingIntent)
            .addAction(
                R.drawable.quakes_24dp,
                context.getString(R.string.view_quake_notification_button),
                pendingIntent
            )
            .run {

                if (quake.greaterThan(minMagnitude) && (quake.isVerified || preliminaryNotifications)) {
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

        val randomChannel = getRandomChannel(sharedPrefUtil, RANDOM_CHANNEL_ID)

        Builder(
            context,
            randomChannel.toString()
        ).setContentTitle(remoteMessage.notification?.title)
            .setContentText(remoteMessage.notification?.body)
            .setStyle(BigTextStyle().bigText(remoteMessage.notification?.body))
            .setSmallIcon(R.drawable.lastquakechile_400)
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
                latitude = getValue(LATITUDE).toDouble(),
                longitude = getValue(LONGITUDE).toDouble()
            )

            val localDate = getValue(UTC_DATE)
                .stringToLocalDateTime()
                .utcToLocalDate()
                .localDateTimeToString()


            return Quake(
                quakeCode = getValue(QUAKE_CODE).toInt(),
                localDate = localDate,
                city = getValue(CITY),
                reference = getValue(REFERENCE),
                magnitude = getValue(MAGNITUDE).toDouble(),
                scale = getValue(SCALE),
                depth = getValue(DEPTH).toDouble(),
                isVerified = getValue(STATE).toBoolean(),
                isSensitive = getValue(IS_SENSIBLE).toBoolean(),
                coordinate = coordinate
            )
        }
    }
}