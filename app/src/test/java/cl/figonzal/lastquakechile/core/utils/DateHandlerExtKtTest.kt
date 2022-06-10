package cl.figonzal.lastquakechile.core.utils

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.LocalDateTime

class DateHandlerExtKtTest {

    @Test
    fun `localDateToDHMS have keys`() {

        val resultMap = LocalDateTime.now().localDateToDHMS()

        assertThat(resultMap).containsKey("days")
        assertThat(resultMap).containsKey("hours")
        assertThat(resultMap).containsKey("minutes")
        assertThat(resultMap).containsKey("seconds")
    }

    @Test
    fun `latLongToDMS have keys`() {

        val lat = -28.173718

        val latResult = lat.latLongToDMS()

        assertThat(latResult).containsKey("grados")
        assertThat(latResult).containsKey("minutos")
        assertThat(latResult).containsKey("segundos")
    }


    @Test
    fun `latitude to DMS`() {

        val lat = -28.173718
        val latResult = lat.latLongToDMS()

        assertThat(latResult).containsEntry("grados", 28.0)
        assertThat(latResult).containsEntry("minutos", 10.0)
        assertThat(latResult).containsEntry("segundos", 25.0)
    }

    @Test
    fun `longitude to DMS`() {

        val long = -69.785156
        val longResult = long.latLongToDMS()

        assertThat(longResult).containsEntry("grados", 69.0)
        assertThat(longResult).containsEntry("minutos", 47.0)
        assertThat(longResult).containsEntry("segundos", 7.0)
    }
}