package cl.figonzal.lastquakechile.core.utils

import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.roundToLong


data class Elapsed(val days: Long, val hours: Long, val minutes: Long, val seconds: Long)

/**
 * Time elapsed between this LocalDateTime and now
 */
fun LocalDateTime.toElapsed(): Elapsed {

    val currentTime = LocalDateTime.now()

    val mDiff = Duration.between(this, currentTime).toMillis()
    val mSeconds = mDiff / 1000
    val mMinutes = mSeconds / 60
    val mHours = mMinutes / 60
    val mDays = mHours / 24

    return Elapsed(mDays, mHours, mMinutes, mSeconds)
}

data class DMS(val degrees: Double, val minutes: Double, val seconds: Double)

/**
 * Lat or Long to Degree/Minutes/Seconds
 */
fun Double.toDMS(): DMS {

    val abs = abs(this)

    val degree = floor(abs)
    val minutes = floor((abs - degree) * 3600 / 60)
    val seg = ((abs - degree) * 3600 / 60 - minutes) * 60

    return DMS(degree, minutes.roundToLong().toDouble(), seg.roundToLong().toDouble())
}

fun LocalDateTime.localDateTimeToString(): String {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    return this.format(formatter)
}

fun String.stringToLocalDateTime(): LocalDateTime {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    return LocalDateTime.parse(this, formatter)
}

/**
 * Convert utcLocalDateTime to device localDateTime
 */
fun LocalDateTime.utcToLocalDate(): LocalDateTime =
    this.atZone(ZoneOffset.UTC).withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime()