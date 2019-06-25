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
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Objects;

import cl.figonzal.lastquakechile.FragmentPageAdapter;
import cl.figonzal.lastquakechile.R;
import cl.figonzal.lastquakechile.services.MyFirebaseMessagingService;
import cl.figonzal.lastquakechile.services.QuakeUtils;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class MainActivity extends AppCompatActivity {


    private AppBarLayout mAppBarLayout;
    private ImageView mIvFoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Setear configuracion por defecto
        PreferenceManager.setDefaultValues(this, R.xml.root_preferences, false);

        //Checkear si preferencias tiene modo noche
        QuakeUtils.checkNightMode(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MobileAds.initialize(this, "ca-app-pub-3940256099942544~3347511713");

        Bundle mBundleWelcome = getIntent().getExtras();
        if (mBundleWelcome != null) {
            //Si el usuario viene desde deep link, no se realiza first check
            //Si viene desde Google play, se realiza el check
            if (!mBundleWelcome.getBoolean(getString(R.string.desde_deep_link))) {
                QuakeUtils.checkFirstRun(this, false);
            }
        }

        //Verifica si el celular tiene googleplay services activado
        QuakeUtils.checkPlayServices(this);

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

        //Llamada a creacion de canal de notificaciones
        MyFirebaseMessagingService.createNotificationChannel(getApplicationContext());

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

        //Setear imagen de toolbar con Glide
        mIvFoto = findViewById(R.id.toolbar_image);
        loadImage();

        //Suscribir automaticamente al tema (FIREBASE - Quakes)
        MyFirebaseMessagingService.checkSuscription(this);

        //Mostrar dialog para actualización de versiones
        change_log_dialog();

    }

    /**
     * Funcion encargada de cargar la imagen de fondo en el toolbar
     */
    private void loadImage() {
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
    }

    private void change_log_dialog() {
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            long versionCode = packageInfo.versionCode;

            final SharedPreferences sharedPreferences = getPreferences(Context.MODE_PRIVATE);

            long actual_version_code = sharedPreferences.getLong("actual_version_code", 0);

            Log.d("VERSION_CODE_APP", String.valueOf(actual_version_code));

            if (actual_version_code == 0) {
                //Actualizar version
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putLong("actual_version_code", versionCode);
                editor.apply();
            } else if (actual_version_code < versionCode) {

                final Dialog dialog = new Dialog(MainActivity.this);
                dialog.setContentView(R.layout.changelog_layout);
                dialog.setCanceledOnTouchOutside(false);
                Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();

                Button entendido = dialog.findViewById(R.id.btn_changelog_accept);
                entendido.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
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
                editor.putLong("actual_version_code", versionCode);
                editor.apply();

            }

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }
}
