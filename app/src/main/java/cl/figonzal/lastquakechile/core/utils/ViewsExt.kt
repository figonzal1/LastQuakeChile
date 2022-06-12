package cl.figonzal.lastquakechile.core.utils

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import cl.figonzal.lastquakechile.R
import cl.figonzal.lastquakechile.quake_feature.domain.model.Coordinates
import cl.figonzal.lastquakechile.quake_feature.domain.model.Quake
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory
import com.google.android.material.tabs.TabLayout
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import kotlin.math.floor

/**
 * Function that sets background colors depending on the magnitude of the earthquake
 *
 * @param magnitude Quake magnitude
 * @return id color resource
 */
fun getMagnitudeColor(magnitude: Double, forMapa: Boolean): Int {
    return getColorResource(forMapa, floor(magnitude).toInt())
}

private fun getColorResource(forMapa: Boolean, mMagFloor: Int): Int {

    return when {
        mMagFloor == 1 -> {
            when {
                forMapa -> R.color.magnitude1_alpha
                else -> R.color.magnitude1
            }

        }
        mMagFloor == 2 -> {
            when {
                forMapa -> R.color.magnitude2_alpha
                else -> R.color.magnitude2
            }
        }
        mMagFloor == 3 -> {
            when {
                forMapa -> R.color.magnitude3_alpha
                else -> R.color.magnitude3
            }
        }
        mMagFloor == 4 -> {
            when {
                forMapa -> R.color.magnitude4_alpha
                else -> R.color.magnitude4
            }
        }
        mMagFloor == 5 -> {
            when {
                forMapa -> R.color.magnitude5_alpha
                else -> R.color.magnitude5
            }
        }
        mMagFloor == 6 -> {
            when {
                forMapa -> R.color.magnitude6_alpha
                else -> R.color.magnitude6
            }
        }
        mMagFloor == 7 -> {
            when {
                forMapa -> R.color.magnitude7_alpha
                else -> R.color.magnitude7
            }
        }
        mMagFloor >= 8 -> {
            when {
                forMapa -> R.color.magnitude8_alpha
                else -> R.color.magnitude8
            }
        }
        else -> {
            R.color.colorPrimary
        }
    }
}

/**
 * Inflater any view
 */
fun ViewGroup.layoutInflater(layout: Int): View =
    LayoutInflater.from(context).inflate(layout, this, false)

/**
 * Function that lets set quake status image (Preliminary or Verified)
 *
 * @param state    Quake state
 * @param tvState TextView that hold the value
 */
fun ImageView.setStatusImage(
    state: Boolean,
    tvState: TextView
) {

    when {
        !state -> {
            tvState.text = String.format(
                Locale.US,
                context.getString(R.string.quakes_details_estado_sismo),
                context.getString(R.string.quakes_details_estado_sismo_preliminar)
            )
            setImageDrawable(
                ContextCompat.getDrawable(
                    context,
                    R.drawable.ic_round_check_circle_outline_24
                )
            )
        }
        state -> {

            tvState.text = String.format(
                Locale.US,
                context.getString(R.string.quakes_details_estado_sismo),
                context.getString(R.string.quakes_details_estado_sismo_verificado)
            )
            setImageDrawable(
                ContextCompat.getDrawable(
                    context,
                    R.drawable.ic_round_check_circle_24
                )
            )
        }
    }
}

/**
 * Setting scale textview depending on the value of string
 *
 * @param scale Quake scale
 */
fun TextView.setScale(scale: String) {
    text = when {
        scale.contains("Mw") -> {
            String.format(
                context.getString(R.string.quake_details_escala),
                context.getString(R.string.quake_details_magnitud_momento)
            )
        }
        else -> {
            String.format(
                context.getString(R.string.quake_details_escala),
                context.getString(R.string.quake_details_magnitud_local)
            )
        }
    }
}

/**
 * Extension for toast
 */
fun Activity.toast(stringId: Int) {
    Toast.makeText(
        this,
        getString(stringId),
        Toast.LENGTH_LONG
    ).show()
}

/**
 * Transform localDateTime to a string text in (days or hours or minutes)
 */
