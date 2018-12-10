package cl.figonzal.lastquakechile;

import android.content.Context;

import java.util.Date;

class QuakeTime {

    QuakeTime(){

    }

    private long calculateDiff(Date fecha_local){

        long diff=0;
        Date currentTime = new Date();

        long sismo_tiempo = fecha_local.getTime();
        long actual_tiempo = currentTime.getTime();

        diff = actual_tiempo-sismo_tiempo;

        return diff;

    }

    void timeToText(Context context, Date fecha_local, QuakeAdapter.QuakeViewHolder holder){

        long diff = calculateDiff(fecha_local);
        long seconds = diff/1000;
        long minutes = seconds/60;
        long hours = minutes /60;
        long days = hours/24;

        //Condiciones días.
        if (days==0){

            if (hours>=1) {
                holder.tv_hora.setText(String.format(context.getString(R.string.quake_time_hour),hours));
            }

            else if (hours<1){
                holder.tv_hora.setText(String.format(context.getString(R.string.quake_time_minute),minutes));

                if(minutes<1){
                    holder.tv_hora.setText(String.format(context.getString(R.string.quake_time_second),seconds));
                }
            }
        }
        else if (days >0){
            if (days>=1){

                if (hours==0){
                    holder.tv_hora.setText(String.format(context.getString(R.string.quake_time_day),days));
                }
                else if ((hours>=1)) {
                    holder.tv_hora.setText(String.format(context.getString(R.string.quake_time_day_hour), days, hours/24));
                }
            }
        }
    }
}
