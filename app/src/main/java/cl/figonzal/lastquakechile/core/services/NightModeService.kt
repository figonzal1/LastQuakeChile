package cl.figonzal.lastquakechile.core.services

import android.app.Activity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.preference.PreferenceManager
import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber

private const val ROOT_PREF_NIGHT_MODE = "pref_night_mode"
private const val FIREBASE_NIGHT_MODE_STATUS = "night_mode_status"

class NightModeService(
    private val activity: Activity,
    private val crashlytics: FirebaseCrashlytics
) : DefaultLifecycleObserver {


    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)

        checkNightMode()
    }

    private fun checkNightMode() {

        //Leer preference settings
        val isNightModeActivated = PreferenceManager
            .getDefaultSharedPreferences(activity)
            .getBoolean(ROOT_PREF_NIGHT_MODE, false)

        when {
            isNightModeActivated -> {

                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

                Timber.d("Night mode: ON")
                crashlytics.setCustomKey(FIREBASE_NIGHT_MODE_STATUS, "ON")
            }

            else -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

                Timber.d("Night mode: OFF")
                crashlytics.setCustomKey(FIREBASE_NIGHT_MODE_STATUS, "OFF")
            }
        }
    }
}