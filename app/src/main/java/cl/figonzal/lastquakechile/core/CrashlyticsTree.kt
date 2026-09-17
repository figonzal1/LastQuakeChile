package cl.figonzal.lastquakechile.core

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber

class CrashlyticsTree : Timber.Tree() {

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {

        // VERBOSE/DEBUG es ruido de desarrollo; INFO en adelante es una miga de pan real para
        // el próximo crash report.
        if (priority < Log.INFO) return

        val crashlytics = FirebaseCrashlytics.getInstance()
        crashlytics.log(if (tag != null) "$tag: $message" else message)
        if (t != null) {
            crashlytics.recordException(t)
        }
    }
}