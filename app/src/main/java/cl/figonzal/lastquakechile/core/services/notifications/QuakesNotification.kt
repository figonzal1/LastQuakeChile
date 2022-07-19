package cl.figonzal.lastquakechile.core.services.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat.BigTextStyle
import androidx.core.app.NotificationCompat.Builder
import cl.figonzal.lastquakechile.R
import cl.figonzal.lastquakechile.core.utils.SharedPrefUtil
import cl.figonzal.lastquakechile.core.utils.notification
import cl.figonzal.lastquakechile.quake_feature.data.local.entity.CoordinateEntity
import cl.figonzal.lastquakechile.quake_feature.data.local.entity.QuakeEntity
import cl.figonzal.lastquakechile.quake_feature.data.local.entity.relation.QuakeAndCoordinate
import cl.figonzal.lastquakechile.quake_feature.ui.QuakeDetailsActivity
import com.google.android.gms.tasks.Task
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.RemoteMessage
import timber.log.Timber
import java.io.Serializable

interface NotificationService {
    fun createChannel()
    fun showNotification(remoteMessage: RemoteMessage)
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

        val name = context.getString(R.string.firebase_channel_name_quakes)
        val description = context.getString(R.string.firebase_channel_description_quakes)

        val importance = NotificationManager.IMPORTANCE_HIGH

        context.getSystemService(NotificationManager::class.java).apply {

            createNotificationChannel(
                NotificationChannel(
                    context.getString(R.string.firebase_channel_id_quakes),
                    name,
                    importance
                ).apply {
                    this.description = description
                    this.importance = NotificationManager.IMPORTANCE_HIGH
                    this.enableLights(true)
                    this.lightColor = R.color.colorSecondary
                }
            )
        }


        Timber.d(context.getString(R.string.FIREBASE_CHANNEL_CREATED_MESSAGE))
        crashlytics.setCustomKey(context.getString(R.string.firebase_channel_status), true)
    }

    /**
     * Function that checks subscriptions to quake channels alerts
     *
     * @param isSubscribed
     */
    fun subscribedToQuakes(isSubscribed: Boolean) {

        with(FirebaseMessaging.getInstance()) {

            when {
                isSubscribed -> this.subscribeToTopic(context.getString(R.string.firebase_topic_name))
                    .addOnCompleteListener { task: Task<Void?> ->
                        when {
                            task.isSuccessful -> {

                                with(true) {
                                    sharedPrefUtil.saveData(
                                        context.getString(R.string.firebase_pref_key),
                                        this
                                    )

                                    Timber.d(context.getString(R.string.FIREBASE_SUB_OK))
                                    crashlytics.setCustomKey(
                                        context.getString(R.string.subsqribed_quake),
                                        this
                                    )
                                }
                            }
                        }
                    }
                else -> this.unsubscribeFromTopic(context.getString(R.string.firebase_topic_name))
                    .addOnCompleteListener { task: Task<Void> ->

                        when {
                            task.isSuccessful -> {

                                with(false) {
                                    sharedPrefUtil.saveData(
                                        context.getString(R.string.firebase_pref_key),
                                        this
                                    )

                                    Timber.d(context.getString(R.string.FIREBASE_SUB_DELETE))
                                    crashlytics.setCustomKey(
                                        context.getString(R.string.subsqribed_quake),
                                        this
                                    )
                                }
                            }
                        }
                    }
                    .addOnFailureListener {
                        Timber.d(
                            context.getString(R.string.FIREBASE_SUB_ALREADY)
                        )
                    }
            }
        }
    }

    /**
     * Show notification function from own server
     *
     * @remoteMessage: RemoteMessage with quake data
     */
    override fun showNotification(remoteMessage: RemoteMessage) {

        //Get data from php file in lqch-server
        with(remoteMessage.data) {

            val data = handleFcmData(this)

            Intent(context, QuakeDetailsActivity::class.java).apply {
                addFlags(FLAG_ACTIVITY_CLEAR_TOP)
                putExtra(context.getString(R.string.INTENT_TITULO), data[0].toString())
                putExtra(context.getString(R.string.INTENT_DESCRIPCION), data[1].toString())
                putExtra(context.getString(R.string.INTENT_QUAKE), data[2] as Serializable)

            }.also { intent ->

                PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE).run {
                    context.notification(data, this)
                }

                Timber.d(context.getString(R.string.TRY_INTENT_NOTIFICATION_1))
                crashlytics.setCustomKey(context.getString(R.string.try_intent_notification), true)
            }
        }
    }

    /**
     * Show notification function from FCM (Generic)
     *
     * @remoteMessage: RemoteMessage with FCM
     */
    fun showNotificationGeneric(remoteMessage: RemoteMessage) {

        Builder(
            context,
            context.getString(R.string.firebase_channel_id_quakes)
        ).setContentTitle(remoteMessage.notification?.title)
            .setContentText(remoteMessage.notification?.body)
            .setStyle(BigTextStyle().bigText(remoteMessage.notification?.body))
            .setSmallIcon(R.drawable.ic_lastquakechile_400)
            .setAutoCancel(true)
            .also {
                (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify(
                    context.getString(R.string.firebase_channel_id_quakes).toInt(),
                    it.build()
                )
            }
    }

    /**
     * Fill data to objects with data from notification
     */
    private fun handleFcmData(mutableMap: MutableMap<String, String>): ArrayList<Any> {

        with(mutableMap) {
            val title = getValue(context.getString(R.string.INTENT_TITULO))
            val description = getValue(context.getString(R.string.INTENT_DESCRIPCION))

            val coordinate = CoordinateEntity(
                latitude = getValue(context.getString(R.string.INTENT_LATITUD)).toDouble(),
                longitude = getValue(context.getString(R.string.INTENT_LONGITUD)).toDouble()
            )


            val quake = QuakeEntity(
                quakeCode = getValue(context.getString(R.string.INTENT_QUAKE_CODE)).toInt(),
                utcDate = getValue(context.getString(R.string.INTENT_FECHA_UTC)),
                city = getValue(context.getString(R.string.INTENT_CIUDAD)),
                reference = getValue(context.getString(R.string.INTENT_REFERENCIA)),
                magnitude = getValue(context.getString(R.string.INTENT_MAGNITUD)).toDouble(),
                scale = getValue(context.getString(R.string.INTENT_ESCALA)),
                depth = getValue(context.getString(R.string.INTENT_PROFUNDIDAD)).toDouble(),
                isVerified = getValue(context.getString(R.string.INTENT_ESTADO)).toBoolean(),
                isSensitive = getValue(context.getString(R.string.INTENT_SENSIBLE)).toBoolean()
            )

            val quakeEntityAndCoordinate = QuakeAndCoordinate(
                quake, coordinate
            ).toDomain()

            return arrayListOf(title, description, quakeEntityAndCoordinate)
        }
    }
}