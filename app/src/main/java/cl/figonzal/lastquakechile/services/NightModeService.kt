package cl.figonzal.lastquakechile.services

import android.app.Activity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.preference.PreferenceManager
import cl.figonzal.lastquakechile.R
import timber.log.Timber

class NightModeService(
    private val activity: Activity
) : DefaultLifecycleObserver {


    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)

        checkNightMode()
    }

    private fun checkNightMode() {

        //Leer preference settings
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)

        val manualNightMode =
            sharedPreferences.getBoolean(activity.getString(R.string.NIGHT_MODE_KEY), false)

        //MANUAL MODE
        //manual mode activated
        when {
            manualNightMode -> {

                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

                Timber.i(
                    activity.getString(R.string.TAG_NIGHT_MODE) + ": ON"
                )
            }

            else -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

                Timber.i(
                    activity.getString(R.string.TAG_NIGHT_MODE) + ": OFF"
                )
            }
        }
    }
}