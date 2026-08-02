package cl.figonzal.lastquakechile.quake_feature.ui.share

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import androidx.annotation.ColorInt
import cl.figonzal.lastquakechile.R
import java.util.Locale

/** The two Instagram-Stories-style backgrounds the user can pick for the share canvas. */
enum class StickerBackground {
    SOLID, GRADIENT
}

val StickerBackground.labelRes: Int
    get() = when (this) {
        StickerBackground.SOLID -> R.string.SHARE_BG_SOLID
        StickerBackground.GRADIENT -> R.string.SHARE_BG_GRADIENT
    }

/** Top/bottom hex colors for Instagram's `top_background_color` / `bottom_background_color` extras. */
fun StickerBackground.storyColors(@ColorInt magnitudeColor: Int): Pair<String, String> {
    val (top, bottom) = when (this) {
        StickerBackground.SOLID -> magnitudeColor to magnitudeColor
        StickerBackground.GRADIENT -> magnitudeColor to Color.BLACK
    }
    return top.toHexColor() to bottom.toHexColor()
}

/**
 * Drawable used both by the background-picker dot (an oval) and by the carousel's story canvas
 * (a rounded rect). SOLID uses a single fill color instead of a two-stop gradient with identical
 * stops, which would otherwise dither/band on some GPUs.
 */
fun StickerBackground.previewDrawable(
    @ColorInt magnitudeColor: Int,
    shape: Int,
    cornerRadiusPx: Float = 0f
): GradientDrawable = GradientDrawable().apply {
    this.shape = shape
    this.cornerRadius = cornerRadiusPx
    when (this@previewDrawable) {
        StickerBackground.SOLID -> setColor(magnitudeColor)
        StickerBackground.GRADIENT -> {
            orientation = GradientDrawable.Orientation.TOP_BOTTOM
            colors = intArrayOf(magnitudeColor, Color.BLACK)
        }
    }
}

fun Int.toHexColor(): String = String.format(Locale.US, "#%06X", 0xFFFFFF and this)
