package cl.figonzal.lastquakechile.core.utils

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.LocalDateTime

class DateHandlerExtKtTest {

    @Test
    fun `toElapsed for now is zero`() {

        val elapsed = LocalDateTime.now().toElapsed()

        assertThat(elapsed.days).isEqualTo(0L)
        assertThat(elapsed.hours).isEqualTo(0L)
        assertThat(elapsed.minutes).isEqualTo(0L)
        assertThat(elapsed.seconds).isEqualTo(0L)
    }

    @Test
    fun `latitude to DMS`() {

        val lat = -28.173718
        val latResult = lat.toDMS()

        assertThat(latResult.degrees).isEqualTo(28.0)
        assertThat(latResult.minutes).isEqualTo(10.0)
        assertThat(latResult.seconds).isEqualTo(25.0)
    }

    @Test
    fun `longitude to DMS`() {

        val long = -69.785156
        val longResult = long.toDMS()

        assertThat(longResult.degrees).isEqualTo(69.0)
        assertThat(longResult.minutes).isEqualTo(47.0)
        assertThat(longResult.seconds).isEqualTo(7.0)
    }
}
