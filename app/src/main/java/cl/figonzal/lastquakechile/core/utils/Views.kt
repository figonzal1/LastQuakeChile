package cl.figonzal.lastquakechile.core.utils

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import cl.figonzal.lastquakechile.R
import cl.figonzal.lastquakechile.quake_feature.domain.model.Coordinates
import cl.figonzal.lastquakechile.quake_feature.domain.model.Quake
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import kotlin.math.floor

/**
 * Funcion encargada de setear los colores de background
 * dependiendo de la magnitud del sismo
 *
 * @param magnitude Magnitud del sismo desde el cardview
 * @return id recurso desde colors.xml
 */
fun getMagnitudeColor(magnitude: Double, forMapa: Boolean): Int {
    val mMagFloor = floor(magnitude).toInt()
    return getColorResource(forMapa, mMagFloor)
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
 * Funcion que permite setear la imagen de estado del sismos (Preliminar o Verificado)
 *
 * @param estado    Estado del sismos (preliminar/verificado)
 * @param tv_estado Texview que tendrá el valor de estado
 */
fun ImageView.setStatusImage(
    estado: Boolean,
    tv_estado: TextView
) {

    when {
        !estado -> {
            tv_estado.text = String.format(
                Locale.US,
                context.getString(R.string.quakes_details_estado_sismo),
                context.getString(R.string.quakes_details_estado_sismo_preliminar)
            )
            setImageDrawable(
                ContextCompat.getDrawable(
                    context,
                    R.drawable.ic_progress_check_24
                )
            )
        }
        estado -> {

            tv_estado.text = String.format(
                Locale.US,
                context.getString(R.string.quakes_details_estado_sismo),
                context.getString(R.string.quakes_details_estado_sismo_verificado)
            )
            setImageDrawable(
                ContextCompat.getDrawable(
                    context,
                    R.drawable.ic_baseline_check_circle_24px
                )
            )
        }
    }
}

/**
 * Funcion que permite setear el textview de escala dependiendo del valor del string
 *
 * @param escala    Escala del sismo puede ser Ml o Mw
 */
fun TextView.setEscala(escala: String) {
    text = when {
        escala.contains("Mw") -> {
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
 * Funcion encargada de setear el tiempo en los text views
 *
 * @param tiempos Variable que cuenta con el mapeo de dias,horas,minutos y segundos
 */
fun TextView.setTimeToTextView(tiempos: Map<String, Long>) {

    val mDays: Long = tiempos[context.getString(R.string.UTILS_TIEMPO_DIAS)]!!
    val mMinutes: Long = tiempos[context.getString(R.string.UTILS_TIEMPO_MINUTOS)]!!
    val mHours: Long = tiempos[context.getString(R.string.UTILS_TIEMPO_HORAS)]!!
    val mSeconds: Long = tiempos[context.getString(R.string.UTILS_TIEMPO_SEGUNDOS)]!!

    //Condiciones días.
    when {
        mDays == 0L -> {
            when {
                mHours >= 1 ->
                    this.text = String.format(context.getString(R.string.quake_time_hour), mHours)
                else -> {
                    this.text =
                        String.format(context.getString(R.string.quake_time_minute), mMinutes)
                    if (mMinutes < 1) {
                        this.text =
                            String.format(context.getString(R.string.quake_time_second), mSeconds)
                    }
                }
            }
        }
        mDays > 0 -> {
            when {
                mHours == 0L -> this.text =
                    String.format(context.getString(R.string.quake_time_day), mDays)
                mHours >= 1 -> this.text =
                    String.format(
                        context.getString(R.string.quake_time_day_hour),
                        mDays,
                        mHours / 24
                    )
            }
        }
    }
}

fun Context.toast(stringId: Int) {
    Toast.makeText(
        this,
        this.getString(stringId),
        Toast.LENGTH_LONG
    ).show()
}

fun TextView.calculateHours(quake: Quake, context: Context) {

    val timeMap = dateToDHMS(quake.localDate)

    val days = timeMap[context.getString(R.string.UTILS_TIEMPO_DIAS)]
    val hour = timeMap[context.getString(R.string.UTILS_TIEMPO_HORAS)]
    val min = timeMap[context.getString(R.string.UTILS_TIEMPO_MINUTOS)]
    val seg = timeMap[context.getString(R.string.UTILS_TIEMPO_SEGUNDOS)]

    //Condiciones días.
    when {
        days != null && days == 0L -> {

            when {
                hour != null && hour >= 1 -> text =
                    String.format(context.getString(R.string.quake_time_hour_info_windows), hour)
                else -> {
                    text = String.format(
                        context.getString(R.string.quake_time_minute_info_windows),
                        min
                    )

                    if (min != null && min < 1) text = String.format(
                        context.getString(R.string.quake_time_second_info_windows), seg
                    )
                }
            }
        }
        days != null && days > 0 -> {
            when {
                hour != null && hour == 0L -> text =
                    String.format(context.getString(R.string.quake_time_day_info_windows), days)
                hour != null && hour >= 1 -> text = String.format(
                    context.getString(R.string.quake_time_day_hour_info_windows), days, hour / 24
                )
            }
        }
    }
}

/**
 * Coordinates to DMS
 */
fun TextView.formatDMS(coordinates: Coordinates) {

    //Calculo de lat to GMS
    val latDMS = latLongToDMS(coordinates.latitude)
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

    //Calculo de long to GMS
    val longDMS = latLongToDMS(coordinates.longitude)
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
 * Funcion encargada se guardar en directorio de celular una imagen bitmap
 *
 * @param bitmap  Bitmap de la imagen
 * @param context Contexto necesario para usar recursos
 * @return Path de la imagen
 */
@Throws(IOException::class)
fun Context.getLocalBitmapUri(bitmap: Bitmap): Uri {

    val c = Calendar.getInstance()
    val date = c.timeInMillis.toInt()
    val file = File(this.cacheDir, "share$date.jpeg")

    when {
        file.exists() -> Timber.i("Share image exist")
        else -> {
            Timber.i("Share image not exist")
            val out = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
            out.close()
        }
    }
    return FileProvider.getUriForFile(this, "cl.figonzal.lastquakechile.fileprovider", file)
}