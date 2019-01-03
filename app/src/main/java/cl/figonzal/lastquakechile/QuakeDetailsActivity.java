package cl.figonzal.lastquakechile;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.Locale;
import java.util.Objects;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class QuakeDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
        TextView tv_escala = findViewById(R.id.tv_escala);
        TextView tv_sensible = findViewById(R.id.tv_sensible);
        TextView tv_magnitud = findViewById(R.id.tv_magnitud_detail);
        TextView tv_profundidad = findViewById(R.id.tv_epicentro);
        TextView tv_fecha_local = findViewById(R.id.tv_fecha);
        ImageView iv_mag_color = findViewById(R.id.iv_mag_color);
        ImageView iv_mapa = findViewById(R.id.iv_map_quake);

        if (b != null) {
            String ciudad = b.getString(getString(R.string.INTENT_CIUDAD));
            String latitud = b.getString(getString(R.string.INTENT_LATITUD));
            String longitud = b.getString(getString(R.string.INTENT_LONGITUD));

            String fecha_local = b.getString(getString(R.string.INTENT_FECHA_LOCAL));

            Double magnitud = b.getDouble(getString(R.string.INTENT_MAGNITUD));
            Double profundidad = b.getDouble(getString(R.string.INTENT_PROFUNDIDAD));
            String escala = b.getString(getString(R.string.INTENT_ESCALA));
            Boolean sensible = b.getBoolean(getString(R.string.INTENT_SENSIBLE));
            String foto_url = b.getString(getString(R.string.INTENT_LINK_FOTO));

            //Setear titulo de ciudad en activity
            getSupportActionBar().setTitle(ciudad);

            //Setear magnitud en en circulo de color
            tv_magnitud.setText(String.format(getString(R.string.magnitud), magnitud));

            //Setear el color de background dependiendo de magnitud del sismo
            iv_mag_color.setColorFilter(getColor(QuakeUtils.getMagnitudeColor(magnitud)));

            //Setear profundidad
            tv_profundidad.setText(String.format(Locale.US, getString(R.string.quake_details_profundidad), profundidad));

            //Setear fecha
            tv_fecha_local.setText(fecha_local);

            /*
                Seccion Tipo Escala
             */
            if (escala != null) {

                switch (escala) {
                    case "Ml":
                        tv_escala.setText(String.format(getString(R.string.quake_details_escala), "Magnitud Local"));
                        break;

                    case "Mw":
                        tv_escala.setText(String.format(getString(R.string.quake_details_escala), "Magnitud Momento"));
                        break;

                }
            }

            /*
                Seccion Sismo sensible
             */
            if (sensible) {
                tv_sensible.setText(String.format(getString(R.string.quake_details_sensible), "percibido"));
            } else {
                tv_sensible.setText(String.format(getString(R.string.quake_details_sensible), "no percibido"));
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
                    .into(iv_mapa);



        }
    }

}
