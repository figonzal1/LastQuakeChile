package cl.figonzal.lastquakechile.core.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import cl.figonzal.lastquakechile.R
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
fun Context.subscribedToQuakes(isSubscribed: Boolean, sharedPrefUtil: SharedPrefUtil) {

    val firebase = FirebaseMessaging.getInstance()
    val crashlytics = FirebaseCrashlytics.getInstance()

    when {
        isSubscribed -> {

            firebase.subscribeToTopic(getString(R.string.firebase_topic_name))
                .addOnCompleteListener { task: Task<Void?> ->
                    when {
                        task.isSuccessful -> {

                            with(true) {
                                sharedPrefUtil.saveData(getString(R.string.firebase_pref_key), this)

                                Timber.d(getString(R.string.FIREBASE_SUB_OK))
                                crashlytics.setCustomKey(getString(R.string.subsqribed_quake), this)
                            }
                        }
                    }
                }
        }
        else -> {
            firebase.unsubscribeFromTopic(getString(R.string.firebase_topic_name))
                .addOnCompleteListener {
                    when {
                        it.isSuccessful -> {

                            with(false) {
                                sharedPrefUtil.saveData(
                                    getString(R.string.firebase_pref_key),
                                    this
                                )

                                Timber.d(getString(R.string.FIREBASE_SUB_DELETE))
                                crashlytics.setCustomKey(
                                    getString(R.string.subsqribed_quake),
                                    this
                                )
                            }
                        }
                    }
                }
                .addOnFailureListener {
                    Timber.d(getString(R.string.FIREBASE_SUB_ALREADY))
                }
        }
    }
}

fun Context.generateRandomChannelId(sharedPrefUtil: SharedPrefUtil): Int {

    val savedRandomChannel =
        sharedPrefUtil.getData(getString(R.string.random_channel_key), 1) as Int

    var newRandomChannel = 1
    while (savedRandomChannel == newRandomChannel) {
        newRandomChannel = SecureRandom().asKotlinRandom().nextInt()
    }

    sharedPrefUtil.saveData(getString(R.string.random_channel_key), newRandomChannel)

    return newRandomChannel
}
