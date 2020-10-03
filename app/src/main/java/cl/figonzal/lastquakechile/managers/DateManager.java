package cl.figonzal.lastquakechile.managers;

import android.content.Context;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import cl.figonzal.lastquakechile.R;

public class DateManager {

    public DateManager() {
    }

    /**
     * Funcion que calcula la diferencia en milisegundos
     * entre el tiempo del sismo y la hora actual
     *
     * @param fecha_local parametro que entrega la fecha local desde el modelo en cardview
     * @return retorna la diferencia en milisegundos
     */
    private long calculateDiff(Date fecha_local) {

        long mDiff;
        Date mCurrentTime = new Date();

        long mQuakeTime = fecha_local.getTime();
        long mActualTime = mCurrentTime.getTime();

        mDiff = mActualTime - mQuakeTime;

        return mDiff;
    }

    /**
     * Funcion encargada de entregar un mapeo los tiempos calculados y retornarlos en dias,horas,
     * minutos, segundos, de alguna fecha.
     *
     * @param fecha fecha local del modelo de sismo desde cardview
     */
    public Map<String, Long> dateToDHMS(Date fecha) {

        long mDiff = calculateDiff(fecha);
        long mSeconds = mDiff / 1000;
        long mMinutes = mSeconds / 60;
        long mHours = mMinutes / 60;
        long mDays = mHours / 24;

        Map<String, Long> mTimes = new HashMap<>();
        mTimes.put("dias", mDays);
        mTimes.put("horas", mHours);
        mTimes.put("minutos", mMinutes);
        mTimes.put("segundos", mSeconds);

        return mTimes;
    }

    /**
     * Convierte desde UTC a Local de dispositivo (Según zona horaria)
     *
     * @param date Parametro date Utc
     * @return retorna el date en local
     */
    public Date utcToLocal(Date date) {

        String mTimeZone = Calendar.getInstance().getTimeZone().getID();
        return new Date(date.getTime() + TimeZone.getTimeZone(mTimeZone).getOffset(date.getTime()));
    }


    /**
     * Funcion encargada de transformar un String a un Date
     *
     * @param sFecha Fecha en string que será convertida en date
     * @return dFecha Fecha en Date entregada por le funcion
     */
    public Date stringToDate(Context context, String sFecha) throws ParseException {

        SimpleDateFormat mFormat = new SimpleDateFormat(context.getString(R.string.DATETIME_FORMAT), Locale.US);

        return mFormat.parse(sFecha);
    }

    /**
     * Funcion que convierte una fecha date en un string
     *
     * @param context Contexto utilizado para el uso de strings
     * @param dFecha  Fecha que será convertida
     * @return String de la fecha
     */
    public String dateToString(Context context, Date dFecha) {

        SimpleDateFormat mFormat = new SimpleDateFormat(context.getString(R.string.DATETIME_FORMAT), Locale.US);

        return mFormat.format(dFecha);
    }

    /**
     * Funcion encargada de sumar horas a un date
     *
     * @param date  Date al que se le sumaran horas
     * @param hours Horas que seran sumadas
     * @return Date con las horas ya sumadas
     */
    public Date addHoursToJavaUtilDate(Date date, int hours) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.HOUR_OF_DAY, hours);

        return calendar.getTime();
    }

    /**
     * Funcion encargada de setear el tiempo en los text views
     *
     * @param context Contexto para utilizar recursos
     * @param tiempos Variable que cuenta con el mapeo de dias,horas,minutos y segundos
     * @param tv_hora Textview que será usado para fijar el tiempo
     */
    public void setTimeToTextView(Context context, Map<String, Long> tiempos,
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
}
