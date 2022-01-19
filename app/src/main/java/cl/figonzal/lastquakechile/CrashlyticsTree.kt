package cl.figonzal.lastquakechile

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber

class CrashlyticsTree : Timber.Tree() {

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {

        when (priority) {
            Log.VERBOSE, Log.DEBUG, Log.INFO -> return
            else -> {
                FirebaseCrashlytics.getInstance().log(message)
                if (t != null) {
                    FirebaseCrashlytics.getInstance().recordException(t)
                }
            }
        }
    }
}