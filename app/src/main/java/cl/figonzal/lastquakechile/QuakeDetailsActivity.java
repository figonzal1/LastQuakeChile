package cl.figonzal.lastquakechile;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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
        ImageView iv_mag_color = findViewById(R.id.iv_mag_color);
        TextView tv_hora = findViewById(R.id.tv_hora_detail);
        final ImageView iv_mapa = findViewById(R.id.iv_map_quake);

        if (b != null) {

            /*
                OBTENCION DE INFO DESDE INTENT
             */
            String ciudad = b.getString(getString(R.string.INTENT_CIUDAD));
            String referencia = b.getString(getString(R.string.INTENT_REFERENCIA));
            //String latitud = b.getString(getString(R.string.INTENT_LATITUD));
            //String longitud = b.getString(getString(R.string.INTENT_LONGITUD));

            Map<String, Long> tiempos = null;

            //Si el bundle viene del adapter
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

            //Si el bundle viene de notificacion
            String fecha_utc = b.getString(getString(R.string.INTENT_FECHA_UTC));
            if (fecha_utc != null) {
                SimpleDateFormat format = new SimpleDateFormat(getString(R.string.DATETIME_FORMAT), Locale.US);
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


            Double magnitud = b.getDouble(getString(R.string.INTENT_MAGNITUD));
            Double profundidad = b.getDouble(getString(R.string.INTENT_PROFUNDIDAD));
            String escala = b.getString(getString(R.string.INTENT_ESCALA));
            //TODO: Agregar sensible a los detalles del sismo
            Boolean sensible = b.getBoolean(getString(R.string.INTENT_SENSIBLE));
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
            /*if (sensible) {
                tv_sensible.setText(String.format(getString(R.string.quake_details_sensible), "percibido"));
            } else {
                tv_sensible.setText(String.format(getString(R.string.quake_details_sensible), "no percibido"));
            }*/

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
}

