package cl.figonzal.lastquakechile.views.activities;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import cl.figonzal.lastquakechile.R;
import cl.figonzal.lastquakechile.adapter.ChangeLogAdapter;
import cl.figonzal.lastquakechile.model.ChangeLog;
import cl.figonzal.lastquakechile.services.Utils;

public class ChangeLogActivity extends AppCompatActivity {

    private RecyclerView rv;
    private ChangeLogAdapter adapter;
    private List<ChangeLog> changeLogList;

    private FirebaseCrashlytics crashlytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_log);

        /*
         * Checkear MODO NOCHE
         */
        Utils.checkNightMode(this, getWindow());

        Toolbar mToolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(mToolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Historial de versiones");

        instanciarRecursosInterfaz();

    }

    private void instanciarRecursosInterfaz() {

        changeLogList = new ArrayList<>();

        //Add changelogs
        addChangeLogs();

        rv = findViewById(R.id.recycler_view_change_log);
        rv.setHasFixedSize(true);

        LinearLayoutManager ly = new LinearLayoutManager(getApplicationContext());
        rv.setLayoutManager(ly);

        adapter = new ChangeLogAdapter(changeLogList);
        rv.setAdapter(adapter);
    }

    private void addChangeLogs() {

        //NEWER FIRTS
        changeLogList.add(new ChangeLog("v1.3.1", "2020-03-23",
                new String[]{
                        "- Ahora modo noche automático se activa con ahorro de energía",
                        "- Se agregan reportes sismológicos mensuales",
                        "- Actualizaciones internas",
                        "- Publicidad no invasiva"
                }
        ));

        changeLogList.add(new ChangeLog("v1.2.2", "2019-10-1",
                new String[]{
                        "- Se puede ajustar la cantidad de sismos que se muestran en la aplicación en el panel de configuración",
                        "- Solución del problema al intentar compartir sismos por WhatsApp",
                        "- Correcciones menores"
                }
        ));

        changeLogList.add(new ChangeLog("v1.2.1", "2019-04-31",
                new String[]{
                        "- Corrección de bug que provocaba cierres inesperados en algunos dispositivos",
                }
        ));

        changeLogList.add(new ChangeLog("v1.2.0", "2019-04-30",
                new String[]{
                        "- Se agrega actividad de configuración de preferencias de usuario",
                        "- Modo noche implementado",
                        "- Mapa Google map con la ubicación de un sismo",
                        "- Correcciones de bugs",
                }
        ));

        changeLogList.add(new ChangeLog("v1.1.0", "2019-03-26",
                new String[]{
                        "- Correcciones de rendimiento",
                        "- Mejoras de interfaz en actividad de bienvenida y actividad de detalles de sismos",
                        "- Corrección de listado de sismos repetidos al actualizar el listado"
                }
        ));

        changeLogList.add(new ChangeLog("v1.0.0", "2019-02-25",
                new String[]{
                        "- ¡Ahora puedes compartir los sismos, en Facebook, Whatsapp o enviarlos por mail!",
                        "- Correcciones de rendimiento y accesibilidad",
                }
        ));


    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}