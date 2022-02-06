package cl.figonzal.lastquakechile.core

import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import cl.figonzal.lastquakechile.R
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
    estado: String,
    tv_estado: TextView
) {

    when {
        estado.contains("preliminar") -> {
            tv_estado.text = String.format(
                Locale.US,
                context.getString(R.string.quakes_details_estado_sismo),
                context.getString(R.string.quakes_details_estado_sismo_preliminar)
            )
            this.setImageDrawable(
                ContextCompat.getDrawable(
                    context,
                    R.drawable.ic_progress_check_24
                )
            )
        }
        estado.contains("verificado") -> {

            tv_estado.text = String.format(
                Locale.US,
                context.getString(R.string.quakes_details_estado_sismo),
                context.getString(R.string.quakes_details_estado_sismo_verificado)
            )
            this.setImageDrawable(
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
    if (escala.contains("Mw")) {
        this.text = String.format(
            context.getString(R.string.quake_details_escala),
            context.getString(R.string.quake_details_magnitud_momento)
        )
    } else {
        this.text = String.format(
            context.getString(R.string.quake_details_escala),
            context.getString(R.string.quake_details_magnitud_local)
        )
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
