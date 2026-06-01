package cl.figonzal.lastquakechile.onboarding_feature.ui.compose

data class OnboardingPageModel(val title: String, val description: String)

val onboardingPages = listOf(
    OnboardingPageModel("Bienvenido", "Monitoreo sísmico de Chile en tiempo real."),
    OnboardingPageModel("Sismos recientes", "Consulta los últimos terremotos con detalle."),
    OnboardingPageModel("Notificaciones", "Recibe alertas de sismos importantes.")
)
