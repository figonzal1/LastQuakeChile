package cl.figonzal.lastquakechile.onboarding_feature.di

import cl.figonzal.lastquakechile.onboarding_feature.data.OnboardingPreferences
import cl.figonzal.lastquakechile.onboarding_feature.ui.OnboardingViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val onboardingModule = module {
    single { OnboardingPreferences(get()) }
    viewModel { OnboardingViewModel(get()) }
}
