package cl.figonzal.lastquakechile.core.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import cl.figonzal.lastquakechile.R
import cl.figonzal.lastquakechile.core.ui.compose.SettingsScreen
import cl.figonzal.lastquakechile.core.ui.theme.LastQuakeChileTheme
import cl.figonzal.lastquakechile.core.utils.SharedPrefUtil
import com.google.android.ump.ConsentInformation
import com.google.android.ump.UserMessagingPlatform
import org.koin.android.ext.android.inject
import timber.log.Timber

class SettingsActivity : AppCompatActivity() {

    private val sharedPrefUtil: SharedPrefUtil by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()

        val consentInformation = UserMessagingPlatform.getConsentInformation(this)
        val showAdsPolicy = consentInformation.privacyOptionsRequirementStatus ==
                ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED

        setContent {
            LastQuakeChileTheme {
                SettingsScreen(
                    sharedPrefUtil = sharedPrefUtil,
                    showAdsPolicy = showAdsPolicy,
                    onBack = { finish() },
                    onNightModeChanged = ::applyNightMode,
                    onShowPrivacyForm = ::showPrivacyForm,
                )
            }
        }
    }

    private fun applyNightMode(enabled: Boolean) {
        setDefaultNightMode(if (enabled) MODE_NIGHT_YES else MODE_NIGHT_NO)
        setTheme(R.style.AppTheme)
        recreate()
    }

    private fun showPrivacyForm() {
        UserMessagingPlatform.showPrivacyOptionsForm(this) { formError ->
            formError?.let { Timber.w("Privacy options form: ${it.message}") }
        }
    }
}
