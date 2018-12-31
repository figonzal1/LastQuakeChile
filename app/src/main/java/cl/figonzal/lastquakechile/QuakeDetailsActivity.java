package cl.figonzal.lastquakechile;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import java.util.Objects;

public class QuakeDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quake_details);

        //Setting toolbar
        Toolbar toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        //Muestra la flecha en toolbar para volver atras
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        //Obtener datos desde intent
        Bundle b = getIntent().getExtras();

        if (b != null) {
            String ciudad = b.getString(getString(R.string.INTENT_CIUDAD));
            String referencia = b.getString(getString(R.string.INTENT_REFERENCIA));
            String latitud = b.getString(getString(R.string.INTENT_LATITUD));
            String longitud = b.getString(getString(R.string.INTENT_LONGITUD));

            String fecha_local = b.getString(getString(R.string.INTENT_FECHA_LOCAL));
            Double magnitud = b.getDouble(getString(R.string.INTENT_MAGNITUD));
            Double profundidad = b.getDouble(getString(R.string.INTENT_PROFUNDIDAD));
            String escala = b.getString(getString(R.string.INTENT_ESCALA));
            Boolean sensible = b.getBoolean(getString(R.string.INTENT_SENSIBLE));

            Log.d("INTENT", referencia);
            Log.d("INTENT", String.valueOf(sensible));
        }
    }
}
