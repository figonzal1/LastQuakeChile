package cl.figonzal.lastquakechile;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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
    private static long calculateDiff(Date fecha_local) {

        long diff;
        Date currentTime = new Date();

        long sismo_tiempo = fecha_local.getTime();
        long actual_tiempo = currentTime.getTime();

        diff = actual_tiempo - sismo_tiempo;

        return diff;

    }

    /**
     * Funcion encargada de entregar los tiempos calculados y retornarlos en dias,horas,minutos,segundos
     * @param fecha_local fecha local del modelo de sismo desde cardview
     */
    static Map<String, Long> timeToText(Date fecha_local) {

        long diff = calculateDiff(fecha_local);
        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        Map<String, Long> tiempos = new HashMap<>();
        tiempos.put("dias", days);
        tiempos.put("horas", hours);
        tiempos.put("minutos", minutes);
        tiempos.put("segundos", seconds);

        return tiempos;
    }

    /**
     * Funcion encargada de setear los colores de background
     * dependiendo de la magnitud del sismo
     *
     * @param magnitude Magnitud del sismo desde el cardview
     * @return id recurso desde colors.xml
     */
    static int getMagnitudeColor(double magnitude) {

        int mag_floor = (int) Math.floor(magnitude);
        int mag_resource_id = 0;
        switch (mag_floor) {

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
                mag_resource_id = R.color.magnitude8;
                break;
        }
        return mag_resource_id;
    }

    /**
     * Funcion encargada de checkear si el celular tiene red movil o wifi activada
     *
     * @param context es recibido para hacer uso del getSystemService
     * @return boolean, true para conectado, false celular sin conexion
     */
    static boolean checkInternet(Context context) {

        //Zona Network Information
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();

        return networkInfo != null && networkInfo.isConnected();
    }
}
