package cl.figonzal.lastquakechile.quake_feature.ui

import android.content.Intent
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@MediumTest
@RunWith(AndroidJUnit4::class)
class QuakeDetailsActivityTest {

    /**
     * Regression test for a crash where a `QUAKE` extra parceled by an older app version
     * (before the @Parcelize migration) could no longer be read, leaving the extras empty
     * and crashing on an unsafe cast. The activity must finish gracefully instead.
     */
    @Test
    fun launchWithoutQuakeExtra_finishesInsteadOfCrashing() {
        val intent = Intent(
            ApplicationProvider.getApplicationContext(),
            QuakeDetailsActivity::class.java
        )

        ActivityScenario.launch<QuakeDetailsActivity>(intent).use {
            assertThat(it.state).isEqualTo(Lifecycle.State.DESTROYED)
        }
    }
}
