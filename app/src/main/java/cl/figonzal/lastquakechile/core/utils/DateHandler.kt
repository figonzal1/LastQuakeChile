package cl.figonzal.lastquakechile.core.utils

import java.time.Duration
import java.time.LocalDateTime
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.roundToLong


/**
 * Funcion encargada de entregar un mapeo los tiempos calculados y retornarlos en dias,horas,
 * minutos, segundos, de alguna fecha.
 *
 * @param fecha fecha local del modelo de sismo desde cardview
 */
fun dateToDHMS(fecha: LocalDateTime): Map<String, Long> {

    val currentTime = LocalDateTime.now()

    val mDiff = Duration.between(fecha, currentTime).toMillis()
    val mSeconds = mDiff / 1000
    val mMinutes = mSeconds / 60
    val mHours = mMinutes / 60
    val mDays = mHours / 24

    return mutableMapOf(
        "dias" to mDays,
        "horas" to mHours,
        "minutos" to mMinutes,
        "segundos" to mSeconds
    )
}

/**
 * Funcion que permite cambiaar latitud o longitud a DMS
 *
 * @param input Longitud o Latitud
 * @return grados, minutos, segundos en un Map
 */
fun latLongToDMS(input: Double): Map<String, Double> {

    val dmsMap = HashMap<String, Double>()
    val abs = abs(input)

    val degree = floor(abs)
    val minutes = floor((abs - degree) * 3600 / 60)
    val seg = ((abs - degree) * 3600 / 60 - minutes) * 60

    dmsMap["grados"] = floor(abs(input))
    dmsMap["minutos"] = minutes.roundToLong().toDouble()
    dmsMap["segundos"] = seg.roundToLong().toDouble()
    return dmsMap
}
