package cl.figonzal.lastquakechile.core.services.notifications.utils

import android.Manifest
import android.app.Activity
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat.PRIORITY_DEFAULT
import androidx.core.app.NotificationCompat.PRIORITY_HIGH
import androidx.core.content.ContextCompat
import cl.figonzal.lastquakechile.core.utils.SharedPrefUtil
import cl.figonzal.lastquakechile.quake_feature.domain.model.Quake
import com.google.android.gms.tasks.Task
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.messaging.FirebaseMessaging
import timber.log.Timber
import java.security.SecureRandom
import kotlin.random.asKotlinRandom

/**
 * Request notification permission for android 13
 */
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
fun Activity.checkAlertsPermissions(
    permissionLauncher: ActivityResultLauncher<String>
) {
    when (PackageManager.PERMISSION_GRANTED) {
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) -> Timber.d("Permission granted for this device")
        else -> permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}

/**
 * Retrieve token for FCM
 */
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

/**
 * Function that checks subscriptions to quake channels alerts
 *
 * @param isSubscribed
 */
fun subscribedToQuakes(
    isSubscribed: Boolean,
    sharedPrefUtil: SharedPrefUtil,
    messaging: FirebaseMessaging,
    crashlytics: FirebaseCrashlytics
) {

    when {
        isSubscribed -> {

            messaging.subscribeToTopic(FIREBASE_TOPIC_CHANNEL)
                .addOnCompleteListener { task: Task<Void?> ->
                    when {
                        task.isSuccessful -> {

                            with(true) {
                                sharedPrefUtil.saveData(ROOT_PREF_SUBSCRIPTION, this)

                                Timber.d("Subscribed to topic")
                                crashlytics.setCustomKey(FIREBASE_SUB_QUAKE, this)
                            }
                        }
                    }
                }
        }
        else -> {
            messaging.unsubscribeFromTopic(FIREBASE_TOPIC_CHANNEL)
                .addOnCompleteListener {
                    when {
                        it.isSuccessful -> {

                            with(false) {
                                sharedPrefUtil.saveData(ROOT_PREF_SUBSCRIPTION, this)

                                Timber.d("Subscription deleted")
                                crashlytics.setCustomKey(FIREBASE_SUB_QUAKE, this)
                            }
                        }
                    }
                }
                .addOnFailureListener { Timber.d("Already subscribed") }
        }
    }
}

fun generateRandomChannelId(
    sharedPrefUtil: SharedPrefUtil,
    randomChannelIdKey: String
): Int {

    val savedRandomChannel = sharedPrefUtil.getData(randomChannelIdKey, 1) as Int

    var newRandomChannel = 1
    while (savedRandomChannel == newRandomChannel) {
        newRandomChannel = SecureRandom().asKotlinRandom().nextInt()
    }
    sharedPrefUtil.saveData(randomChannelIdKey, newRandomChannel)
    return newRandomChannel
}

fun getPreliminaryAlertsStatus(
    sharedPrefUtil: SharedPrefUtil,
    prefQuakePreliminaryKey: String,
    crashlytics: FirebaseCrashlytics
): Boolean {

    val isPreliminaryAlerts = sharedPrefUtil.getData(
        key = prefQuakePreliminaryKey,
        defaultValue = true
    ) as Boolean

    Timber.d("$prefQuakePreliminaryKey: $isPreliminaryAlerts")
    crashlytics.setCustomKey(prefQuakePreliminaryKey, isPreliminaryAlerts)

    return isPreliminaryAlerts
}

fun getRandomChannel(sharedPrefUtil: SharedPrefUtil, randomChannelId: String) =
    sharedPrefUtil.getData(randomChannelId, 1) as Int

/**
 * Return importance level for channel creation
 */
@RequiresApi(Build.VERSION_CODES.N)
fun getChannelImportance(
    sharedPrefUtil: SharedPrefUtil,
    prefHighPriorityKey: String,
    crashlytics: FirebaseCrashlytics
): Int {

    val highPriority = sharedPrefUtil.getData(prefHighPriorityKey, true) as Boolean

    Timber.d("$prefHighPriorityKey: $highPriority")
    crashlytics.setCustomKey(prefHighPriorityKey, highPriority)

    return when {
        highPriority -> NotificationManager.IMPORTANCE_HIGH
        else -> NotificationManager.IMPORTANCE_DEFAULT
    }
}

/**
 * Return priority level for notification
 */
fun getNotificationPriority(
    sharedPrefUtil: SharedPrefUtil,
    prefHighPriorityKey: String,
    crashlytics: FirebaseCrashlytics
): Int {
    val highPriority = sharedPrefUtil.getData(prefHighPriorityKey, true) as Boolean

    Timber.d("$prefHighPriorityKey: $highPriority")
    crashlytics.setCustomKey(prefHighPriorityKey, highPriority)

    return when {
        highPriority -> PRIORITY_HIGH
        else -> PRIORITY_DEFAULT
    }
}

fun getMinMagnitude(
    sharedPrefUtil: SharedPrefUtil,
    minMagnitudeKey: String,
    crashlytics: FirebaseCrashlytics
): String {

    val savedMinMag = sharedPrefUtil.getData(
        minMagnitudeKey,
        "5.0"
    ).toString()

    Timber.d("$minMagnitudeKey: ${savedMinMag.toDouble()}")
    crashlytics.setCustomKey(minMagnitudeKey, savedMinMag)
    return savedMinMag
}

fun Quake.greatherThan(minMagnitude: String) = magnitude >= minMagnitude.toDouble()
