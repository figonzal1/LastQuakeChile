package cl.figonzal.lastquakechile.quake_feature.ui.share

import android.content.Context
import android.graphics.Bitmap
import android.util.DisplayMetrics
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import cl.figonzal.lastquakechile.R
import cl.figonzal.lastquakechile.core.utils.fixedDensityContext
import cl.figonzal.lastquakechile.core.utils.renderToBitmap
import cl.figonzal.lastquakechile.core.utils.stringToLocalDateTime
import cl.figonzal.lastquakechile.core.utils.views.QUAKE_DETAILS_DEPTH_FORMAT
import cl.figonzal.lastquakechile.core.utils.views.QUAKE_DETAILS_MAGNITUDE_FORMAT
import cl.figonzal.lastquakechile.core.utils.views.getMagnitudeColor
import cl.figonzal.lastquakechile.core.utils.views.setScale
import cl.figonzal.lastquakechile.databinding.ShareStoryStickerBinding
import cl.figonzal.lastquakechile.databinding.ShareStoryStickerMagnitudeBinding
import cl.figonzal.lastquakechile.quake_feature.domain.model.Quake
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

private const val STICKER_WIDTH_DP = 360

enum class StickerDesign(@StringRes val labelRes: Int) {
    CARD(R.string.SHARE_DESIGN_CARD),
    MAGNITUDE(R.string.SHARE_DESIGN_MAGNITUDE)
}

/**
 * Renders the earthquake data card used both as the Instagram Stories sticker and as the
 * share bottom sheet preview, in any of the [StickerDesign] variants. Every view is inflated
 * off-screen at a fixed density so the resulting bitmap is always the same pixel size
 * regardless of the host device.
 */
class QuakeStoryRenderer(private val context: Context) {

    fun renderSticker(quake: Quake, mapSnapshot: Bitmap?, design: StickerDesign): Bitmap =
        when (design) {
            StickerDesign.CARD -> renderCardSticker(quake, mapSnapshot)
            StickerDesign.MAGNITUDE -> renderMagnitudeSticker(quake)
        }

    private fun renderCardSticker(quake: Quake, mapSnapshot: Bitmap?): Bitmap {
        val densityContext = themedDensityContext()
        val binding = ShareStoryStickerBinding.inflate(LayoutInflater.from(densityContext))

        with(binding) {
            tvShareCity.text = quake.city
            tvShareReference.text = quake.reference
            tvShareDatetime.text = quake.fullDateText()

            tvShareMagnitude.text = String.format(
                Locale.getDefault(),
                QUAKE_DETAILS_MAGNITUDE_FORMAT,
                quake.magnitude
            )
            ivShareMagColor.setColorFilter(
                ContextCompat.getColor(densityContext, getMagnitudeColor(quake.magnitude, false))
            )

            tvShareDepthValue.text = String.format(
                Locale.getDefault(),
                QUAKE_DETAILS_DEPTH_FORMAT,
                quake.depth
            )

            tvShareScaleValue.setScale(quake.scale)

            when (mapSnapshot) {
                null -> {
                    ivShareMap.setImageDrawable(null)
                    ivShareMap.setBackgroundColor(
                        ContextCompat.getColor(densityContext, getMagnitudeColor(quake.magnitude, false))
                    )
                    tvShareMapAttribution.visibility = View.GONE
                }

                else -> ivShareMap.setImageBitmap(mapSnapshot)
            }
        }

        return binding.root.toStickerBitmap(densityContext.resources.displayMetrics.density)
    }

    private fun renderMagnitudeSticker(quake: Quake): Bitmap {
        val densityContext = themedDensityContext()
        val binding = ShareStoryStickerMagnitudeBinding.inflate(LayoutInflater.from(densityContext))

        val magnitudeColor = ContextCompat.getColor(densityContext, getMagnitudeColor(quake.magnitude, false))

        with(binding) {
            tvShareMagnitudeValue.text = String.format(
                Locale.getDefault(),
                QUAKE_DETAILS_MAGNITUDE_FORMAT,
                quake.magnitude
            )
            tvShareMagnitudeValue.setTextColor(magnitudeColor)
            viewShareMagnitudeRule.setBackgroundColor(magnitudeColor)
            tvShareMagnitudeScale.setScale(quake.scale)
            tvShareMagnitudeCity.text = quake.city
            tvShareMagnitudeReference.text = quake.reference
            tvShareMagnitudeDatetime.text = quake.fullDateText()
        }

        return binding.root.toStickerBitmap(densityContext.resources.displayMetrics.density)
    }

    /**
     * context is the Application context (Koin androidContext()), whose theme is the OS
     * default, not Theme.MaterialComponents - only Activities inherit the manifest's
     * android:theme. MaterialCardView requires a MaterialComponents descendant to inflate,
     * so the app theme must be applied explicitly here.
     */
    private fun themedDensityContext(): Context = ContextThemeWrapper(
        context.fixedDensityContext(DisplayMetrics.DENSITY_XXHIGH),
        R.style.AppTheme
    )

    private fun View.toStickerBitmap(density: Float): Bitmap {
        val widthPx = (STICKER_WIDTH_DP * density).toInt()
        return renderToBitmap(widthPx)
    }

    /** Stickers outlive the moment they were generated, so they show an absolute date, not a relative one. */
    private fun Quake.fullDateText(): String = localDate.stringToLocalDateTime()
        .format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT))

    /** The quake's magnitude color, used to build the picker's [StickerBackground] previews. */
    @ColorInt
    fun magnitudeColor(quake: Quake): Int =
        ContextCompat.getColor(context, getMagnitudeColor(quake.magnitude, false))
}
