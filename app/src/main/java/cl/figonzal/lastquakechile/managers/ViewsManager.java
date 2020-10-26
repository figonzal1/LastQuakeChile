package cl.figonzal.lastquakechile.managers;

import android.content.Context;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import java.util.Locale;
import java.util.Map;

import cl.figonzal.lastquakechile.R;

public class ViewsManager {

    public ViewsManager() {
    }

    /**
     * Funcion encargada de setear los colores de background
     * dependiendo de la magnitud del sismo
     *
     * @param magnitude Magnitud del sismo desde el cardview
     * @return id recurso desde colors.xml
     */
    public int getMagnitudeColor(double magnitude, boolean forMapa) {

        int mMagFloor = (int) Math.floor(magnitude);
        int mMagResourseId;

        if (mMagFloor == 1) {
            if (forMapa) {
                mMagResourseId = R.color.magnitude1_alpha;
            } else {
                mMagResourseId = R.color.magnitude1;
            }
        } else if (mMagFloor == 2) {
            if (forMapa) {
                mMagResourseId = R.color.magnitude2_alpha;
            } else {
                mMagResourseId = R.color.magnitude2;
            }
        } else if (mMagFloor == 3) {
            if (forMapa) {
                mMagResourseId = R.color.magnitude3_alpha;
            } else {
                mMagResourseId = R.color.magnitude3;
            }
        } else if (mMagFloor == 4) {
            if (forMapa) {
                mMagResourseId = R.color.magnitude4_alpha;
            } else {
                mMagResourseId = R.color.magnitude4;
            }
        } else if (mMagFloor == 5) {
            if (forMapa) {
                mMagResourseId = R.color.magnitude5_alpha;
            } else {
                mMagResourseId = R.color.magnitude5;
            }
        } else if (mMagFloor == 6) {
            if (forMapa) {
                mMagResourseId = R.color.magnitude6_alpha;
            } else {
                mMagResourseId = R.color.magnitude6;
            }
        } else if (mMagFloor == 7) {
            if (forMapa) {
                mMagResourseId = R.color.magnitude7_alpha;
            } else {
                mMagResourseId = R.color.magnitude7;
            }
        } else if (mMagFloor >= 8) {
            if (forMapa) {
                mMagResourseId = R.color.magnitude8_alpha;
            } else {
                mMagResourseId = R.color.magnitude8;
            }
        } else {
            mMagResourseId = R.color.colorPrimary;
        }

        return mMagResourseId;
    }


    /**
     * Funcion que permite setear la imagen de estado del sismos (Preliminar o Verificado)
     *
     * @param context   Contexto que permite acceder a los recursos de strings
     * @param estado    Estado del sismos (preliminar/verificado)
     * @param tv_estado Texview que tendrá el valor de estado
     * @param iv_estado ImageView fijada dependiendo del valor de estado
     */
    public void setStatusImage(@NonNull Context context, @NonNull String estado, @NonNull TextView tv_estado, @NonNull ImageView iv_estado) {

        if (estado.contains("preliminar")) {

            tv_estado.setText(String.format(Locale.US, context.getString(R.string.quakes_details_estado_sismo), context.getString(R.string.quakes_details_estado_sismo_preliminar)));
            iv_estado.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_progress_check_24));

        } else if (estado.contains("verificado")) {

            tv_estado.setText(String.format(Locale.US, context.getString(R.string.quakes_details_estado_sismo), context.getString(R.string.quakes_details_estado_sismo_verificado)));
            iv_estado.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_baseline_check_circle_24px));
        }
    }

    /**
     * Funcion que permite setear el textview de escala dependiendo del valor del string
     *
     * @param context   Contexto utilizado para el uso de recursos
     * @param escala    Escala del sismo puede ser Ml o Mw
     * @param tv_escala Textview que será fijado con el valor de escala
     */
    public void setEscala(@NonNull Context context, @NonNull String escala, @NonNull TextView tv_escala) {

        switch (escala) {

            case "Ml":
                tv_escala.setText(String.format(context.getString(R.string.quake_details_escala), context.getString(R.string.quake_details_magnitud_local)));
                break;

            case "Mw":
                tv_escala.setText(String.format(context.getString(R.string.quake_details_escala), context.getString(R.string.quake_details_magnitud_momento)));
                break;

        }
    }

    /**
     * Funcion encargada de setear el tiempo en los text views
     *
     * @param context Contexto para utilizar recursos
     * @param tiempos Variable que cuenta con el mapeo de dias,horas,minutos y segundos
     * @param tv_hora Textview que será usado para fijar el tiempo
     */
    public void setTimeToTextView(@NonNull Context context, @NonNull Map<String, Long> tiempos, @NonNull TextView tv_hora) {
        Long mDays = tiempos.get(context.getString(R.string.UTILS_TIEMPO_DIAS));
        Long mMinutes = tiempos.get(context.getString(R.string.UTILS_TIEMPO_MINUTOS));
        Long mHours = tiempos.get(context.getString(R.string.UTILS_TIEMPO_HORAS));
        Long mSeconds = tiempos.get(context.getString(R.string.UTILS_TIEMPO_SEGUNDOS));

        //Condiciones días.
        if (mDays != null && mDays == 0) {

            if (mHours != null && mHours >= 1) {

                tv_hora.setText(String.format(context.getString(R.string.quake_time_hour), mHours));

            } else {

                tv_hora.setText(String.format(context.getString(R.string.quake_time_minute), mMinutes));

                if (mMinutes != null && mMinutes < 1) {

                    tv_hora.setText(String.format(context.getString(R.string.quake_time_second), mSeconds));
                }
            }

        } else if (mDays != null && mDays > 0) {

            if (mHours != null && mHours == 0) {

                tv_hora.setText(String.format(context.getString(R.string.quake_time_day), mDays));

            } else if (mHours != null && mHours >= 1) {

                tv_hora.setText(String.format(context.getString(R.string.quake_time_day_hour), mDays, mHours / 24));
            }
        }
    }
}
