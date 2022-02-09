package cl.figonzal.lastquakechile.services.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat.*
import cl.figonzal.lastquakechile.R
import cl.figonzal.lastquakechile.core.utils.SharedPrefUtil
import cl.figonzal.lastquakechile.views.activities.QuakeDetailsActivity
import com.google.android.gms.tasks.Task
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.RemoteMessage
import org.json.JSONException
import timber.log.Timber
import java.util.*

/**
 * Notificaciones de sismos con implementacion de Firebase
 */
class QuakesNotification(private val context: Context, private val sharedPrefUtil: SharedPrefUtil) :
    NotificationService {

    private val crashlytics: FirebaseCrashlytics = FirebaseCrashlytics.getInstance()

    @RequiresApi(api = Build.VERSION_CODES.O)
    override fun createChannel() {

        //Definicion de atributos de canal de notificacion
        val name = context.getString(R.string.FIREBASE_CHANNEL_NAME_QUAKES)
        val description = context.getString(R.string.FIREBASE_CHANNEL_DESCRIPTION_QUAKES)

        val importance = NotificationManager.IMPORTANCE_HIGH

        context.getSystemService(NotificationManager::class.java).apply {

            createNotificationChannel(
                NotificationChannel(
                    context.getString(R.string.FIREBASE_CHANNEL_ID_QUAKES),
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


        Timber.i(context.getString(R.string.FIREBASE_CHANNEL_CREATED_MESSAGE))
        crashlytics.setCustomKey(context.getString(R.string.FIREBASE_CHANNEL_STATUS), true)
    }

    /**
     * Funcion encargada de checkear la suscripcion del usuario al canal de alertas de sismos
     *
     * @param subcribed Boleano para determinar suscripcion a tema
     */
    fun suscribedToQuakes(subcribed: Boolean) {


        with(FirebaseMessaging.getInstance()) {
            //Suscribir a tema quakes
            //Eliminacion de la suscripcion
            when {
                subcribed -> this.subscribeToTopic(context.getString(R.string.FIREBASE_TOPIC_NAME))
                    .addOnCompleteListener { task: Task<Void?> ->
                        when {
                            task.isSuccessful -> {
                                //Modificar valor en sharepref de settings
                                sharedPrefUtil.saveData(
                                    context.getString(R.string.FIREBASE_PREF_KEY),
                                    true
                                )

                                Timber.i(context.getString(R.string.TAG_FIREBASE_SUSCRIPTION_OK))
                                crashlytics.setCustomKey(
                                    context.getString(R.string.SUSCRITO_QUAKE),
                                    true
                                )
                            }
                        }
                    }
                else -> this.unsubscribeFromTopic(context.getString(R.string.FIREBASE_TOPIC_NAME))
                    .addOnCompleteListener { task: Task<Void> ->

                        when {
                            task.isSuccessful -> {
                                //Modificar valor en sharepref de settings
                                sharedPrefUtil.saveData(
                                    context.getString(R.string.FIREBASE_PREF_KEY),
                                    false
                                )

                                //LOG ZONE
                                Timber.i(context.getString(R.string.TAG_FIREBASE_SUSCRIPTION_DELETE))
                                crashlytics.setCustomKey(
                                    context.getString(R.string.SUSCRITO_QUAKE),
                                    false
                                )
                            }
                        }

                    }
                    .addOnFailureListener {
                        Timber.i(
                            context.getString(R.string.TAG_FIREBASE_SUSCRIPTION_ALREADY)
                        )
                    }
            }
        }

    }

    /**
     * Funcion que muestra notificacion de FCM
     */
    override fun showNotification(remoteMessage: RemoteMessage) {

        //Obtener datos desde send_notification.php en servidor
        val mParams: MutableMap<String, String> = remoteMessage.data

        val title: String
        val description: String
        val city: String
        val utcDate: String
        val status: String
        val latitude: String
        val longitude: String
        val magnitude: Double
        val scale: String
        val depth: Double
        val isSensitive: String
        val reference: String
        val imgUrl: String
        try {

            with(mParams) {
                title = getValue(context.getString(R.string.INTENT_TITULO))
                description = getValue(context.getString(R.string.INTENT_DESCRIPCION))
                utcDate = getValue(context.getString(R.string.INTENT_FECHA_UTC))
                city = getValue(context.getString(R.string.INTENT_CIUDAD))
                reference = getValue(context.getString(R.string.INTENT_REFERENCIA))
                latitude = getValue(context.getString(R.string.INTENT_LATITUD))
                longitude = getValue(context.getString(R.string.INTENT_LONGITUD))
                magnitude = getValue(context.getString(R.string.INTENT_MAGNITUD)).toDouble()
                scale = getValue(context.getString(R.string.INTENT_ESCALA))
                depth = getValue(context.getString(R.string.INTENT_PROFUNDIDAD)).toDouble()
                status = getValue(context.getString(R.string.INTENT_ESTADO))
                isSensitive = getValue(context.getString(R.string.INTENT_SENSIBLE))
                imgUrl = getValue(context.getString(R.string.INTENT_LINK_FOTO))
            }


            /*
            PREPARACION DE INTENT DESDE INFO EN PHP
            */
            Intent(context, QuakeDetailsActivity::class.java).apply {
                addFlags(FLAG_ACTIVITY_CLEAR_TOP)

                val bundle = Bundle().apply {
                    putString(context.getString(R.string.INTENT_TITULO), title)
                    putString(context.getString(R.string.INTENT_DESCRIPCION), description)
                    putString(context.getString(R.string.INTENT_CIUDAD), city)
                    putString(context.getString(R.string.INTENT_FECHA_UTC), utcDate)
                    putString(context.getString(R.string.INTENT_LATITUD), latitude)
                    putString(context.getString(R.string.INTENT_LONGITUD), longitude)
                    putDouble(context.getString(R.string.INTENT_MAGNITUD), magnitude)
                    putString(context.getString(R.string.INTENT_SENSIBLE), isSensitive)
                    putDouble(context.getString(R.string.INTENT_PROFUNDIDAD), depth)
                    putString(context.getString(R.string.INTENT_ESCALA), scale)
                    putString(context.getString(R.string.INTENT_REFERENCIA), reference)
                    putString(context.getString(R.string.INTENT_LINK_FOTO), imgUrl)
                    putString(context.getString(R.string.INTENT_ESTADO), status)
                }
                putExtras(bundle)

            }.also { it ->

                val pendingIntent =
                    PendingIntent.getActivity(context, 0, it, PendingIntent.FLAG_ONE_SHOT)

                Timber.i(context.getString(R.string.TRY_INTENT_NOTIFICATION_1))
                crashlytics.setCustomKey(context.getString(R.string.TRY_INTENT_NOTIFICATION), true)


                Builder(
                    context,
                    context.getString(R.string.FIREBASE_CHANNEL_ID_QUAKES)
                ).setSmallIcon(R.drawable.ic_lastquakechile_400)
                    .setContentTitle(title)
                    .setContentText(description)
                    .setStyle(BigTextStyle().bigText(description))
                    .setPriority(PRIORITY_MAX)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .also {

                        //Notify
                        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify(
                            Random().nextInt(60000),
                            it.build()
                        )
                    }
            }

        } catch (e: JSONException) {
            Timber.e(e, "JSon object exception error: %s", e.message)
        }
    }

    /**
     * Funcion que muestra notificacion generica
     *
     * @param remoteMessage Mensaje desde FCM
     */
    fun showNotificationGeneric(remoteMessage: RemoteMessage) {

        //Maneja la notificacion cuando esta en foreground
        Builder(
            context,
            context.getString(R.string.FIREBASE_CHANNEL_ID_QUAKES)
        ).setContentTitle(remoteMessage.notification?.title)
            .setContentText(remoteMessage.notification?.body)
            .setStyle(
                BigTextStyle().bigText(remoteMessage.notification?.body)
            )
            .setSmallIcon(R.drawable.ic_lastquakechile_400)
            .setAutoCancel(true)
            .also {
                (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify(
                    context.getString(R.string.FIREBASE_CHANNEL_ID_QUAKES).toInt(),
                    it.build()
                )
            }
    }

}