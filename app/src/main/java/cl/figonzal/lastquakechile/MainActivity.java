package cl.figonzal.lastquakechile;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;

import cl.figonzal.lastquakechile.messageservice.MyFirebaseMessagingService;

public class MainActivity extends AppCompatActivity {

    private QuakeViewModel viewModel;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //Seteo de elementos a utilizar
        progressBar = findViewById(R.id.progress_bar_main_activity);

        //Instancia de view model
        viewModel = ViewModelProviders.of(this).get(QuakeViewModel.class);

        /*
            Firebase SECTION
         */
        FirebaseMessaging.getInstance().setAutoInitEnabled(true);
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(this, new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                String token = instanceIdResult.getToken();
                Log.d(getString(R.string.TAG_FIREBASE_TOKEN), token);

                //CRASH ANALYTICS LOG
                Crashlytics.log(Log.DEBUG, getString(R.string.TAG_FIREBASE_TOKEN), token);
                Crashlytics.setUserIdentifier(token);
            }
        });

        //Llamada a creacion de canal de notificaciones
        MyFirebaseMessagingService.createNotificationChannel(getApplicationContext());

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

        //Suscribir automaticamente al tema (FIREBASE - Quakes)
        MyFirebaseMessagingService.checkSuscription(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menuItem) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.toolbar_menu, menuItem);

        return super.onCreateOptionsMenu(menuItem);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {

        switch (item.getItemId()) {

            case R.id.refresh:

                //Progress abr de main se vuelve a activar durante el refresh de datos
                progressBar.setVisibility(View.VISIBLE);
                getData();

                Log.d(getString(R.string.TAG_PROGRESS_FROM_REFRESH), getString(R.string.TAG_PROGRESS_FROM_REFRESH_UPDATE_RESPONSE));
                Crashlytics.log(Log.DEBUG, getString(R.string.TAG_PROGRESS_FROM_REFRESH), getString(R.string.TAG_PROGRESS_FROM_REFRESH_UPDATE_RESPONSE));

                return true;

            case R.id.contact:
                Intent intent = new Intent(this, ContactActivity.class);
                startActivity(intent);

                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Funcion encargada de refrescar los datos del viewmodel cuando se presiona icono refresh en toolbar
     * Muestra snackbar de actualizacion de datos
     */
    private void getData() {
        //Se refresca el listado de sismos
        viewModel.refreshMutableQuakeList();
        //Progressbar desaparece despues de la descarga de datos
        progressBar.setVisibility(View.INVISIBLE);
    }
}
