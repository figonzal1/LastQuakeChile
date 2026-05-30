package cl.figonzal.lastquakechile.core.services.notifications.utils

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat.PRIORITY_DEFAULT
import androidx.core.app.NotificationCompat.PRIORITY_HIGH
import androidx.core.content.ContextCompat
import cl.figonzal.lastquakechile.core.services.notifications.QuakeNotificationImpl
import cl.figonzal.lastquakechile.core.utils.SharedPrefUtil
import cl.figonzal.lastquakechile.quake_feature.domain.model.Quake
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.crashlytics.crashlytics
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.messaging
import timber.log.Timber

/**
 * Sets up notification channels and subscribes to the FCM topic only if POST_NOTIFICATIONS
 * is already granted (or the device is below Android 13 where no runtime permission is needed).
 * The permission request itself is deferred to the in-app cardview in QuakeFragment.
 */
fun setUpNotificationService(
    context: android.content.Context,
    sharedPrefUtil: SharedPrefUtil
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        QuakeNotificationImpl(context, sharedPrefUtil).createChannel()
    }

    val notificationsEnabled = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ->
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

        else -> true
    }

    if (notificationsEnabled) {
        // Respect the user's persisted preference — re-subscribing on every cold start would
        // override an explicit OFF toggle the user made in Settings.
        val userWantsAlerts = sharedPrefUtil.getData(ROOT_PREF_SUBSCRIPTION, true)
        if (userWantsAlerts) {
            subscribedToQuakes(true)
        } else {
            Timber.d("User opted out of alerts — skipping FCM subscription")
        }
    } else {
        Timber.d("POST_NOTIFICATIONS not granted — skipping FCM subscription")
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
fun subscribedToQuakes(isSubscribed: Boolean) {

    val fcm = Firebase.messaging
    val crashlytics = Firebase.crashlytics

    // Persistence of the user's preference is owned by SwitchPreferenceCompat (bound to the
    // "pref_suscrito_quake" key). This function is a pure FCM side-effect — writing here would
    // shadow that same key and corrupt the switch state.
    when {
        isSubscribed -> {

            fcm.subscribeToTopic(FIREBASE_TOPIC_CHANNEL)
                .addOnCompleteListener {
                    when {
                        it.isSuccessful -> {
                            Timber.d("Subscribed to topic")
                            crashlytics.setCustomKey(FIREBASE_SUB_QUAKE, true)
                        }
                    }
                }
        }

        else -> {
            fcm.unsubscribeFromTopic(FIREBASE_TOPIC_CHANNEL)
                .addOnCompleteListener {
                    when {
                        it.isSuccessful -> {
                            Timber.d("Subscription deleted")
                            crashlytics.setCustomKey(FIREBASE_SUB_QUAKE, false)
                        }
                    }
                }
                .addOnFailureListener { Timber.d("Already subscribed") }
        }
    }
}

fun getPreliminaryAlertsStatus(
    sharedPrefUtil: SharedPrefUtil,
    prefQuakePreliminaryKey: String,
    crashlytics: FirebaseCrashlytics
): Boolean {

    val isPreliminaryAlerts = sharedPrefUtil.getData(
        key = prefQuakePreliminaryKey,
        defaultValue = true
    )

    Timber.d("$prefQuakePreliminaryKey: $isPreliminaryAlerts")
    crashlytics.setCustomKey(prefQuakePreliminaryKey, isPreliminaryAlerts)

    return isPreliminaryAlerts
}

fun getNotificationPriority(
    sharedPrefUtil: SharedPrefUtil,
    prefHighPriorityKey: String,
    crashlytics: FirebaseCrashlytics
): Int {
    val highPriority = sharedPrefUtil.getData(prefHighPriorityKey, true)

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
        MIN_MAGNITUDE_ALERT
    ).toString()

    Timber.d("$minMagnitudeKey: ${savedMinMag.toDouble()}")
    crashlytics.setCustomKey(minMagnitudeKey, savedMinMag)
    return savedMinMag
}

fun Quake.greaterThan(minMagnitude: String) = magnitude >= minMagnitude.toDouble()
