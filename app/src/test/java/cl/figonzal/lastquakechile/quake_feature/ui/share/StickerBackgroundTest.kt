package cl.figonzal.lastquakechile.quake_feature.ui.share

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class StickerBackgroundTest {

    @Test
    fun `storyColors returns valid hex pairs for every background`() {
        val magnitudeColor = 0xFF3F51B5.toInt()

        StickerBackground.entries.forEach { background ->
            val (top, bottom) = background.storyColors(magnitudeColor)

            assertThat(top).matches("#[0-9A-F]{6}")
            assertThat(bottom).matches("#[0-9A-F]{6}")

            when (background) {
                StickerBackground.SOLID -> assertThat(bottom).isEqualTo(top)
                StickerBackground.GRADIENT -> assertThat(bottom).isEqualTo("#000000")
            }
        }
    }

    @Test
    fun `toHexColor discards alpha`() {
        assertThat(0xFF3F51B5.toInt().toHexColor()).isEqualTo("#3F51B5")
    }
}
