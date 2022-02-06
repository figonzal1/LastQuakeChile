package cl.figonzal.lastquakechile.core.utils

import android.content.Context
import cl.figonzal.lastquakechile.R
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter.ofPattern
import java.util.*


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
 * Convierte desde UTC a Local de dispositivo (Según zona horaria)
 *
 * @param date Parametro date Utc
 * @return retorna el date en local
 */
fun utcToLocal(date: LocalDateTime): LocalDateTime {

    // convert LocalDateTime to ZonedDateTime, with default system zone id
    return date.atZone(ZoneId.systemDefault()).toLocalDateTime()
}

/**
 * Funcion encargada de transformar un String a un Date
 *
 * @param sFecha Fecha en string que será convertida en date
 * @return dFecha Fecha en Date entregada por le funcion
 */
fun stringToDate(context: Context, sFecha: String): LocalDateTime {

    return LocalDateTime.parse(sFecha, ofPattern(context.getString(R.string.DATETIME_FORMAT)))
}

/**
 * Funcion que convierte una fecha date en un string
 *
 * @param context Contexto utilizado para el uso de strings
 * @param dFecha  Fecha que será convertida
 * @return String de la fecha
 */
fun dateToString(context: Context, dFecha: Date): String {
    val mFormat = SimpleDateFormat(context.getString(R.string.DATETIME_FORMAT), Locale.US)
    return mFormat.format(dFecha)
}

/**
 * Funcion encargada de sumar horas a un date
 *
 * @param date  Date al que se le sumaran horas
 * @param hours Horas que seran sumadas
 * @return Date con las horas ya sumadas
 */
fun addHoursToJavaUtilDate(date: LocalDateTime, hours: Long): LocalDateTime {
    return date.plusHours(hours)
}
