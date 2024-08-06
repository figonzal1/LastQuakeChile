package cl.figonzal.lastquakechile.core.services.notifications.utils

import android.Manifest
import android.app.Activity
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat.PRIORITY_DEFAULT
import androidx.core.app.NotificationCompat.PRIORITY_HIGH
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import cl.figonzal.lastquakechile.R
import cl.figonzal.lastquakechile.core.services.notifications.QuakeNotificationImpl
import cl.figonzal.lastquakechile.core.utils.SharedPrefUtil
import cl.figonzal.lastquakechile.core.utils.views.toast
import cl.figonzal.lastquakechile.databinding.FragmentQuakeBinding
import cl.figonzal.lastquakechile.quake_feature.domain.model.Quake
import com.google.android.gms.tasks.Task
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.ktx.messaging
import timber.log.Timber
import java.security.SecureRandom
import kotlin.random.asKotlinRandom

fun Activity.setUpNotificationService(sharedPrefUtil: SharedPrefUtil) {

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        QuakeNotificationImpl(this, sharedPrefUtil).createChannel()
    }

    //Automatic subscribe
    subscribedToQuakes(true, sharedPrefUtil)
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
fun Fragment.handleCvAlertPermission(
    binding: FragmentQuakeBinding,
    sharedPrefUtil: SharedPrefUtil
) {

    val requestPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->

        //Called when requestPermission is launch
        when {
            isGranted -> {
                Timber.d("Alert permission granted")
                toast(R.string.notification_permission_on)
                sharedPrefUtil.saveData(SHARED_PREF_PERMISSION_ALERT_ANDROID_13, true)
            }

            else -> {
                Timber.d("Alert permission not granted")
                toast(R.string.notification_permission_off)
                sharedPrefUtil.saveData(SHARED_PREF_PERMISSION_ALERT_ANDROID_13, false)
            }
        }
        sharedPrefUtil.saveData(SHARED_HIDE_ALERT_PERMISSION_CV, true)
        binding.cvAlertPermission.root.visibility = View.GONE
    }

    val showCv = sharedPrefUtil.getData(SHARED_HIDE_ALERT_PERMISSION_CV, false)

    if (!showCv) {

        with(binding.cvAlertPermission) {
            root.visibility = View.VISIBLE
            btnRequestPermission.setOnClickListener {
                launchRequestPermission(
                    this@handleCvAlertPermission,
                    sharedPrefUtil,
                    requestPermission
                ) {
                    root.visibility = View.GONE
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
fun launchRequestPermission(
    fragment: Fragment,
    sharedPrefUtil: SharedPrefUtil,
    requestPermission: ActivityResultLauncher<String>,
    hideCardView: () -> Unit
) {
    when (PackageManager.PERMISSION_GRANTED) {
        ContextCompat.checkSelfPermission(
            fragment.requireContext(),
            Manifest.permission.POST_NOTIFICATIONS
        ) -> {
            Timber.d("Permission already granted for this device")
            sharedPrefUtil.saveData(SHARED_PREF_PERMISSION_ALERT_ANDROID_13, true)

            //Hide cardview permission
            sharedPrefUtil.saveData(SHARED_HIDE_ALERT_PERMISSION_CV, true)
            fragment.toast(R.string.notification_permission_on)

            hideCardView()
        }

        else -> requestPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
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
) {

    val fcm = Firebase.messaging
    val crashlytics = Firebase.crashlytics

    when {
        isSubscribed -> {

            fcm.subscribeToTopic(FIREBASE_TOPIC_CHANNEL)
                .addOnCompleteListener {
                    when {
                        it.isSuccessful -> {

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
            fcm.unsubscribeFromTopic(FIREBASE_TOPIC_CHANNEL)
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

    val savedRandomChannel = sharedPrefUtil.getData(randomChannelIdKey, 1)

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
    )

    Timber.d("$prefQuakePreliminaryKey: $isPreliminaryAlerts")
    crashlytics.setCustomKey(prefQuakePreliminaryKey, isPreliminaryAlerts)

    return isPreliminaryAlerts
}

fun getRandomChannel(sharedPrefUtil: SharedPrefUtil, randomChannelId: String) =
    sharedPrefUtil.getData(randomChannelId, 1)

/**
 * Return importance level for channel creation
 */
@RequiresApi(Build.VERSION_CODES.N)
fun getChannelImportance(
    sharedPrefUtil: SharedPrefUtil,
    prefHighPriorityKey: String,
    crashlytics: FirebaseCrashlytics
): Int {

    val highPriority = sharedPrefUtil.getData(prefHighPriorityKey, true)

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
