package cl.figonzal.lastquakechile;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class QuakeUtils {

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
     * Convierte desde UTC a Local de dispositivo
     *
     * @param date Parametro date Utc
     * @return retorna el date en local
     */
    public static Date utcToLocal(Date date) {

        String timeZone = Calendar.getInstance().getTimeZone().getID();
        return new Date(date.getTime() + TimeZone.getTimeZone(timeZone).getOffset(date.getTime()));
    }

    /**
     * Funcion encargada de entregar los tiempos calculados y retornarlos en dias,horas,minutos,segundos
     * @param fecha fecha local del modelo de sismo desde cardview
     */
    public static Map<String, Long> timeToText(Date fecha) {

        long diff = calculateDiff(fecha);
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
    public static int getMagnitudeColor(double magnitude) {

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
                mag_resource_id = R.color.magnitude8;
                break;

            //Si no nignuno se elige color por defecto
            default:
                mag_resource_id = R.color.colorAccent;
                break;
        }
        return mag_resource_id;
    }

}
