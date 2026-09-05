package cl.figonzal.lastquakechile.core.utils

import android.content.Context
import android.graphics.Bitmap
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@MediumTest
@RunWith(AndroidJUnit4::class)
class BitmapExtTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun cacheImageUri_writesUnderShareSubdirAndResolvesThroughFileProvider() {
        val bitmap = Bitmap.createBitmap(2, 2, Bitmap.Config.ARGB_8888)

        val uri = context.cacheImageUri(bitmap, "test", Bitmap.CompressFormat.PNG)

        assertThat(File(context.cacheDir, "share/test.png").exists()).isTrue()
        assertThat(context.contentResolver.openInputStream(uri)).isNotNull()
    }

    @Test
    fun clearShareImageCache_deletesOnlyTheShareSubdir() {
        val bitmap = Bitmap.createBitmap(2, 2, Bitmap.Config.ARGB_8888)
        context.cacheImageUri(bitmap, "test", Bitmap.CompressFormat.PNG)
        val untouchedFile = File(context.cacheDir, "untouched.txt").apply { writeText("keep me") }

        context.clearShareImageCache()

        assertThat(File(context.cacheDir, "share/test.png").exists()).isFalse()
        assertThat(untouchedFile.exists()).isTrue()
        untouchedFile.delete()
    }
}
