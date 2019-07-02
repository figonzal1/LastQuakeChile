package cl.figonzal.lastquakechile.views;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

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
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Date;
import java.util.Objects;

import cl.figonzal.lastquakechile.FragmentPageAdapter;
import cl.figonzal.lastquakechile.R;
import cl.figonzal.lastquakechile.services.MyFirebaseMessagingService;
import cl.figonzal.lastquakechile.services.QuakeUtils;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class MainActivity extends AppCompatActivity {


    private AppBarLayout mAppBarLayout;
    private ImageView mIvFoto;
    private RewardedVideoAd rewardedVideoAd;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        /*
         * Setear configuraciones por defecto de ConfigActivity
         */
        PreferenceManager.setDefaultValues(this, R.xml.root_preferences, false);

        /*
         * Checkear MODO NOCHE
         */
        QuakeUtils.checkNightMode(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*
         * Checkear logica de first run con actividad de welcome
         */
        checkWelcomeActivity();

        /*
         * Servicios de google play
         */
        QuakeUtils.checkPlayServices(this);

        /*
         * Servicios de Firebase
         */
        checkFirebaseServices();

        /*
         * Creacion de canal de notificaciones para sismos (Requerido para API > 26)
         */
        MyFirebaseMessagingService.createNotificationChannel(getApplicationContext());

        /*
         * Realizar suscripcion el tema 'Quakes' para notificaciones
         */
        MyFirebaseMessagingService.checkSuscription(this);

        /*
         * Iniciar Ads
         */
        MobileAds.initialize(this, "ca-app-pub-3940256099942544~3347511713");

        /*
         * Setear toolbars, viewpagers y tabs
         */
        setToolbarViewPagerTabs();

        /*
         * Setear imagen de toolbar
         */
        loadImage();

        /*
         * Dialog's de changelog & rewards
         */
        //changeLogDialog();
        //rewardDialog();

    }

    /**
     * Funcion que realiza la configuracion de reward dialog
     */
    private void rewardDialog() {
        sharedPreferences = getSharedPreferences(getString(R.string.MAIN_SHARED_PREF_KEY),
                Context.MODE_PRIVATE);
        Date reward_date =
                new Date(sharedPreferences.getLong(getString(R.string.SHARED_PREF_END_REWARD_TIME), 0));
        Date now_date = new Date();

        rewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this);
        rewardedVideoAd.setRewardedVideoAdListener(new RewardedVideoAdListener() {
            @Override
            public void onRewardedVideoAdLoaded() {
                Log.d(getString(R.string.TAG_VIDEO_REWARD_STATUS), getString(R.string.TAG_VIDEO_REWARD_STATUS_LOADED));
            }

            @Override
            public void onRewardedVideoAdOpened() {

            }

            @Override
            public void onRewardedVideoStarted() {

            }

            @Override
            public void onRewardedVideoAdClosed() {

            }

            @Override
            public void onRewarded(RewardItem rewardItem) {
                Log.d(getString(R.string.TAG_VIDEO_REWARD_STATUS), getString(R.string.TAG_VIDEO_REWARD_STATUS_REWARDED));
            }

            @Override
            public void onRewardedVideoAdLeftApplication() {

            }

            @Override
            public void onRewardedVideoAdFailedToLoad(int i) {

            }

            @Override
            public void onRewardedVideoCompleted() {
                Log.d(getString(R.string.TAG_VIDEO_REWARD_STATUS), getString(R.string.TAG_VIDEO_REWARD_STATUS_COMPLETED));

                Date date_now = new Date();

                Log.d(getString(R.string.TAG_POST_REWARD_HORA_AHORA), QuakeUtils.dateToString(getApplicationContext(), date_now));
                //sumar 24 horas al tiempo del celular
                Date date_new = QuakeUtils.addHoursToJavaUtilDate(date_now, 1);
                Log.d(getString(R.string.TAG_POST_REWARD_HORA_REWARD), QuakeUtils.dateToString(getApplicationContext(), date_new));

                //Guardar fecha de termino de reward
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putLong(getString(R.string.SHARED_PREF_END_REWARD_TIME), date_new.getTime()).apply();

                recreate();
            }
        });

        //Si la hora del celular es posterior a reward date
        if (now_date.after(reward_date)) {

            Log.d(getString(R.string.TAG_REWARD_STATUS), getString(R.string.TAG_REWARD_STATUS_EN_PERIODO));
            //Cargar video
            loadRewardedVideo();

            boolean showDialog = QuakeUtils.generateRandomNumber();
            if (showDialog) {
                //Cargar dialog
                loadDialogReward();
                Log.d(getString(R.string.TAG_RANDOM_SHOW_REWARD_DIALOG), getString(R.string.TAG_RANDOM_SHOW_REWARD_DIALOG_ON));
            } else {
                Log.d(getString(R.string.TAG_RANDOM_SHOW_REWARD_DIALOG), getString(R.string.TAG_RANDOM_SHOW_REWARD_DIALOG_OFF));
            }
        }

        //Si el periodo de reward aun no pasa
        else if (now_date.before(reward_date)) {
            Log.d(getString(R.string.TAG_REWARD_STATUS), getString(R.string.TAG_REWARD_STATUS_PERIODO_INACTIVO));
        }
    }

    /**
     * Funcion encargada de realizar la iniciacion de los servicios de FIREBASE
     */
    private void checkFirebaseServices() {

        //FIREBASE SECTION
        FirebaseMessaging.getInstance().setAutoInitEnabled(true);
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(this,
                new OnSuccessListener<InstanceIdResult>() {
                    @Override
                    public void onSuccess(InstanceIdResult instanceIdResult) {
                        String token = instanceIdResult.getToken();
                        Log.d(getString(R.string.TAG_FIREBASE_TOKEN), token);

                        //CRASH ANALYTICS LOG
                        Crashlytics.log(Log.DEBUG, getString(R.string.TAG_FIREBASE_TOKEN), token);
                        Crashlytics.setUserIdentifier(token);


                    }
                });
    }

    /**
     * Funcion encargada de realizar el checkeo de first run de la aplicacion para lanzar welcomeActivity
     */
    private void checkWelcomeActivity() {
        Bundle mBundleWelcome = getIntent().getExtras();
        if (mBundleWelcome != null) {
            //Si el usuario viene desde deep link, no se realiza first check (Para que welcome activity no abra 2 veces)
            //Si viene desde Google play, se realiza el check
            if (!mBundleWelcome.getBoolean(getString(R.string.desde_deep_link))) {
                QuakeUtils.checkFirstRun(this, false);
            }
        }
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
        mViewPager.setAdapter(new FragmentPageAdapter(getSupportFragmentManager(),
                getApplicationContext()));


        //Seteo de tabs.
        TabLayout mTabLayout = findViewById(R.id.tabs);
        mTabLayout.setupWithViewPager(mViewPager);
        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {

            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 1) {
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
     * Funcion encargada de cargar el video de bonificacion
     */
    private void loadRewardedVideo() {
        rewardedVideoAd.loadAd("ca-app-pub-3940256099942544/5224354917", new AdRequest.Builder().build());
    }

    /**
     * Funcion encargada de mostrar el dialog de rewards
     */
    private void loadDialogReward() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.reward_dialog_layout);
        dialog.setCanceledOnTouchOutside(false);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

        Button button_ver_video = dialog.findViewById(R.id.btn_reward_ver_video);
        button_ver_video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (rewardedVideoAd.isLoaded()) {
                    dialog.dismiss();
                    rewardedVideoAd.show();

                    Log.d(getString(R.string.TAG_REWARD_DIALOG), getString(R.string.TAG_REWARD_DIALOG_BTN_VER_VIDEO));
                }
            }
        });
        Button button_cancel = dialog.findViewById(R.id.btn_reward_cancelar);
        button_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();

                Log.d(getString(R.string.TAG_REWARD_DIALOG), getString(R.string.TAG_REWARD_DIALOG_BTN_CANCEL));
            }
        });
    }

    /**
     * Funcion que muestra el change log dialog
     */
    private void changeLogDialog() {
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            long versionCode = packageInfo.versionCode;

            long actual_version_code =
                    sharedPreferences.getLong(getString(R.string.SHARED_PREF_ACTUAL_VERSION_CODE)
                            , 0);

            Log.d(getString(R.string.TAG_VERSION_CODE_APP), String.valueOf(actual_version_code));

            if (actual_version_code == 0) {
                //Actualizar version
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putLong(getString(R.string.SHARED_PREF_ACTUAL_VERSION_CODE), versionCode);
                editor.apply();
            } else if (actual_version_code < versionCode) {

                //Dialog de changelog
                final Dialog dialog = new Dialog(MainActivity.this);
                dialog.setContentView(R.layout.changelog_dialog_layout);
                dialog.setCanceledOnTouchOutside(false);
                Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                dialog.show();

                //Boton entendido
                Button entendido = dialog.findViewById(R.id.btn_reward_ver_video);
                entendido.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();

                        Log.d(getString(R.string.TAG_CHANGE_LOG_DIALOG), getString(R.string.TAG_CHANGE_LOG_DIALOG_BTN_ENTENDIDO));
                    }
                });

                TextView tv_descripcion = dialog.findViewById(R.id.tv_changelog_description);
                TextView tv_version = dialog.findViewById(R.id.tv_changelog_version);

                tv_version.setText("v1.2.1");
                tv_descripcion.setText("- Corrección de bug que provocaba cierres inesperados\n" +
                        "- Actividad de configuración de preferencias\n" +
                        "- Modo noche\n" +
                        "- Google map en detalle de sismo\n" +
                        "- Correcciones de bugs");

                //Actualizar version
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putLong(getString(R.string.SHARED_PREF_ACTUAL_VERSION_CODE), versionCode);
                editor.apply();

            }

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Funcion encargada de cargar la imagen de fondo en el toolbar
     */
    private void loadImage() {
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
    protected void onResume() {
        super.onResume();

        QuakeUtils.checkPlayServices(this);

        //rewardedVideoAd.resume(this);
    }

    @Override
    public void onPause() {
        //rewardedVideoAd.pause(this);
        super.onPause();
    }

    @Override
    public void onDestroy() {
        //rewardedVideoAd.destroy(this);
        super.onDestroy();
    }
}
