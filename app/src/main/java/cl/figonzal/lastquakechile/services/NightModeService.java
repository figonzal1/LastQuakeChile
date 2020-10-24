package cl.figonzal.lastquakechile.services;

import android.app.Activity;
import android.content.SharedPreferences;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.preference.PreferenceManager;

import cl.figonzal.lastquakechile.R;
import timber.log.Timber;

public class NightModeService implements LifecycleObserver {

    private final Activity activity;
    private final Window window;

    public NightModeService(Activity activity, @NonNull Lifecycle lifecycle, Window window) {
        this.activity = activity;
        this.window = window;

        lifecycle.addObserver(this);
    }

    /**
     * Funcion que permite revisar y establecer el modo noche desde Shared Preference Settings
     */
    @SuppressWarnings("unused")
    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void checkNightMode() {

        //Leer preference settings
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);

        boolean manual_night_mode = sharedPreferences.getBoolean(activity.getString(R.string.NIGHT_MODE_MANUAL_KEY), false);

        boolean auto_night_mode = sharedPreferences.getBoolean(activity.getString(R.string.NIGHT_MODE_AUTO_KEY), false);

        //MODO MANUAL
        //Si el modo manual esta activado
        if (manual_night_mode) {

            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            window.setStatusBarColor(activity.getColor(R.color.colorPrimaryVariantNightMode));

            //fixAdViewNightMode(activity);

            Timber.i(activity.getString(R.string.TAG_NIGHT_MODE_MANUAL) + ": " + activity.getString(R.string.TAG_NIGHT_MODE_STATUS_ON));
        }

        //MODO AUTOMATICO
        //Si el modo automatico esta activado
        else if (auto_night_mode) {

            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY);

            Timber.i(activity.getString(R.string.TAG_NIGHT_MODE_AUTO) + ": " + activity.getString(R.string.TAG_NIGHT_MODE_STATUS_ON));
        }

        //DESACTIVADO
        //Si el modo nocturno esta desactivado
        else {

            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            window.setStatusBarColor(activity.getColor(R.color.colorPrimaryVariant));

            Timber.i(activity.getString(R.string.TAG_NIGHT_MODE) + ": " + activity.getString(R.string.TAG_NIGHT_MODE_STATUS_OFF));
        }
    }
}
