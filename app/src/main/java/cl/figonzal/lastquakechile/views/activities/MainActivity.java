package cl.figonzal.lastquakechile.views.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Date;
import java.util.Random;

import cl.figonzal.lastquakechile.R;
import cl.figonzal.lastquakechile.adapter.MainFragmentStateAdapter;
import cl.figonzal.lastquakechile.dialogs.RewardDialogFragment;
import cl.figonzal.lastquakechile.handlers.DateHandler;
import cl.figonzal.lastquakechile.services.AdsService;
import cl.figonzal.lastquakechile.services.FirebaseService;
import cl.figonzal.lastquakechile.services.GooglePlayService;
import cl.figonzal.lastquakechile.services.NightModeService;
import cl.figonzal.lastquakechile.services.SharedPrefService;
import cl.figonzal.lastquakechile.services.UpdaterService;
import cl.figonzal.lastquakechile.services.notifications.ChangeLogNotification;
import cl.figonzal.lastquakechile.services.notifications.QuakesNotification;
import timber.log.Timber;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class MainActivity extends AppCompatActivity {

    private AppBarLayout mAppBarLayout;
    private ImageView mIvFoto;
    private AdsService adsService;
    private UpdaterService updaterService;
    private SharedPrefService sharedPrefService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //Setear configuraciones por defecto de ConfigActivity
        PreferenceManager.setDefaultValues(this, R.xml.root_preferences, false);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DateHandler dateHandler = new DateHandler();
        sharedPrefService = new SharedPrefService(getApplicationContext());

        //Ad service
        adsService = new AdsService(this, getApplicationContext(), dateHandler);
        adsService.loadRewardVideo();

        //Night mode
        new NightModeService(this, this.getLifecycle(), getWindow());

        //GP services
        new GooglePlayService(this, this.getLifecycle());

        //Firebase services
        FirebaseService firebaseService = new FirebaseService(FirebaseMessaging.getInstance());
        firebaseService.getFirebaseToken();

        //Updater service
        updaterService = new UpdaterService(this, AppUpdateManagerFactory.create(this));
        updaterService.checkAvailability();

        //Creacion de canal de notificaciones para sismos y para changelogs (Requerido para API >26)
        ChangeLogNotification changeLogNotification = new ChangeLogNotification(this, sharedPrefService);
        QuakesNotification quakeNotification = new QuakesNotification(this, sharedPrefService);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            quakeNotification.createChannel();
            changeLogNotification.createChannel();
        }
        changeLogNotification.configNotificationChangeLog();
        quakeNotification.suscribedToQuakes(true);

        //Setear toolbars, viewpagers y tabs
        setToolbarViewPagerTabs();

        //Setear imagen de toolbar
        loadImageToolbar();

        //TODO: Contar la cantidad de aperturas del usuario
        //TODO: Cuando llegue a 10 abrir reviewmanager
        //TODO: Si e usuario no puntua agregar 10 aperturas mas, caso contrario borrar sharedpref
        //calculateInits();

        /*ReviewManager manager = ReviewManagerFactory.create(this);
        Task<ReviewInfo> request = manager.requestReviewFlow();
        request.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // We can get the ReviewInfo object
                ReviewInfo reviewInfo = task.getResult();
                Log.e("REVIEW", "review lanzada");

                boolean statusInits = check10Inits();

                //if (statusInits) {
                Task<Void> flow = manager.launchReviewFlow(this, reviewInfo);
                flow.addOnCompleteListener(task1 -> {

                    if (task1.isSuccessful()) {
                        Log.e("REVIEW", "review opinada");
                    } else {
                        Log.e("REVIEW", "review saltada");
                    }
                });
                //}
            } else {
                // There was some problem, continue regardless of the result.
            }
        });*/


    }

    /*private void calculateInits() {
        SharedPrefService sharedPrefService = new SharedPrefService(getApplicationContext());

        int initCounts = (int) sharedPrefService.getData("initCounts", 0);

        sharedPrefService.saveData("initCounts", initCounts + 1);
        Log.d("INIT_COUNTS", String.valueOf(initCounts + 1));
    }

    private boolean check10Inits() {
        SharedPrefService sharedPrefService = new SharedPrefService(getApplicationContext());

        int initCounts = (int) sharedPrefService.getData("initCounts", 0);

        return initCounts == 10;
    }*/


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
        ViewPager2 viewPager2 = findViewById(R.id.view_pager);
        viewPager2.setAdapter(new MainFragmentStateAdapter(this, getApplicationContext()));

        //Seteo de tabs.
        TabLayout tabLayout = findViewById(R.id.tabs);
        new TabLayoutMediator(tabLayout, viewPager2, (tab, position) -> tab.setText(MainFragmentStateAdapter.getTabs()[position])).attach();

        for (int i = 0; i < tabLayout.getTabCount(); i++) {

            TabLayout.Tab tab = tabLayout.getTabAt(i);

            if (tab != null) {

                if (i == 0) {

                    tab.setIcon(R.drawable.ic_quakes_24dp);
                } else if (i == 1) {

                    tab.setIcon(R.drawable.ic_report_24dp);
                } else if (i == 2) {

                    tab.setIcon(R.drawable.ic_map_24dp);
                }
            }
        }

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {

            @Override
            public void onTabSelected(@NonNull TabLayout.Tab tab) {

                mAppBarLayout.setExpanded(tab.getPosition() != 2);
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

        int modeNightType = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;

        //Detecta modo noche automatico como YES
        if (modeNightType == Configuration.UI_MODE_NIGHT_YES) {

            mCollapsingToolbarLayout.setContentScrimColor(getResources().getColor(R.color.colorPrimaryNightMode, getTheme()));

        } else if (modeNightType == Configuration.UI_MODE_NIGHT_NO) {

            mCollapsingToolbarLayout.setContentScrimColor(getResources().getColor(R.color.colorPrimary, getTheme()));
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

                        mIvFoto.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.not_found));
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

    /**
     * Determina si el dalogo se debe mostrar o no.
     */
    public void rewardDialog() {
        Date rewardDate = new Date((long) sharedPrefService.getData(getString(R.string.SHARED_PREF_END_REWARD_DATE), 0L));
        Date nowDate = new Date();

        if (nowDate.after(rewardDate)) {

            Timber.i("%s%s", getString(R.string.TAG_REWARD_STATUS), getString(R.string.TAG_REWARD_STATUS_EN_PERIODO));

            boolean showDialog = generateRandomNumber();

            if (showDialog) {

                RewardDialogFragment fragment = new RewardDialogFragment(adsService);
                fragment.setCancelable(false);
                fragment.show(getSupportFragmentManager(), getString(R.string.REWARD_DIALOG));

                Timber.i("%s%s", getString(R.string.TAG_RANDOM_SHOW_REWARD_DIALOG), getString(R.string.TAG_RANDOM_SHOW_REWARD_DIALOG_ON));
            } else {
                Timber.i("%s%s", getString(R.string.TAG_RANDOM_SHOW_REWARD_DIALOG), getString(R.string.TAG_RANDOM_SHOW_REWARD_DIALOG_OFF));
            }
        } else if (nowDate.before(rewardDate)) {
            Timber.i("%s%s", getString(R.string.TAG_REWARD_STATUS), getString(R.string.TAG_REWARD_STATUS_PERIODO_INACTIVO));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int itemId = item.getItemId();

        if (itemId == R.id.changelog_menu) {

            Intent intent = new Intent(MainActivity.this, ChangeLogActivity.class);
            startActivity(intent);
            return true;

        } else if (itemId == R.id.settings_menu) {

            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

        updaterService.resumeUpdater();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == UpdaterService.UPDATE_CODE) {
            if (resultCode == RESULT_OK) {
                Timber.i(getString(R.string.UPDATE_OK));
            } else {
                Timber.e(getString(R.string.UPDATE_FAILED), resultCode);
            }
        }
    }

    /**
     * Funcion encargada de generar un numero aleatorio para dialogs.
     *
     * @return Booleano con el resultado
     */
    private boolean generateRandomNumber() {

        Random random = new Random();
        int item = random.nextInt(10);
        return item % 3 == 0;
    }
}
