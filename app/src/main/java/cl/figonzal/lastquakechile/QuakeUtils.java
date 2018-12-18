package cl.figonzal.lastquakechile;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.util.Date;

class QuakeUtils {

    QuakeUtils() {

    }

    /**
     * Funcion que calcula la diferencia en milisegundos
     * entre el tiempo del sismo y la hora actual
     *
     * @param fecha_local parametro que entrega la fecha local desde el modelo en cardview
     * @return retorna la diferencia en milisegundos
     */
    private long calculateDiff(Date fecha_local) {

        long diff;
        Date currentTime = new Date();

        long sismo_tiempo = fecha_local.getTime();
        long actual_tiempo = currentTime.getTime();

        diff = actual_tiempo - sismo_tiempo;

        return diff;

    }

    /**
     * Funcion encargada de setear el texto en el cardview
     * segun los dias que han pasado del sismo
     *
     * @param context     Contexto necesario para usar los recursos strings de formato
     * @param fecha_local fecha local del modelo de sismo desde cardview
     * @param holder      viewholder que permite acceder a los textviews del cardview
     */
    void timeToText(Context context, Date fecha_local, QuakeAdapter.QuakeViewHolder holder) {

        long diff = calculateDiff(fecha_local);
        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        //Condiciones días.
        if (days == 0) {

            if (hours >= 1) {
                holder.tv_hora.setText(String.format(context.getString(R.string.quake_time_hour), hours));
            } else {
                holder.tv_hora.setText(String.format(context.getString(R.string.quake_time_minute), minutes));

                if (minutes < 1) {
                    holder.tv_hora.setText(String.format(context.getString(R.string.quake_time_second), seconds));
                }
            }
        } else if (days > 0) {

            if (hours == 0) {
                holder.tv_hora.setText(String.format(context.getString(R.string.quake_time_day), days));
            } else if ((hours >= 1)) {
                holder.tv_hora.setText(String.format(context.getString(R.string.quake_time_day_hour), days, hours / 24));
            }
        }
    }

    /**
     * Funcion encargada de setear los colores de background
     * dependiendo de la magnitud del sismo
     *
     * @param magnitude Magnitud del sismo desde el cardview
     * @return id recurso desde colors.xml
     */
    int getMagnitudeColor(double magnitude) {

        int mag_floor = (int) Math.floor(magnitude);
        int mag_resource_id = 0;
        switch (mag_floor) {

            case 1:
                mag_resource_id = R.color.magnitude1;
                break;
            case 2:
                mag_resource_id = R.color.magnitude2;
                break;
            case 3:
                mag_resource_id = R.color.magnitude3;
                break;
            case 4:
                mag_resource_id = R.color.magnitude4;
                break;
            case 5:
                mag_resource_id = R.color.magnitude5;
                break;
            case 6:
                mag_resource_id = R.color.magnitude6;
                break;
            case 7:
                mag_resource_id = R.color.magnitude7;
                break;
            case 8:
                mag_resource_id = R.color.magnitude8;
                break;
            case 9:
                mag_resource_id = R.color.magnitude9plus;
                break;
        }
        return mag_resource_id;
    }


    /**
     * Funcion que permite checkear si el celular cuenta con conexion a internet tanto wifi como datos moviles.
     *
     * @param context Contexto necesario para realizar las llamadas a servicios
     * @return boolean  Retorna un boleano True para internet, False cuando esta desconectado
     */
    static boolean checkInternet(Context context) {

        //Zona Network Information
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();

        return networkInfo != null && networkInfo.isConnected();
    }
}
