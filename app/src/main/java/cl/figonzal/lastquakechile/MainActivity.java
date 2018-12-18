package cl.figonzal.lastquakechile;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
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

import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements ResponseNetworkHandler {

    private QuakeUtils quakeUtils;
    private RecyclerView rv;
    private ProgressBar progressBar;
    private QuakeViewModel viewModel;
    private MenuItem item;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
                    Viewmodel encargado de mostrar mensajes de errores desde Volley (LoadDATA)
                 */
                viewModel.getStatusData().observe(this, new Observer<String>() {
                    @Override
                    public void onChanged(@Nullable String s) {
                        if (s != null) {

                            Snackbar
                                    .make(getWindow().getDecorView().getRootView(), s, Snackbar.LENGTH_INDEFINITE)
                                    .show();
                            progressBar.setVisibility(View.INVISIBLE);
                            Log.d("SNACKBAR", "Snack bar de servidor no responde");
                        }

                    }
                });

                /*
                    Flujo de informacion dependiendo de la conexion a internet
                */
                if (QuakeUtils.checkInternet(getApplicationContext())) {
                    getData();
                } else {
                    showSnackBar(getApplicationContext(), "Retry");
                    progressBar.setVisibility(View.INVISIBLE);
                }

                return true;

            case R.id.settings:
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

                //Mostrar snackbar con sismos actualizados
                showSnackBar(getApplicationContext(), "Update");

                Log.d("PROGRESS_TOOLBAR", "UPDATE INFORMATION - TOOLBAR");

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
        switch (tipo) {
            case "Retry":
                Snackbar
                        .make(getWindow().getDecorView().getRootView(), "Sin conexion a internet", Snackbar.LENGTH_INDEFINITE)
                        .setAction("Reintentar", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                if (QuakeUtils.checkInternet(Objects.requireNonNull(getApplication()))) {
                                    progressBar.setVisibility(View.VISIBLE);
                                    getData();
                                    showSnackBar(getApplicationContext(), "Update");
                                } else {
                                    Log.d("SNACKBAR", "Snack bar retry - Toolbar");
                                    showSnackBar(getApplicationContext(), "Retry");
                                }
                            }
                        })
                        .show();
                break;

            case "Update":
                Log.d("SNACKBAR", "Snack bar de actualizacion - Toolbar");
                Snackbar
                        .make(getWindow().getDecorView().getRootView(), "Sismos Actualizados", Snackbar.LENGTH_LONG)
                        .show();
                break;

        }
    }
}
