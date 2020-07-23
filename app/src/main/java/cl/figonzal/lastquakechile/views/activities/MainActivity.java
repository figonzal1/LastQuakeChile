package cl.figonzal.lastquakechile.views.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;

import cl.figonzal.lastquakechile.R;
import cl.figonzal.lastquakechile.SettingsActivity;
import cl.figonzal.lastquakechile.adapter.FragmentPageAdapter;
import cl.figonzal.lastquakechile.services.AdsService;
import cl.figonzal.lastquakechile.services.Utils;
import cl.figonzal.lastquakechile.services.notifications.ChangeLogNotification;
import cl.figonzal.lastquakechile.services.notifications.NotificationService;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class MainActivity extends AppCompatActivity {


    private AppBarLayout mAppBarLayout;
    private ImageView mIvFoto;
    private AdsService adsService;

    private FirebaseCrashlytics crashlytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //Setear configuraciones por defecto de ConfigActivity
        PreferenceManager.setDefaultValues(this, R.xml.root_preferences, false);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        crashlytics = FirebaseCrashlytics.getInstance();

        //ADS
        MobileAds.initialize(this);
        adsService = new AdsService(getApplicationContext(), getSupportFragmentManager());
        adsService.loadRewardedVideo(MainActivity.this);

        //Configurar MODO NOCHE
        Utils.checkNightMode(MainActivity.this, getWindow());

        //Servicios de google play
        Utils.checkPlayServices(this);

        //Servicios de Firebase
        getFirebaseToken();

        //Creacion de canal de notificaciones para sismos y para changelogs (Requerido para API >26)
        NotificationService.createNotificationChannel(getApplicationContext());

        //Realizar suscripcion el tema 'Quakes' para notificaciones
        NotificationService.checkSuscriptions(this);

        //Enviar notificacion changelog de ser necesario
        new ChangeLogNotification().configNotificationChangeLog(false, getApplicationContext());

        //Setear toolbars, viewpagers y tabs
        setToolbarViewPagerTabs();

        //Setear imagen de toolbar
        loadImageToolbar();
    }

    /**
     * Funcion encargada de realizar la iniciacion de los servicios de FIREBASE
     */
    private void getFirebaseToken() {

        //FIREBASE SECTION
        FirebaseMessaging.getInstance().setAutoInitEnabled(true);
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(this,
                new OnSuccessListener<InstanceIdResult>() {
                    @Override
                    public void onSuccess(InstanceIdResult instanceIdResult) {
                        String token = instanceIdResult.getToken();
                        Log.e(getString(R.string.TAG_FIREBASE_TOKEN), token);

                        //CRASH ANALYTICS LOG
                        crashlytics.log(getString(R.string.TAG_FIREBASE_TOKEN) + token);
                        crashlytics.setUserId(token);
                    }
                });
    }

    /**
     * Setear elementos de UI necesarios para el funcionamiento de la APP
     */
    private void setToolbarViewPagerTabs() {
        //Buscar toolbar en resources
        Toolbar mToolbar = findViewById(R.id.tool_bar);

        //Setear el toolbar sobre el main activity
        setSupportActionBar(mToolbar);

        //Appbar layout para minimizar el collapse toolbar cuando se presiona el tab de mapa
        mAppBarLayout = findViewById(R.id.app_bar);

        //View pager para los fragments (Solo 1 fragment en esta app)
        ViewPager mViewPager = findViewById(R.id.view_pager);
        mViewPager.setAdapter(new FragmentPageAdapter(getSupportFragmentManager(), getApplicationContext()));


        //Seteo de tabs.
        TabLayout mTabLayout = findViewById(R.id.tabs);
        mTabLayout.setupWithViewPager(mViewPager);

        for (int i = 0; i < mTabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = mTabLayout.getTabAt(i);
            assert tab != null;
            if (i == 0) {
                tab.setIcon(R.drawable.ic_quakes_24dp);
            } else if (i == 1) {
                tab.setIcon(R.drawable.ic_report_24dp);
            } else if (i == 2) {
                tab.setIcon(R.drawable.ic_map_24dp);
            }
        }
        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {

            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 2) {
                    mAppBarLayout.setExpanded(false);
                } else {
                    mAppBarLayout.setExpanded(true);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        //Setear collapsing toolbar con titulo estatico superior y animacion de colores al recoger
        // toolbar
        CollapsingToolbarLayout mCollapsingToolbarLayout = findViewById(R.id.collapsing_toolbar);
        mCollapsingToolbarLayout.setTitleEnabled(true);

        int modeNightType = getResources().getConfiguration().uiMode &
                Configuration.UI_MODE_NIGHT_MASK;

        //Detecta modo noche automatico como YES
        if (modeNightType == Configuration.UI_MODE_NIGHT_YES) {
            mCollapsingToolbarLayout.setContentScrimColor(getResources().getColor(R.color.colorPrimaryNightMode
                    , getTheme()));
        } else if (modeNightType == Configuration.UI_MODE_NIGHT_NO) {
            mCollapsingToolbarLayout.setContentScrimColor(getResources().getColor(R.color.colorPrimary,
                    getTheme()));
        }
    }

    /**
     * Funcion encargada de cargar la imagen de fondo en el toolbar
     */
    private void loadImageToolbar() {
        mIvFoto = findViewById(R.id.toolbar_image);
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
                    public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                Target<Drawable> target,
                                                boolean isFirstResource) {
                        mIvFoto.setImageDrawable(getDrawable(R.drawable.not_found));
                        return false;
                    }

                    //No es necesario usarlo (If u want)
                    @Override
                    public boolean onResourceReady(Drawable resource, Object model,
                                                   Target<Drawable> target,
                                                   DataSource dataSource,
                                                   boolean isFirstResource) {

                        return false;
                    }
                })
                .into(mIvFoto);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {

            case R.id.changelog_menu:
                Intent intent = new Intent(MainActivity.this, ChangeLogActivity.class);
                startActivity(intent);
                return true;

            case R.id.settings_menu:
                Intent intent2 = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent2);
                return true;


        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Utils.checkPlayServices(this);
        adsService.getRewardedVideoAd().resume(this);
    }

    @Override
    public void onPause() {
        adsService.getRewardedVideoAd().pause(this);
        super.onPause();
    }

    @Override
    public void onDestroy() {
        adsService.getRewardedVideoAd().destroy(this);
        super.onDestroy();
    }

}
