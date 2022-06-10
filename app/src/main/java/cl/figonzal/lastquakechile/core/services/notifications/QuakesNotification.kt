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
import cl.figonzal.lastquakechile.quake_feature.data.remote.dto.QuakeDTO
import cl.figonzal.lastquakechile.quake_feature.ui.QuakeDetailsActivity
import com.google.android.gms.tasks.Task
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.RemoteMessage
import timber.log.Timber
import java.io.Serializable

/**
 * Notificaciones de sismos con implementacion de Firebase
 */
class QuakesNotification(private val context: Context, private val sharedPrefUtil: SharedPrefUtil) :
    NotificationService {

    private val crashlytics: FirebaseCrashlytics = FirebaseCrashlytics.getInstance()

    @RequiresApi(api = Build.VERSION_CODES.O)
    override fun createChannel() {

        //Definicion de atributos de canal de notificacion
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
     * Funcion encargada de checkear la suscripcion del usuario al canal de alertas de sismos
     *
     * @param subcribed Boleano para determinar suscripcion a tema
     */
    fun suscribedToQuakes(subcribed: Boolean) {


        with(FirebaseMessaging.getInstance()) {
            //Suscribir a tema quakes
            //Eliminacion de la suscripcion
            when {
                subcribed -> this.subscribeToTopic(context.getString(R.string.firebase_topic_name))
                    .addOnCompleteListener { task: Task<Void?> ->
                        when {
                            task.isSuccessful -> {
                                //Modificar valor en sharepref de settings
                                sharedPrefUtil.saveData(
                                    context.getString(R.string.firebase_pref_key),
                                    true
                                )

                                Timber.d(context.getString(R.string.FIREBASE_SUB_OK))
                                crashlytics.setCustomKey(
                                    context.getString(R.string.subsqribed_quake),
                                    true
                                )
                            }
                        }
                    }
                else -> this.unsubscribeFromTopic(context.getString(R.string.firebase_topic_name))
                    .addOnCompleteListener { task: Task<Void> ->

                        when {
                            task.isSuccessful -> {
                                //Modificar valor en sharepref de settings
                                sharedPrefUtil.saveData(
                                    context.getString(R.string.firebase_pref_key),
                                    false
                                )

                                //LOG ZONE
                                Timber.d(context.getString(R.string.FIREBASE_SUB_DELETE))
                                crashlytics.setCustomKey(
                                    context.getString(R.string.subsqribed_quake),
                                    false
                                )
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
     * Funcion que muestra notificacion de FCM
     */
    override fun showNotification(remoteMessage: RemoteMessage) {

        //Obtener datos desde send_notification.php en servidor
        val result: MutableMap<String, String> = remoteMessage.data

        //Decompress data from FCM
        with(result) {

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
     * Funcion que muestra notificacion generica
     *
     * @param remoteMessage Mensaje desde FCM
     */
    fun showNotificationGeneric(remoteMessage: RemoteMessage) {

        //Maneja la notificacion cuando esta en foreground
        Builder(
            context,
            context.getString(R.string.firebase_channel_id_quakes)
        ).setContentTitle(remoteMessage.notification?.title)
            .setContentText(remoteMessage.notification?.body)
            .setStyle(
                BigTextStyle().bigText(remoteMessage.notification?.body)
            )
            .setSmallIcon(R.drawable.ic_lastquakechile_400)
            .setAutoCancel(true)
            .also {
                (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify(
                    context.getString(R.string.firebase_channel_id_quakes).toInt(),
                    it.build()
                )
            }
    }


    private fun handleFcmData(mutableMap: MutableMap<String, String>): ArrayList<Any> {

        with(mutableMap) {
            val title = getValue(context.getString(R.string.INTENT_TITULO))
            val description = getValue(context.getString(R.string.INTENT_DESCRIPCION))

            val quake = QuakeDTO(
                fecha_utc = getValue(context.getString(R.string.INTENT_FECHA_UTC)),
                ciudad = getValue(context.getString(R.string.INTENT_CIUDAD)),
                referencia = getValue(context.getString(R.string.INTENT_REFERENCIA)),
                latitud = getValue(context.getString(R.string.INTENT_LATITUD)).toDouble(),
                longitud = getValue(context.getString(R.string.INTENT_LONGITUD)).toDouble(),
                magnitud = getValue(context.getString(R.string.INTENT_MAGNITUD)).toDouble(),
                escala = getValue(context.getString(R.string.INTENT_ESCALA)),
                profundidad = getValue(context.getString(R.string.INTENT_PROFUNDIDAD)).toDouble(),
                estado = getValue(context.getString(R.string.INTENT_ESTADO)),
                sensible = getValue(context.getString(R.string.INTENT_SENSIBLE)),
                imagen_url = getValue(context.getString(R.string.INTENT_QUAKE_CODE)).toString(),
            ).toQuakeEntity().toDomainQuake()

            return arrayListOf(title, description, quake)
        }
    }
}

interface NotificationService {
    fun createChannel()
    fun showNotification(remoteMessage: RemoteMessage)
}