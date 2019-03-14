package cl.figonzal.lastquakechile.services;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import cl.figonzal.lastquakechile.R;

public class QuakeUtils {

    /**
     * Funcion que calcula la diferencia en milisegundos
     * entre el tiempo del sismo y la hora actual
     *
     * @param fecha_local parametro que entrega la fecha local desde el modelo en cardview
     * @return retorna la diferencia en milisegundos
     */
    public static long calculateDiff(Date fecha_local) {

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
        int mag_resource_id;
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

    /**
     * Funcion que permite cambiaar latitud o longitud a DMS
     *
     * @param input Longitud o Latitud
     * @return grados, minutos, segundos en un Map
     */
    public static Map<String, Double> toDMS(double input) {

        Map<String, Double> dms = new HashMap<>();

        double abs = Math.abs(input);

        double lat_grados_rest = Math.floor(abs); //71
        double minutes = Math.floor((((abs - lat_grados_rest) * 3600) / 60)); // 71.43 -71 = 0.43 =25.8 = 25
        //(71.43 - 71)*3600 /60 - (71.43-71)*3600/60 = 25.8 - 25 =0.8
        double seconds = ((((abs - lat_grados_rest) * 3600) / 60) - Math.floor((((abs - lat_grados_rest) * 3600) / 60))) * 60;

        dms.put("grados", Math.floor(Math.abs(input)));
        dms.put("minutos", (double) Math.round(minutes));
        dms.put("segundos", (double) Math.round(seconds));

        return dms;
    }

    /**
     * Funcion que guardar una imagen en cache desde la descarga de glide
     *
     * @param drawable imagen de la cual se buscara la ruta
     * @param context  contexto de la actividad
     * @return Uri retorna la direccion dentro del celular donde esta la imagen
     */
    public static Uri getLocalBitmapUri(Drawable drawable, Context context) {

        Bitmap bmp;

        if (drawable instanceof BitmapDrawable) {
            bmp = ((BitmapDrawable) drawable).getBitmap();
        } else {
            return null;
        }

        Uri bmpUri = null;

        try {
            File file = new File(context.getCacheDir(), "share_image_" + System.currentTimeMillis() + ".jpeg");
            FileOutputStream out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.close();

            bmpUri = FileProvider.getUriForFile(context, "cl.figonzal.lastquakechile.fileprovider", file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bmpUri;

    }

    /**
     * Funcion que realiza la instalacion de un paquete dado
     *
     * @param packageName Nombre del paquete
     * @param context     Contexto que permite utilizar recursos de strings
     */
    public static void doInstallation(String packageName, Context context) {

        Intent intent;
        try {
            //Intenta abrir google play
            intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setData(Uri.parse("market://details?id=" + packageName));

            //LOG
            Log.d(context.getString(R.string.TAG_INTENT), context.getString(R.string.TAG_INTENT_GOOGLEPLAY));
            Crashlytics.log(Log.DEBUG, context.getString(R.string.TAG_INTENT), context.getString(R.string.TAG_INTENT_GOOGLEPLAY));

            context.startActivity(intent);
        } catch (android.content.ActivityNotFoundException anfe) {

            //Si gogle play no esta abre webview
            Log.d(context.getString(R.string.TAG_INTENT), context.getString(R.string.TAG_INTENT_NAVEGADOR));
            Crashlytics.log(Log.DEBUG, context.getString(R.string.TAG_INTENT), context.getString(R.string.TAG_INTENT_NAVEGADOR));

            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + packageName)));
        }
    }

}
