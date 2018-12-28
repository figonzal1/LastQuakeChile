package cl.figonzal.lastquakechile;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.List;

public class MainActivity extends AppCompatActivity implements ResponseNetworkHandler {

    private MenuItem item;
    private boolean suscrito = false;
    private QuakeViewModel viewModel;
    private RecyclerView rv;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*
            Firebase SECTION
         */
        FirebaseMessaging.getInstance().setAutoInitEnabled(true);
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(this, new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                String token = instanceIdResult.getToken();
                Log.d(getString(R.string.TAG_FIREBASE_TOKEN), "Nuevo token: " + token);
            }
        });

        //Llamada a creacion de canal de notificaciones
        QuakeUtils.createNotificationChannel(getApplicationContext());

        //Buscar toolbar en resources
        Toolbar toolbar = findViewById(R.id.tool_bar);

        //Setear el toolbar sobre el main activity
        setSupportActionBar(toolbar);

        //View pager para los fragments (Solo 1 fragment en esta app)
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(new FragmentPageAdapter(getSupportFragmentManager()));

        //Seteo de los eventos de tabs.
        //TabLayout tabLayout = (TabLayout)findViewById(R.id.tabs);;
        //tabLayout.setupWithViewPager(viewPager);

        //Setear collapsing toolbar con titulo estatico superior y animacion de colores al recoger toolbar
        final CollapsingToolbarLayout collapsingToolbarLayout = findViewById(R.id.collapsing_toolbar);
        collapsingToolbarLayout.setTitleEnabled(true);
        collapsingToolbarLayout.setContentScrimColor(getResources().getColor(R.color.colorPrimary, getTheme()));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menuItem) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.toolbar_menu, menuItem);
        return super.onCreateOptionsMenu(menuItem);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {

        this.item = item;
        switch (item.getItemId()) {

            case R.id.refresh:

                //Definicion de materiales a usar para checkear internet UI
                rv = findViewById(R.id.recycle_view);
                progressBar = findViewById(R.id.progressBar);
                progressBar.setVisibility(View.VISIBLE);
                viewModel = ViewModelProviders.of(this).get(QuakeViewModel.class);

                /*
                    Flujo de informacion dependiendo de la conexion a internet
                */
                if (QuakeUtils.checkInternet(getApplicationContext())) {
                    getData();
                } else {
                    //Mostrar Snackbar de Retry de datos
                    showSnackBar(getApplicationContext(), getString(R.string.FLAG_RETRY));
                    progressBar.setVisibility(View.INVISIBLE);

                    //LOG ZONE
                    Log.d(getString(R.string.TAG_PROGRESS_FROM_REFRESH), getString(R.string.TAG_PROGRESS_FROM_REFRESH_RETRY_RESPONSE));
                }

                return true;

            case R.id.settings:


                if (!suscrito) {
                    FirebaseMessaging.getInstance().subscribeToTopic(getString(R.string.FIREBASE_TOPIC_NAME))
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(getApplicationContext(), getString(R.string.FIREBASE_SNACKBAR_SUBSCRIBE_TOPIC_SUCCESS), Toast.LENGTH_LONG).show();
                                    }
                                    Log.d(getString(R.string.TAG_FIREBASE_SUSCRIPTION), "SUSCRITO");
                                }
                            });

                    suscrito = true;
                } else {
                    FirebaseMessaging.getInstance().unsubscribeFromTopic(getString(R.string.FIREBASE_TOPIC_NAME));
                    Toast.makeText(getApplicationContext(), getString(R.string.FIREBASE_SNACKBAR_SUBSCRIBE_TOPIC_DELETED), Toast.LENGTH_LONG).show();
                    Log.d(getString(R.string.TAG_FIREBASE_SUSCRIPTION), "SUSCRIPCION ELIMINADA");
                }

                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void getData() {

        viewModel.getQuakeList().observe(this, new Observer<List<QuakeModel>>() {
            @Override
            public void onChanged(@Nullable List<QuakeModel> quakeModelList) {


                //Setear el adapter con la lista de quakes
                QuakeAdapter adapter = new QuakeAdapter(quakeModelList, getApplicationContext());
                adapter.notifyDataSetChanged();
                rv.setAdapter(adapter);

                //Progressbar desaparece despues de la descarga de datos
                progressBar.setVisibility(View.INVISIBLE);

                //Mostrar Snackbar de sismos actualizados
                showSnackBar(getApplicationContext(), getString(R.string.FLAG_UPDATE));

                //LOG ZONE
                Log.d(getString(R.string.TAG_PROGRESS_FROM_REFRESH), getString(R.string.TAG_PROGRESS_FROM_REFRESH_UPDATE_RESPONSE));
            }
        });
    }

    /**
     * Funcion para mostrar SnackBar en caso de RETRY o UPDATE de informacion
     *
     * @param tipo String que sera RETRY-> Para intento de update sin internet y UPDATE -> Cuando la lista sea actualizada
     */
    @Override
    public void showSnackBar(Context context, String tipo) {

        if (tipo.equals(getString(R.string.FLAG_RETRY))) {
            Snackbar
                    .make(getWindow().getDecorView().getRootView(), getString(R.string.SNACKBAR_STATUS_MESSAGE_NOCONNECTION), Snackbar.LENGTH_INDEFINITE)
                    .setAction(getString(R.string.FLAG_RETRY), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onOptionsItemSelected(item);
                        }
                    })
                    .show();
        } else if (tipo.equals(getString(R.string.FLAG_UPDATE))) {
            Snackbar
                    .make(getWindow().getDecorView().getRootView(), getString(R.string.SNACKBAR_STATUS_MESSAGE_UPDATE), Snackbar.LENGTH_LONG)
                    .show();
        }
    }
}
