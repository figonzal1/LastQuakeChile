package cl.figonzal.lastquakechile.core.services.notifications.utils

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.view.View
import androidx.activity.result.ActivityResultLauncher
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
import com.google.firebase.Firebase
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.crashlytics.crashlytics
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
 * Manages the permission cardview in QuakeFragment.
 *
 * Visibility is driven by the real permission state (checkSelfPermission), NOT SharedPreferences,
 * so the card reappears after a reinstall or revocation regardless of Auto Backup state.
 *
 * The [launcher] must be registered by the Fragment before onStart (e.g. as a property).
 *
 * Flow:
 *  - Permission already granted → hide cardview.
 *  - Not granted, can ask again → show cardview with "Activate" → system dialog.
 *  - Permanently denied → show cardview with "Open Settings" → system notification settings.
 */
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
fun Fragment.handleCvAlertPermission(
    binding: FragmentQuakeBinding,
    sharedPrefUtil: SharedPrefUtil,
    launcher: ActivityResultLauncher<String>
) {
    val isGranted = ContextCompat.checkSelfPermission(
        requireContext(),
        Manifest.permission.POST_NOTIFICATIONS
    ) == PackageManager.PERMISSION_GRANTED

    if (isGranted) {
        // Detect the "just granted from Settings" transition: if the card was visible (or its
        // initial INVISIBLE/default state) right before this call, FCM wasn't subscribed at boot
        // because permission was missing. Subscribe now so the topic is registered without
        // requiring an app restart.
        val wasShowingCard = binding.cvAlertPermission.root.visibility != View.GONE
        binding.cvAlertPermission.root.visibility = View.GONE
        sharedPrefUtil.saveData(SHARED_PREF_PERMISSION_ALERT_ANDROID_13, true)
        // Subscribe only on the denied → granted transition AND if the user wants alerts.
        // Re-evaluating on every onResume would otherwise hit FCM with no actual state change.
        if (wasShowingCard && sharedPrefUtil.getData(ROOT_PREF_SUBSCRIPTION, true)) {
            subscribedToQuakes(true)
        }
        return
    }

    binding.cvAlertPermission.root.visibility = View.VISIBLE

    // shouldShowRequestPermissionRationale() returns false in TWO cases:
    //   1. Permission was NEVER requested (fresh install) — should show "Activate"
    //   2. User selected "Don't ask again" (permanently denied) — should show "Open Settings"
    // We use a persisted flag to distinguish them: if we've never launched the request, case 1.
    val wasAskedBefore = sharedPrefUtil.getData(SHARED_PREF_PERMISSION_ASKED_ONCE, false)
    val permanentlyDenied = wasAskedBefore && !shouldShowRequestPermissionRationale(
        Manifest.permission.POST_NOTIFICATIONS
    )

    with(binding.cvAlertPermission) {
        if (permanentlyDenied) {
            btnRequestPermission.setText(R.string.open_settings_button)
            btnRequestPermission.setOnClickListener {
                openNotificationSettings()
            }
        } else {
            btnRequestPermission.setText(R.string.activate_button)
            btnRequestPermission.setOnClickListener {
                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}

/**
 * Called from QuakeFragment after the permission launcher returns a result.
 * Updates SharedPrefs and subscribes to FCM on grant.
 */
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
fun Fragment.onNotificationPermissionResult(
    isGranted: Boolean,
    sharedPrefUtil: SharedPrefUtil
) {
    // Mark that the system dialog was shown at least once, so the next call to
    // handleCvAlertPermission can correctly distinguish "never asked" from "permanently denied".
    sharedPrefUtil.saveData(SHARED_PREF_PERMISSION_ASKED_ONCE, true)

    if (isGranted) {
        Timber.d("POST_NOTIFICATIONS granted")
        toast(R.string.notification_permission_on)
        sharedPrefUtil.saveData(SHARED_PREF_PERMISSION_ALERT_ANDROID_13, true)
        subscribedToQuakes(true)
    } else {
        Timber.d("POST_NOTIFICATIONS denied")
        toast(R.string.notification_permission_off)
        sharedPrefUtil.saveData(SHARED_PREF_PERMISSION_ALERT_ANDROID_13, false)
    }
}

private fun Fragment.openNotificationSettings() {
    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
        putExtra(Settings.EXTRA_APP_PACKAGE, requireContext().packageName)
    }
    startActivity(intent)
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
