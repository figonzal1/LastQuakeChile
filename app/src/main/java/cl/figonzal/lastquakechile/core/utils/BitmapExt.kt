package cl.figonzal.lastquakechile.core.utils

import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.view.View
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

private const val SHARE_CACHE_DIR = "share"

private val Context.shareCacheDir: File
    get() = File(cacheDir, SHARE_CACHE_DIR).apply { mkdirs() }

/**
 * Wraps [this] in a Context whose display density is fixed, so an offscreen view inflated
 * from it (e.g. the Instagram Stories sticker) always renders at the same pixel size
 * regardless of the host device's density.
 */
fun Context.fixedDensityContext(densityDpi: Int): Context {
    val configuration = Configuration(resources.configuration).apply {
        this.densityDpi = densityDpi
    }
    return createConfigurationContext(configuration)
}

/**
 * Measures, lays out and draws a view that was never attached to a window, producing a
 * bitmap of it. [widthPx] is required because an unattached root has no parent to resolve
 * its layout_width against.
 */
fun View.renderToBitmap(widthPx: Int): Bitmap {
    val widthSpec = View.MeasureSpec.makeMeasureSpec(widthPx, View.MeasureSpec.EXACTLY)
    val heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)

    measure(widthSpec, heightSpec)
    layout(0, 0, measuredWidth, measuredHeight)

    return Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888).also {
        it.setHasAlpha(true)
        it.eraseColor(Color.TRANSPARENT)
        draw(Canvas(it))
    }
}

/**
 * Deletes every previously cached share image. Callers never revisit old share links, so this
 * should run once before writing a new batch - [cacheImageUri] itself no longer deletes, since
 * a single share can now produce several images (one per [StickerDesign]) that must coexist.
 */
fun Context.clearShareImageCache() {
    shareCacheDir.listFiles()?.forEach { it.delete() }
}

/**
 * Writes [bitmap] to the share cache dir and returns a [FileProvider] uri for it.
 */
@Throws(IOException::class)
fun Context.cacheImageUri(
    bitmap: Bitmap,
    name: String,
    format: Bitmap.CompressFormat,
    quality: Int = 100
): Uri {
    val extension = if (format == Bitmap.CompressFormat.PNG) "png" else "jpeg"
    val file = File(shareCacheDir, "$name.$extension")

    FileOutputStream(file).use { out -> bitmap.compress(format, quality, out) }

    return FileProvider.getUriForFile(this, "${applicationContext.packageName}.fileprovider", file)
}
