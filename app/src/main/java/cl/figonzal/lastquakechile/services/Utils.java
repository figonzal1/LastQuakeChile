package cl.figonzal.lastquakechile.services;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import cl.figonzal.lastquakechile.R;

public class Utils {



    /**
     * Funcion encargada de setear los colores de background
     * dependiendo de la magnitud del sismo
     *
     * @param magnitude Magnitud del sismo desde el cardview
     * @return id recurso desde colors.xml
     */
    public static int getMagnitudeColor(double magnitude, boolean forMapa) {

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
     * Funcion que permite cambiaar latitud o longitud a DMS
     *
     * @param input Longitud o Latitud
     * @return grados, minutos, segundos en un Map
     */
    public static Map<String, Double> latLonToDMS(double input) {

        Map<String, Double> mDMS = new HashMap<>();

        double abs = Math.abs(input);

        double mLatGradosLet = Math.floor(abs); //71
        double mMinutes = Math.floor((((abs - mLatGradosLet) * 3600) / 60)); // 71.43 -71 = 0.43
        // =25.8 = 25
        //(71.43 - 71)*3600 /60 - (71.43-71)*3600/60 = 25.8 - 25 =0.8
        double mSeconds = ((((abs - mLatGradosLet) * 3600) / 60) - mMinutes) * 60;

        mDMS.put("grados", Math.floor(Math.abs(input)));
        mDMS.put("minutos", (double) Math.round(mMinutes));
        mDMS.put("segundos", (double) Math.round(mSeconds));

        return mDMS;
    }

    /**
     * Funcion encargada se guardar en directorio de celular una imagen bitmap
     *
     * @param bitmap  Bitmap de la imagen
     * @param context Contexto necesario para usar recursos
     * @return Path de la imagen
     */
    public static Uri getLocalBitmapUri(Bitmap bitmap, Context context) throws IOException {

        File mFile = new File(context.getCacheDir(), "share_image_" + System.currentTimeMillis() + ".jpeg");
        FileOutputStream out = new FileOutputStream(mFile);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        out.close();

        return FileProvider.getUriForFile(context, "cl.figonzal.lastquakechile.fileprovider", mFile);
    }

    /**
     * Funcion que realiza la instalacion de un paquete dado
     *
     * @param packageName Nombre del paquete
     * @param context     Contexto que permite utilizar recursos de strings
     */
    public static void doInstallation(String packageName, Context context) {

        FirebaseCrashlytics crashlytics = FirebaseCrashlytics.getInstance();

        Intent mIntent;

        try {
            //Intenta abrir google play
            mIntent = new Intent(Intent.ACTION_VIEW);
            mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mIntent.setData(Uri.parse("market://details?id=" + packageName));

            //LOG
            Log.d(context.getString(R.string.TAG_INTENT), context.getString(R.string.TAG_INTENT_GOOGLEPLAY));
            crashlytics.log(context.getString(R.string.TAG_INTENT) + context.getString(R.string.TAG_INTENT_GOOGLEPLAY));

            context.startActivity(mIntent);

        } catch (android.content.ActivityNotFoundException anfe) {

            //Si gogle play no esta abre webview
            Log.d(context.getString(R.string.TAG_INTENT), context.getString(R.string.TAG_INTENT_NAVEGADOR));
            crashlytics.log(context.getString(R.string.TAG_INTENT) + context.getString(R.string.TAG_INTENT_NAVEGADOR));

            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google" + ".com/store/apps/details?id=" + packageName)));
        }
    }


    /**
     * Funcion encargada de setear el tiempo en los text views
     *
     * @param context Contexto para utilizar recursos
     * @param tiempos Variable que cuenta con el mapeo de dias,horas,minutos y segundos
     * @param tv_hora Textview que será usado para fijar el tiempo
     */
    public static void setTimeToTextView(Context context, Map<String, Long> tiempos,
                                         TextView tv_hora) {
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

    /**
     * Funcion que permite setear la imagen de estado del sismos (Preliminar o Verificado)
     *
     * @param context   Contexto que permite acceder a los recursos de strings
     * @param estado    Estado del sismos (preliminar/verificado)
     * @param tv_estado Texview que tendrá el valor de estado
     * @param iv_estado ImageView fijada dependiendo del valor de estado
     */
    public static void setStatusImage(Context context, String estado, TextView tv_estado, ImageView iv_estado) {

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
    public static void setEscala(Context context, String escala, TextView tv_escala) {

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
     * Funcion encargada de corregir el bug de en modo noche procovado por adviews
     *
     * @param activity Actividad desde donde proviene el adview
     */
    private static void fixAdViewNightMode(Activity activity) {

        Log.d(activity.getString(R.string.tag_adview_night_mode), activity.getString(R.string.tag_adview_night_mode_response));

        try {
            new WebView(activity.getApplicationContext());
        } catch (Exception e) {
            Log.e(activity.getString(R.string.tag_adview_night_mode), activity.getString(R.string.tag_adview_night_mode_response_error), e);
        }
    }


    /**
     * Funcion encargada de generar un numero aleatorio para dialogs.
     *
     * @return Booleano con el resultado
     */
    static boolean generateRandomNumber() {

        Random random = new Random();
        int item = random.nextInt(10);
        return item % 3 == 0;
    }
}
