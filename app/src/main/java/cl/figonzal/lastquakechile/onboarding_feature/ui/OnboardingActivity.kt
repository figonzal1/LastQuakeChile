package cl.figonzal.lastquakechile.onboarding_feature.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import cl.figonzal.lastquakechile.core.ui.MainActivity
import cl.figonzal.lastquakechile.core.ui.theme.LastQuakeChileTheme
import cl.figonzal.lastquakechile.onboarding_feature.ui.compose.OnboardingScreen

class OnboardingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LastQuakeChileTheme {
                OnboardingScreen(onFinish = ::navigateToMain)
            }
        }
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
