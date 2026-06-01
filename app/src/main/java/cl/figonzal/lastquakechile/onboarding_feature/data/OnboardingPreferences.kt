package cl.figonzal.lastquakechile.onboarding_feature.data

import cl.figonzal.lastquakechile.core.utils.SharedPrefUtil

class OnboardingPreferences(private val sharedPrefUtil: SharedPrefUtil) {

    fun isCompleted(): Boolean = sharedPrefUtil.getData(KEY, false)

    fun markCompleted() = sharedPrefUtil.saveData(KEY, true)

    companion object {
        private const val KEY = "onboarding_completed"
    }
}
