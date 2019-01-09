package cl.figonzal.lastquakechile;

import android.app.Dialog;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ProgressBar;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;

import cl.figonzal.lastquakechile.messageservice.MyFirebaseMessagingService;

public class MainActivity extends AppCompatActivity {

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //Verifica si el celular tiene googleplay services activado
        checkPlayServices();

        //Seteo de elementos a utilizar
        ProgressBar progressBar = findViewById(R.id.progress_bar_main_activity);

        //Instancia de view model
        QuakeViewModel viewModel = ViewModelProviders.of(this).get(QuakeViewModel.class);

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
    protected void onResume() {
        super.onResume();
        checkPlayServices();
    }

    private void checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);

        if (resultCode != ConnectionResult.SUCCESS) {

            //Si el error puede ser resuelto por el usuario
            if (apiAvailability.isUserResolvableError(resultCode)) {

                Dialog dialog = apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST);
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();
            } else {

                //El error no puede ser resuelto por el usuario y la app se cierra
                Log.d(getString(R.string.TAG_GOOGLE_PLAY), getString(R.string.TAG_GOOGLE_PLAY_NOSOPORTADO));
                Crashlytics.log(Log.DEBUG, getString(R.string.TAG_GOOGLE_PLAY), getString(R.string.TAG_GOOGLE_PLAY_NOSOPORTADO));
                finish();
            }
        } else {
            //La app puede ser utilizada, google play esta actualizado
            Log.d(getString(R.string.TAG_GOOGLE_PLAY), getString(R.string.TAG_GOOGLE_PLAY_ACTUALIZADO));
            Crashlytics.log(Log.DEBUG, getString(R.string.TAG_GOOGLE_PLAY), getString(R.string.TAG_GOOGLE_PLAY_ACTUALIZADO));
        }
    }
}
