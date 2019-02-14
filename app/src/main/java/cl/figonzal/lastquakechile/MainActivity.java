package cl.figonzal.lastquakechile;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;

import cl.figonzal.lastquakechile.messageservice.MyFirebaseMessagingService;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class MainActivity extends AppCompatActivity {

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Checkea si es primer inicio
        checkFirstRun();

        //Verifica si el celular tiene googleplay services activado
        checkPlayServices();

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

        //Setear imagen de toolbar con Glide
        final ImageView iv_foto = findViewById(R.id.toolbar_image);
        Glide.with(this)
                .load(R.drawable.foto)
                .apply(
                        new RequestOptions()
                                .placeholder(R.drawable.placeholder)
                                .error(R.drawable.not_found)
                )
                .transition(withCrossFade())
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        iv_foto.setImageDrawable(getDrawable(R.drawable.not_found));
                        return false;
                    }

                    //No es necesario usarlo (If u want)
                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        return false;
                    }
                })
                .into(iv_foto);

        //Suscribir automaticamente al tema (FIREBASE - Quakes)
        MyFirebaseMessagingService.checkSuscription(this);

    }

    private void checkFirstRun() {
        SharedPreferences sharedPreferences = getPreferences(Context.MODE_PRIVATE);

        boolean firtsRun = sharedPreferences.getBoolean("first_run", true);

        if (firtsRun) {
            Intent intent = new Intent(MainActivity.this, WelcomeActivity.class);
            startActivity(intent);
            finish();
        }

        //Cambiar a falso, para que proxima vez no abra invitation.
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("first_run", false);
        editor.apply();
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
