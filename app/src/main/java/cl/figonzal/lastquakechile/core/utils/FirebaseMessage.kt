package cl.figonzal.lastquakechile.core.utils

import com.google.android.gms.tasks.Task
import com.google.firebase.messaging.FirebaseMessaging
import timber.log.Timber


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
            Timber.i("Token %s", token)
        }
}
