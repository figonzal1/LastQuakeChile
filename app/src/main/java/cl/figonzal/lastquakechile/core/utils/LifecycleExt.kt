package cl.figonzal.lastquakechile.core.utils

import android.app.Activity
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import cl.figonzal.lastquakechile.core.services.ChangeLogService
import cl.figonzal.lastquakechile.core.services.GooglePlayService
import cl.figonzal.lastquakechile.core.services.NightModeService
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import timber.log.Timber


fun AppCompatActivity.initLifecycleObservers(sharedPrefUtil: SharedPrefUtil) {

    with(lifecycle) {
        val crashlytics = Firebase.crashlytics

        //Night mode
        checkNightMode(lifecycle = this, crashlytics = crashlytics)

        //GP services
        val playService = GooglePlayService(
            activity = this@initLifecycleObservers,
            crashlytics = crashlytics
        )
        addObserver(playService)

        //ChangeLog Service
        val changeLogService = ChangeLogService(
            this@initLifecycleObservers,
            sharedPrefUtil,
            crashlytics
        )
        addObserver(changeLogService)
    }
}

private fun Activity.checkNightMode(
    lifecycle: Lifecycle,
    crashlytics: FirebaseCrashlytics
) {
    when {
        Build.VERSION.SDK_INT < Build.VERSION_CODES.Q -> {
            val nightModeService = NightModeService(this@checkNightMode, crashlytics)
            lifecycle.addObserver(nightModeService)
            Timber.d("ANDROID_VERSION < Q: ${Build.VERSION.SDK_INT}")
        }

        else -> Timber.d("ANDROID_VERSION > Q: ${Build.VERSION.SDK_INT}")
    }
}