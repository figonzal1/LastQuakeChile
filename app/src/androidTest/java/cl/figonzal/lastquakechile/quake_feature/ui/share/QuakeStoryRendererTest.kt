package cl.figonzal.lastquakechile.quake_feature.ui.share

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import cl.figonzal.lastquakechile.core.utils.cacheImageUri
import cl.figonzal.lastquakechile.core.utils.localDateTimeToString
import cl.figonzal.lastquakechile.quake_feature.domain.model.Coordinate
import cl.figonzal.lastquakechile.quake_feature.domain.model.Quake
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDateTime

private const val STICKER_WIDTH_PX = 1080

@MediumTest
@RunWith(AndroidJUnit4::class)
class QuakeStoryRendererTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val renderer = QuakeStoryRenderer(context)

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
    fun renderSticker_withoutMapSnapshot_producesFixedWidthArgbBitmapForEveryDesign() {
        StickerDesign.entries.forEach { design ->
            val bitmap = renderer.renderSticker(quake, mapSnapshot = null, design)

            assertThat(bitmap.config).isEqualTo(Bitmap.Config.ARGB_8888)
            assertThat(bitmap.width).isEqualTo(STICKER_WIDTH_PX)
            assertThat(bitmap.height).isGreaterThan(0)

            bitmap.recycle()
        }
    }

    @Test
    fun cacheImageUri_writesReadableFile() {
        val bitmap = renderer.renderSticker(quake, mapSnapshot = null, StickerDesign.CARD)
        val uri = context.cacheImageUri(bitmap, "test-sticker", Bitmap.CompressFormat.PNG)

        context.contentResolver.openInputStream(uri).use { stream ->
            val decoded = BitmapFactory.decodeStream(stream)
            assertThat(decoded).isNotNull()
            assertThat(decoded!!.width).isEqualTo(STICKER_WIDTH_PX)
            decoded.recycle()
        }

        bitmap.recycle()
    }
}
