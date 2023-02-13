package cl.figonzal.lastquakechile.core.utils

import cl.figonzal.lastquakechile.quake_feature.domain.model.Coordinate
import cl.figonzal.lastquakechile.quake_feature.domain.model.Quake
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.LocalDateTime

class GoogleMapsExtKtTest {

    private val quakeList = listOf(
        Quake(
            quakeCode = 123,
            localDate = LocalDateTime.now().localDateTimeToString(),
            city = "La Serena",
            reference = "14km al OS de La Serena",
            magnitude = 5.6,
            depth = 34.8,
            scale = "ml",
            coordinate = Coordinate(-24.23, 95.3),
            isSensitive = false,
            isVerified = true
        ),
        Quake(
            quakeCode = 435,
            localDate = LocalDateTime.now().localDateTimeToString(),
            city = "Concepción",
            reference = "14km al OS de Concpeción",
            magnitude = 7.6,
            depth = 34.8,
            scale = "ml",
            coordinate = Coordinate(-14.88, 34.3),
            isSensitive = true,
            isVerified = true
        )
    )

    @Test
    fun `calculate mean cords`() {

        val result = calculateMeanCords(quakeList)

        assertThat(result.latitude).isEqualTo(-19.555)
        assertThat(result.longitude).isEqualTo(64.8)
    }
}