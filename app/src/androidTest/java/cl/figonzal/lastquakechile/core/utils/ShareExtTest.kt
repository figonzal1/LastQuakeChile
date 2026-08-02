package cl.figonzal.lastquakechile.core.utils

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import cl.figonzal.lastquakechile.quake_feature.domain.model.Coordinate
import cl.figonzal.lastquakechile.quake_feature.domain.model.Quake
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDateTime

@MediumTest
@RunWith(AndroidJUnit4::class)
class ShareExtTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    private val quake = Quake(
        quakeCode = 123,
        localDate = LocalDateTime.now().localDateTimeToString(),
        city = "La Serena",
        reference = "14km al OS de La Serena",
        magnitude = 6.2,
        depth = 34.8,
        scale = "Mw",
        coordinate = Coordinate(-24.23, -70.3),
        isSensitive = false,
        isVerified = true
    )

    @Test
    fun buildShareText_containsQuakeData() {
        val text = context.buildShareText(quake)

        assertThat(text).contains(quake.city)
        assertThat(text).contains(quake.reference)
        assertThat(text).contains(quake.localDate)
    }

    @Test
    fun isInstagramStoriesAvailable_falseWhenInstagramNotInstalled() {
        // Test/CI devices don't have Instagram installed.
        assertThat(context.isInstagramStoriesAvailable()).isFalse()
    }
}
