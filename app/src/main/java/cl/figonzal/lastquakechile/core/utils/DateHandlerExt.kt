package cl.figonzal.lastquakechile.core.utils

import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.roundToLong


/**
 * LocalDateTime to Map<D,H,M,S>
 */
fun LocalDateTime.localDateToDHMS(): Map<String, Long> {

    val currentTime = LocalDateTime.now()

    val mDiff = Duration.between(this, currentTime).toMillis()
    val mSeconds = mDiff / 1000
    val mMinutes = mSeconds / 60
    val mHours = mMinutes / 60
    val mDays = mHours / 24

    return mutableMapOf(
        "days" to mDays,
        "hours" to mHours,
        "minutes" to mMinutes,
        "seconds" to mSeconds
    )
}

/**
 * Lat or Long to Map<Degree,Hours,Miutes,Seconds>
 */
fun Double.latLongToDMS(): Map<String, Double> {

    val dmsMap = HashMap<String, Double>()
    val abs = abs(this)

    val degree = floor(abs)
    val minutes = floor((abs - degree) * 3600 / 60)
    val seg = ((abs - degree) * 3600 / 60 - minutes) * 60

    dmsMap["grados"] = floor(abs(this))
    dmsMap["minutos"] = minutes.roundToLong().toDouble()
    dmsMap["segundos"] = seg.roundToLong().toDouble()
    return dmsMap
}

fun LocalDateTime.localDateTimeToString(): String {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    return this.format(formatter)
}

fun String.stringToLocalDateTime(): LocalDateTime {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    return LocalDateTime.parse(this, formatter)
}
