package cl.figonzal.lastquakechile.services;

import android.app.Activity;
import android.view.Window;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import cl.figonzal.lastquakechile.R;
import timber.log.Timber;

public class NightModeService implements LifecycleObserver {

    private Activity activity;
    private SharedPrefService sharedPrefService;
    private Window window;

    public NightModeService(Activity activity, Lifecycle lifecycle, SharedPrefService sharedPrefServices, Window window) {
        this.activity = activity;
        this.sharedPrefService = sharedPrefServices;
        this.window = window;

        lifecycle.addObserver(this);
    }

    /**
     * Funcion que permite revisar y establecer el modo noche desde Shared Preference Settings
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void checkNightMode() {

        //Leer preference settings

        boolean manual_night_mode = (boolean) sharedPrefService.getData(activity.getString(R.string.NIGHT_MODE_MANUAL_KEY), false);

        boolean auto_night_mode = (boolean) sharedPrefService.getData(activity.getString(R.string.NIGHT_MODE_AUTO_KEY), false);

        //MODO MANUAL
        //Si el modo manual esta activado
        if (manual_night_mode) {

            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            window.setStatusBarColor(activity.getColor(R.color.colorPrimaryVariantNightMode));

            //fixAdViewNightMode(activity);

            Timber.tag(activity.getString(R.string.TAG_NIGHT_MODE_MANUAL)).i(activity.getString(R.string.TAG_NIGHT_MODE_STATUS_ON));
        }

        //MODO AUTOMATICO
        //Si el modo automatico esta activado
        else if (auto_night_mode) {

            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY);

            Timber.tag(activity.getString(R.string.TAG_NIGHT_MODE_AUTO)).i(activity.getString(R.string.TAG_NIGHT_MODE_STATUS_ON));
        }

        //DESACTIVADO
        //Si el modo nocturno esta desactivado
        else {

            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            window.setStatusBarColor(activity.getColor(R.color.colorPrimaryVariant));

            Timber.tag(activity.getString(R.string.TAG_NIGHT_MODE)).i(activity.getString(R.string.TAG_NIGHT_MODE_STATUS_OFF));
        }
    }
}
