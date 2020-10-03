package cl.figonzal.lastquakechile.managers;

import android.content.Context;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import java.util.Locale;

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

        switch (mMagFloor) {

            case 1:

                if (forMapa) {
                    mMagResourseId = R.color.magnitude1_alpha;
                } else {
                    mMagResourseId = R.color.magnitude1;
                }

                break;

            case 2:

                if (forMapa) {
                    mMagResourseId = R.color.magnitude2_alpha;
                } else {
                    mMagResourseId = R.color.magnitude2;
                }

                break;

            case 3:

                if (forMapa) {
                    mMagResourseId = R.color.magnitude3_alpha;
                } else {
                    mMagResourseId = R.color.magnitude3;
                }

                break;

            case 4:

                if (forMapa) {
                    mMagResourseId = R.color.magnitude4_alpha;
                } else {
                    mMagResourseId = R.color.magnitude4;
                }

                break;

            case 5:

                if (forMapa) {
                    mMagResourseId = R.color.magnitude5_alpha;
                } else {
                    mMagResourseId = R.color.magnitude5;
                }

                break;

            case 6:

                if (forMapa) {
                    mMagResourseId = R.color.magnitude6_alpha;
                } else {
                    mMagResourseId = R.color.magnitude6;
                }
                break;

            case 7:

                if (forMapa) {
                    mMagResourseId = R.color.magnitude7_alpha;
                } else {
                    mMagResourseId = R.color.magnitude7;
                }
                break;

            case 8:
            case 9:

                if (forMapa) {
                    mMagResourseId = R.color.magnitude8_alpha;
                } else {
                    mMagResourseId = R.color.magnitude8;
                }

                break;

            //Si no, se elige color por defecto
            default:
                mMagResourseId = R.color.colorSecondary;
                break;
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
    public void setStatusImage(Context context, String estado, TextView tv_estado, ImageView iv_estado) {

        if (estado.equals("preliminar")) {

            tv_estado.setText(String.format(Locale.US, context.getString(R.string.quakes_details_estado_sismo), context.getString(R.string.quakes_details_estado_sismo_preliminar)));
            iv_estado.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_progress_check_24));

        } else if (estado.equals("verificado")) {

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
    public void setEscala(Context context, String escala, TextView tv_escala) {

        switch (escala) {

            case "Ml":
                tv_escala.setText(String.format(context.getString(R.string.quake_details_escala), context.getString(R.string.quake_details_magnitud_local)));
                break;

            case "Mw":
                tv_escala.setText(String.format(context.getString(R.string.quake_details_escala), context.getString(R.string.quake_details_magnitud_momento)));
                break;

        }
    }
}
