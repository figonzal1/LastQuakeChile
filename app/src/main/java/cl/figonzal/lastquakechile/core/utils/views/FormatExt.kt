package cl.figonzal.lastquakechile.core.utils.views

import android.content.Context
import android.widget.ImageView
import android.widget.TextView
import cl.figonzal.lastquakechile.R
import cl.figonzal.lastquakechile.core.utils.latLongToDMS
import cl.figonzal.lastquakechile.core.utils.localDateToDHMS
import cl.figonzal.lastquakechile.core.utils.stringToLocalDateTime
import cl.figonzal.lastquakechile.quake_feature.domain.model.Quake
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Setting scale textview depending on the value of string
 *
 * @param quake Quake information
 */
fun TextView.formatScale(quake: Quake) {

    text = when (quake.scale.lowercase()) {
        "mw" -> String.format(
            QUAKE_DETAILS_SCALE_FORMAT,
            context.getString(R.string.moment_magnitude)
        )

        "mww" -> String.format(
            QUAKE_DETAILS_SCALE_FORMAT,
            context.getString(R.string.moment_magnitude_w)
        )

        else -> String.format(
            QUAKE_DETAILS_SCALE_FORMAT,
            context.getString(R.string.local_magnitude)
        )
    }
}

fun TextView.formatDepth(quake: Quake) {
    text = String.format(QUAKE_DETAILS_DEPTH_FORMAT, quake.depth)
}

/**
 * Format text for quake magnitude
 *
 * @param quake Quake information
 */
fun TextView.formatMagnitude(quake: Quake) {
    text = String.format(
        QUAKE_DETAILS_MAGNITUDE_FORMAT,
        quake.magnitude
    )
}

fun ImageView.formatFilterColor(context: Context, quake: Quake) =
    this.setColorFilter(context.getColor(getMagnitudeColor(quake.magnitude, false)))


fun TextView.formatDateTime(quake: Quake) {
    text = quake.localDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
}

/**
 * Transform localDateTime to a string text in (days or hours or minutes)
 */
fun TextView.formatQuakeTime(quake: Quake, isShortVersion: Boolean = false) {

    val timeMap = quake.localDate.stringToLocalDateTime().localDateToDHMS()
    val days = timeMap[DAYS]

    text = when {
        days != null && days == 0L -> {
            calculateTextViewBelowDay(this.context, timeMap, isShortVersion)
        }

        days != null && days > 0 -> {
            calculateTextViewAboveDay(this.context, timeMap, isShortVersion)
        }

        else -> ""
    }
}

/**
 * Coordinates to DMS
 */
fun TextView.formatDMS(quake: Quake) {

    val latDMS = quake.coordinate.latitude.latLongToDMS()
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
            quake.coordinate.latitude < 0 -> this.context.getString(R.string.south_cords)
            else -> this.context.getString(R.string.north_cords)
        }
    )

    val longDMS = quake.coordinate.longitude.latLongToDMS()
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
            quake.coordinate.longitude < 0 -> this.context.getString(R.string.west_cords)
            else -> this.context.getString(R.string.east_cords)
        }
    )

    text = String.format(QUAKE_CORDS_FORMAT, dmsLat, dmsLong)
}

fun Context.getMonth(month: Int) = arrayOf(
    getString(R.string.JAN),
    getString(R.string.FEB),
    getString(R.string.MAR),
    getString(R.string.APR),
    getString(R.string.MAY),
    getString(R.string.JUN),
    getString(R.string.JUL),
    getString(R.string.AUG),
    getString(R.string.SEP),
    getString(R.string.OCT),
    getString(R.string.NOV),
    getString(R.string.DEC)
)[month - 1]

private fun calculateTextViewBelowDay(
    context: Context,
    timeMap: Map<String, Long>,
    isShortVersion: Boolean
): String {

    val hour = timeMap[HOURS]
    val min = timeMap[MINUTES]
    val seg = timeMap[SECONDS]

    return when {
        hour != null && hour >= 1 -> when {
            isShortVersion -> String.format(QUAKETIME_H_FORMAT, hour)
            else -> String.format(context.getString(R.string.quake_time_hour_info_windows), hour)
        }

        else -> when {
            min != null && min < 1 -> when {
                isShortVersion -> String.format(
                    QUAKETIME_S_FORMAT, seg
                )

                else -> String.format(
                    context.getString(R.string.quake_time_second_info_windows), seg
                )
            }

            else -> when {
                isShortVersion -> String.format(
                    QUAKETIME_M_FORMAT,
                    min
                )

                else -> String.format(
                    context.getString(R.string.quake_time_minute_info_windows),
                    min
                )
            }
        }
    }
}


private fun calculateTextViewAboveDay(
    context: Context,
    timeMap: Map<String, Long>,
    isShortVersion: Boolean
): String {

    val days = timeMap[DAYS]
    val hour = timeMap[HOURS]

    return when {
        hour != null && hour == 0L -> when {
            isShortVersion -> String.format(
                QUAKETIME_D_FORMAT,
                days
            )

            else -> String.format(
                context.getString(R.string.quake_time_day_info_windows),
                days
            )
        }

        hour != null && hour >= 1 -> when {
            isShortVersion -> String.format(
                QUAKETIME_DH_FORMAT,
                days,
                hour / 24
            )

            else -> String.format(
                context.getString(R.string.quake_time_day_hour_info_windows),
                days,
                hour / 24
            )
        }

        else -> ""
    }
}


