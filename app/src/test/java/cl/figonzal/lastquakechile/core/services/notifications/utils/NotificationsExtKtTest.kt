package cl.figonzal.lastquakechile.core.services.notifications.utils

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class NotificationsExtKtTest {

    @Test
    fun `toMinMagnitude falls back to default on unparsable input`() {
        assertThat("".toMinMagnitude()).isEqualTo(1.0)
        assertThat(null.toMinMagnitude()).isEqualTo(1.0)
        assertThat("abc".toMinMagnitude()).isEqualTo(1.0)
        assertThat("1,5".toMinMagnitude()).isEqualTo(1.0)
    }

    @Test
    fun `toMinMagnitude parses a valid value`() {
        assertThat("5.6".toMinMagnitude()).isEqualTo(5.6)
    }
}
