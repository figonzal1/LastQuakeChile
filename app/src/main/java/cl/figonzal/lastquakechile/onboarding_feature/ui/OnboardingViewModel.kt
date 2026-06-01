package cl.figonzal.lastquakechile.onboarding_feature.ui

import androidx.lifecycle.ViewModel
import cl.figonzal.lastquakechile.onboarding_feature.data.OnboardingPreferences

class OnboardingViewModel(private val prefs: OnboardingPreferences) : ViewModel() {
    fun completeOnboarding() = prefs.markCompleted()
}
