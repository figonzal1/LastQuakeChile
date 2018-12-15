package cl.figonzal.lastquakechile;

import android.arch.lifecycle.Observer;
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

public class MainActivity extends AppCompatActivity {

    private QuakeUtils quakeUtils;
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
                final RecyclerView rv = findViewById(R.id.recycle_view);
                final ProgressBar progressBar = findViewById(R.id.progressBar);
                progressBar.setVisibility(View.VISIBLE);

                if (QuakeUtils.checkInternet(getApplicationContext())) {

                    QuakeViewModel quakeViewModel = new QuakeViewModel(getApplication());
                    quakeViewModel.getQuakeList().observe(this, new Observer<List<QuakeModel>>() {
                        @Override
                        public void onChanged(@Nullable List<QuakeModel> quakeModelList) {

                            QuakeAdapter adapter = new QuakeAdapter(quakeModelList, getApplicationContext());
                            adapter.notifyDataSetChanged();
                            rv.setAdapter(adapter);
                            progressBar.setVisibility(View.INVISIBLE);

                            //Mostrar Snackbar De actualizacion
                            showSnackBar("Update");

                            //LOG ZONE
                            Log.d("PROGRESS_REFRESH", "UPDATED INFORMATION - TOOLBAR");
                        }
                    });
                } else {

                    progressBar.setVisibility(View.INVISIBLE);

                    //Mostrar Snackbar de Retry de datos
                    showSnackBar("Retry");

                    //LOG ZONE
                    Log.d("PROGRESS_REFRESH", "RETRY CONNNECTION - TOOLBAR");
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

    /**
     * Funcion para mostrar SnackBar en caso de RETRY o UPDATE de informacion
     *
     * @param tipo String que sera RETRY-> Para intento de update sin internet y UPDATE -> Cuando la lista sea actualizada
     */
    private void showSnackBar(String tipo) {

        if (tipo.equals("Retry")) {
            Snackbar
                    .make(getWindow().getDecorView().getRootView(), "Sin conexion a internet", Snackbar.LENGTH_INDEFINITE)
                    .setAction("Retry", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onOptionsItemSelected(item);
                        }
                    })
                    .show();
        } else if (tipo.equals("Update")) {
            Snackbar
                    .make(getWindow().getDecorView().getRootView(), "Sismos Actualizados", Snackbar.LENGTH_LONG)
                    .show();
        }

    }
}