fun TextView.timeToText(quake: Quake) {

    val timeMap = quake.localDate.localDateToDHMS()

    val days = timeMap[this.context.getString(R.string.utils_time_day)]
    val hour = timeMap[this.context.getString(R.string.utils_time_hour)]
    val min = timeMap[this.context.getString(R.string.utils_time_min)]
    val seg = timeMap[this.context.getString(R.string.utils_time_seg)]

    when {
        days != null && days == 0L -> {

            when {
                hour != null && hour >= 1 -> text =
                    String.format(
                        this.context.getString(R.string.quake_time_hour_info_windows),
                        hour
                    )
                else -> {
                    text = String.format(
                        this.context.getString(R.string.quake_time_minute_info_windows),
                        min
                    )

                    if (min != null && min < 1) text = String.format(
                        this.context.getString(R.string.quake_time_second_info_windows), seg
                    )
                }
            }
        }
        days != null && days > 0 -> {
            when {
                hour != null && hour == 0L -> text =
                    String.format(
                        this.context.getString(R.string.quake_time_day_info_windows),
                        days
                    )
                hour != null && hour >= 1 -> text = String.format(
                    this.context.getString(R.string.quake_time_day_hour_info_windows),
                    days,
                    hour / 24
                )
            }
        }
    }
}

/**
 * Coordinates to DMS
 */
fun TextView.formatDMS(coordinates: Coordinates) {

    val latDMS = coordinates.latitude.latLongToDMS()
    val degreeLat = latDMS["grados"]
    val minLat = latDMS["minutos"]
    val segLat = latDMS["segundos"]

    val dmsLat = String.format(
        Locale.US,
        "%.1f° %.1f' %.1f'' %s",
        degreeLat,
        minLat,
        segLat,
        when {
            coordinates.latitude < 0 -> this.context.getString(R.string.coordenadas_sur)
            else -> this.context.getString(R.string.coordenadas_norte)
        }
    )

    val longDMS = coordinates.longitude.latLongToDMS()
    val degreeLong = longDMS["grados"]
    val minLong = longDMS["minutos"]
    val segLong = longDMS["segundos"]

    val dmsLong = String.format(
        Locale.US,
        "%.1f° %.1f' %.1f'' %s",
        degreeLong,
        minLong,
        segLong,
        when {
            coordinates.longitude < 0 -> this.context.getString(R.string.coordenadas_oeste)
            else -> this.context.getString(R.string.coordenadas_este)
        }
    )

    text =
        String.format(this.context.getString(R.string.format_coordenadas), dmsLat, dmsLong)
}

/**
 * Save snapshot from google map in cache directory
 */
@Throws(IOException::class)
fun Context.getLocalBitmapUri(bitmap: Bitmap): Uri {

    val c = Calendar.getInstance()
    val date = c.timeInMillis.toInt()
    val file = File(this.cacheDir, "share$date.jpeg")

    when {
        file.exists() -> Timber.d(getString(R.string.IMAGE_CACHE_EXISTS))
        else -> {
            Timber.d(getString(R.string.IMAGE_CACHE_NOT_EXISTS))
            val out = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
            out.close()
        }
    }
    return FileProvider.getUriForFile(this, "cl.figonzal.lastquakechile.fileprovider", file)
}

/**
 * Function to print change log features
 */
fun List<String>.printChangeLogList(): CharSequence {
    var changes = ""

    this.indices.forEach { i ->

        changes = when {
            i > 0 -> changes.plus("\n" + this[i])
            else -> changes.plus(this[i])
        }
    }
    return changes
}

/**
 * Set width for icons in tabs
 */
fun TabLayout.setTabWidthAsWrapContent(tabPosition: Int) {
    val layout = (this.getChildAt(0) as LinearLayout).getChildAt(tabPosition) as LinearLayout
    val layoutParams = layout.layoutParams as LinearLayout.LayoutParams
    layoutParams.weight = 0f
    layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT
    layout.layoutParams = layoutParams
}

/**
 * Load image to imageView from URL
 *
 * @param url
 * @param imageView
 * @return
 */
fun Context.loadImage(url: Int, imageView: ImageView) = Glide.with(this)
    .load(url)
    .centerCrop()
    .error(R.drawable.not_found)
    .placeholder(R.drawable.placeholder)
    .transition(
        DrawableTransitionOptions.withCrossFade(
            DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build()
        )
    )
    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
    .into(imageView)