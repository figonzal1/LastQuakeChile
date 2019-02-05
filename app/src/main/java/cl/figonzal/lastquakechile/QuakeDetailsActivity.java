package cl.figonzal.lastquakechile;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class QuakeDetailsActivity extends AppCompatActivity {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quake_details);

        //Setting toolbar
        Toolbar toolbar = findViewById(R.id.tool_bar_detail);
        setSupportActionBar(toolbar);

        //Muestra la flecha en toolbar para volver atras
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        //Obtener datos desde intent
        Bundle b = getIntent().getExtras();

        //Find de recursos
        TextView tv_ciudad = findViewById(R.id.tv_ciudad_detail);
        TextView tv_referencia = findViewById(R.id.tv_referencia_detail);
        TextView tv_escala = findViewById(R.id.tv_escala);
        TextView tv_magnitud = findViewById(R.id.tv_magnitud_detail);
        TextView tv_profundidad = findViewById(R.id.tv_epicentro);
        TextView tv_fecha = findViewById(R.id.tv_fecha);
        ImageView iv_sensible = findViewById(R.id.iv_sensible_detail);
        ImageView iv_mag_color = findViewById(R.id.iv_mag_color_detail);
        TextView tv_hora = findViewById(R.id.tv_hora_detail);
        TextView tv_gms = findViewById(R.id.tv_gms);
        final ImageView iv_mapa = findViewById(R.id.iv_map_quake);

        if (b != null) {

            /*
                OBTENCION DE INFO DESDE INTENT
             */
            String ciudad = b.getString(getString(R.string.INTENT_CIUDAD));
            String referencia = b.getString(getString(R.string.INTENT_REFERENCIA));

            String latitud = b.getString(getString(R.string.INTENT_LATITUD));
            String longitud = b.getString(getString(R.string.INTENT_LONGITUD));


            //Conversion de latitud a dms
            String dms_lat;
            double lat_ubicacion = Double.parseDouble(Objects.requireNonNull(latitud));
            if (lat_ubicacion < 0) {
                dms_lat = getString(R.string.coordenadas_sur);
            } else {
                dms_lat = getString(R.string.coordenadas_norte);
            }

            Map<String, Double> lat_dms = toDMS(lat_ubicacion);
            Double lat_grados_dsm = lat_dms.get("grados");
            Double lat_minutos_dsm = lat_dms.get("minutos");
            Double lat_segundos_dsm = lat_dms.get("segundos");
            dms_lat = String.format(Locale.US, "%.1f° %.1f' %.1f'' %s", lat_grados_dsm, lat_minutos_dsm, lat_segundos_dsm, dms_lat);

            //Conversion de latitud a dms
            String dms_long;
            double long_ubicacion = Double.parseDouble(Objects.requireNonNull(longitud));
            if (long_ubicacion < 0) {
                dms_long = getString(R.string.coordenadas_oeste);
            } else {
                dms_long = getString(R.string.coordenadas_este);
            }
            Map<String, Double> long_dms = toDMS(long_ubicacion);
            Double long_grados_dsm = long_dms.get("grados");
            Double long_minutos_dsm = long_dms.get("minutos");
            Double long_segundos_dsm = long_dms.get("segundos");
            dms_long = String.format(Locale.US, "%.1f° %.1f' %.1f'' %s", long_grados_dsm, long_minutos_dsm, long_segundos_dsm, dms_long);


            Map<String, Long> tiempos = null;

            //Si el bundle viene del adapter, usar directamente TIME LOCAL
            String fecha_local = b.getString(getString(R.string.INTENT_FECHA_LOCAL));
            if (fecha_local != null) {
                SimpleDateFormat format = new SimpleDateFormat(getString(R.string.DATETIME_FORMAT), Locale.US);
                Date local_date = null;
                try {
                    local_date = format.parse(fecha_local);

                } catch (ParseException e) {
                    e.printStackTrace();
                }

                tiempos = QuakeUtils.timeToText(local_date);
                Log.d("ADAPTER", "LOCAL: " + format.format(local_date));
            }

            //Si el bundle viene de notificacion, transformar UTC a TIME LOCAL
            String fecha_utc = b.getString(getString(R.string.INTENT_FECHA_UTC));
            if (fecha_utc != null) {
                SimpleDateFormat format = new SimpleDateFormat(getString(R.string.DATETIME_FORMAT_SLASH), Locale.US);
                format.setTimeZone(TimeZone.getDefault());

                Date utc_date = null;
                try {
                    utc_date = format.parse(fecha_utc);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                Date local_date = QuakeUtils.utcToLocal(Objects.requireNonNull(utc_date));
                fecha_local = format.format(local_date);

                tiempos = QuakeUtils.timeToText(local_date);

                Log.d("NOTIFICATION", "UTC: " + fecha_utc + "- LOCAL: " + fecha_local);
            }


            double magnitud = b.getDouble(getString(R.string.INTENT_MAGNITUD));
            Double profundidad = b.getDouble(getString(R.string.INTENT_PROFUNDIDAD));
            String escala = b.getString(getString(R.string.INTENT_ESCALA));
            //TODO: Agregar sensible a los detalles del sismo
            boolean sensible = b.getBoolean(getString(R.string.INTENT_SENSIBLE));
            String foto_url = b.getString(getString(R.string.INTENT_LINK_FOTO));



            /*
                SETEO DE TEXTVIEWS
             */

            //Setear titulo de ciudad en activity
            getSupportActionBar().setTitle(ciudad);

            //Setear nombre ciudad
            tv_ciudad.setText(ciudad);

            //Setear referencia
            tv_referencia.setText(referencia);

            //Setear magnitud en en circulo de color
            tv_magnitud.setText(String.format(getString(R.string.magnitud), magnitud));

            //Setear el color de background dependiendo de magnitud del sismo
            iv_mag_color.setColorFilter(getColor(QuakeUtils.getMagnitudeColor(magnitud)));

            //Setear profundidad
            tv_profundidad.setText(String.format(Locale.US, getString(R.string.quake_details_profundidad), profundidad));

            //Setear fecha
            tv_fecha.setText(fecha_local);

            //Setear posicionamiento
            tv_gms.setText(String.format(getString(R.string.format_coordenadas), dms_lat, dms_long));

            /*
                SECCION HORA
             */

            if (tiempos != null) {

                Long dias = tiempos.get(getString(R.string.UTILS_TIEMPO_DIAS));
                Long minutos = tiempos.get(getString(R.string.UTILS_TIEMPO_MINUTOS));
                Long horas = tiempos.get(getString(R.string.UTILS_TIEMPO_HORAS));
                Long segundos = tiempos.get(getString(R.string.UTILS_TIEMPO_SEGUNDOS));

                //Condiciones días.
                if (dias != null && dias == 0) {

                    if (horas != null && horas >= 1) {
                        tv_hora.setText(String.format(getString(R.string.quake_time_hour), horas));
                    } else {
                        tv_hora.setText(String.format(getString(R.string.quake_time_minute), minutos));

                        if (minutos != null && minutos < 1) {
                            tv_hora.setText(String.format(getString(R.string.quake_time_second), segundos));
                        }
                    }
                } else if (dias != null && dias > 0) {

                    if (horas != null && horas == 0) {
                        tv_hora.setText(String.format(getString(R.string.quake_time_day), dias));
                    } else if (horas != null && horas >= 1) {
                        tv_hora.setText(String.format(getString(R.string.quake_time_day_hour), dias, horas / 24));
                    }
                }
            }


            /*
                Seccion Tipo Escala
             */
            if (escala != null) {

                switch (escala) {
                    case "Ml":
                        tv_escala.setText(String.format(getString(R.string.quake_details_escala), getString(R.string.quake_details_magnitud_local)));
                        break;

                    case "Mw":
                        tv_escala.setText(String.format(getString(R.string.quake_details_escala), getString(R.string.quake_details_magnitud_momento)));
                        break;

                }
            }

            /*
                Seccion Sismo sensible
             */
            if (sensible) {
                iv_sensible.setVisibility(View.VISIBLE);
            }

            /*
                Seccion image
             */
            Uri uri = Uri.parse(foto_url);
            Glide.with(this)
                    .load(uri)
                    .apply(
                            new RequestOptions()
                                    .placeholder(R.drawable.placeholder)
                                    .error(R.drawable.not_found)
                    )
                    .transition(withCrossFade())
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            iv_mapa.setImageDrawable(getDrawable(R.drawable.not_found));
                            return false;
                        }

                        //No es necesario usarlo (If u want)
                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            return false;
                        }
                    })
                    .into(iv_mapa);
        }
    }

    private Map<String, Double> toDMS(double input) {

        Map<String, Double> dms = new HashMap<>();

        double abs = Math.abs(input);

        double lat_grados_rest = Math.floor(abs); //71
        double minutes = Math.floor((((abs - lat_grados_rest) * 3600) / 60)); // 71.43 -71 = 0.43 =25.8 = 25
        //(71.43 - 71)*3600 /60 - (71.43-71)*3600/60 = 25.8 - 25 =0.8
        double seconds = ((((abs - lat_grados_rest) * 3600) / 60) - Math.floor((((abs - lat_grados_rest) * 3600) / 60))) * 60;

        dms.put("grados", Math.floor(Math.abs(input)));
        dms.put("minutos", minutes);
        dms.put("segundos", seconds);

        return dms;
    }
}

