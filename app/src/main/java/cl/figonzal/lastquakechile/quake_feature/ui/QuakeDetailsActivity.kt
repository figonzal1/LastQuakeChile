package cl.figonzal.lastquakechile.quake_feature.ui

import android.app.NotificationManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.BundleCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import cl.figonzal.lastquakechile.core.services.notifications.utils.IS_SNAPSHOT_REQUEST_FROM_BOTTOM_SHEET
import cl.figonzal.lastquakechile.core.services.notifications.utils.QUAKE
import cl.figonzal.lastquakechile.core.ui.theme.LastQuakeChileTheme
import cl.figonzal.lastquakechile.quake_feature.domain.model.Quake
import cl.figonzal.lastquakechile.quake_feature.ui.compose.QuakeDetailScreen

class QuakeDetailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()

        val quake = getQuakeFromIntent() ?: return
        val isSnapshotRequest = intent.extras
            ?.getBoolean(IS_SNAPSHOT_REQUEST_FROM_BOTTOM_SHEET, false) ?: false

        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
            .cancel(quake.quakeCode)

        setContent {
            LastQuakeChileTheme {
                QuakeDetailScreen(
                    quake = quake,
                    isSnapshotRequest = isSnapshotRequest,
                    fragmentManager = supportFragmentManager,
                    onBack = { finish() }
                )
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun getQuakeFromIntent(): Quake? = intent.extras?.let {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            BundleCompat.getParcelable(it, QUAKE, Quake::class.java)
        } else {
            it.get(QUAKE) as? Quake
        }
    }
}
