package cl.figonzal.lastquakechile.core.services

import android.content.Context
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import cl.figonzal.lastquakechile.R
import cl.figonzal.lastquakechile.core.utils.SharedPrefUtil
import cl.figonzal.lastquakechile.core.utils.localDateTimeToString
import cl.figonzal.lastquakechile.core.utils.stringToLocalDateTime
import timber.log.Timber
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class InAppReviewService(
    private val context: Context,
    private val sharedPrefUtil: SharedPrefUtil,
    private val callBackRequest: () -> Unit
) : DefaultLifecycleObserver {

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)

        checkRequirements()
    }

    private fun checkRequirements() {

        val isLauncherActive = handleLaunchCounter()
        val isInstallMoreThan7Days = handleTimeCounter()

        when {
            isInstallMoreThan7Days && isLauncherActive -> {
                Timber.d("Request inAppReview")
                callBackRequest()
            }
            else -> {
                Timber.d("Ignore inAppReview")
            }
        }
    }

    private fun handleTimeCounter(): Boolean {

        val savedDate = getSavedInstallDate()
        //val hoursDiff = ChronoUnit.HOURS.between(savedDate, LocalDateTime.now())
        val hoursDiff = ChronoUnit.MINUTES.between(savedDate, LocalDateTime.now())

        return when {
            hoursDiff >= 15 -> {
                Timber.d("Install date: >= 15 min")
                true
            }
            else -> {
                Timber.d("Install date: < 15 min")
                false
            }
        }
    }

    private fun getSavedInstallDate(): LocalDateTime {

        //Try to find install date in shared pref
        val installDate = sharedPrefUtil.getData(
            key = context.getString(R.string.shared_pref_install_date),
            defaultValue = ""
        ) as String

        if (installDate.isEmpty() || installDate.isBlank()) {

            //Save new value
            sharedPrefUtil.saveData(
                key = context.getString(R.string.shared_pref_install_date),
                value = LocalDateTime.now().localDateTimeToString()
            )
        }

        return (sharedPrefUtil.getData(
            key = context.getString(R.string.shared_pref_install_date),
            defaultValue = ""
        ) as String).stringToLocalDateTime()
    }

    private fun handleLaunchCounter(): Boolean {

        val launchCount = sharedPrefUtil.getData(
            key = context.getString(R.string.shared_pref_launch_counter),
            defaultValue = 0
        ) as Int

        return when {

            //If user open the app every 10 times
            launchCount % 10 == 0 && launchCount != 0 -> {
                resetInitNumber() //reset counter
                true
            }
            else -> {
                increaseInitNumber(launchCount)
                false
            }
        }
    }

    private fun resetInitNumber() {
        Timber.d("Launcher counter is 10, resetting counter")
        sharedPrefUtil.saveData(context.getString(R.string.shared_pref_launch_counter), 0)
    }

    private fun increaseInitNumber(initNumber: Int) {
        Timber.d("Launcher counter is != 10, increasing counter")
        sharedPrefUtil.saveData(
            key = context.getString(R.string.shared_pref_launch_counter),
            value = initNumber + 1
        )
    }
}