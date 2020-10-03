package cl.figonzal.lastquakechile.managers;

import android.app.Activity;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Window;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import cl.figonzal.lastquakechile.R;

public class NightModeManager {

    public NightModeManager() {
    }

    /**
     * Funcion que permite revisar y establecer el modo noche desde Shared Preference Settings
     *
     * @param activity Actividad para utilizar recursos
     * @param window   Usado para instanciar adview manualmente
     */
    public void checkNightMode(Activity activity, Window window) {

        FirebaseCrashlytics crashlytics = FirebaseCrashlytics.getInstance();

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

            Log.d(activity.getString(R.string.TAG_NIGHT_MODE_MANUAL), activity.getString(R.string.TAG_NIGHT_MODE_STATUS_ON));
            crashlytics.log(activity.getString(R.string.TAG_NIGHT_MODE_MANUAL) + activity.getString(R.string.TAG_NIGHT_MODE_STATUS_ON));
        }

        //MODO AUTOMATICO
        //Si el modo automatico esta activado
        else if (auto_night_mode) {

            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY);

            Log.d(activity.getString(R.string.TAG_NIGHT_MODE_AUTO), activity.getString(R.string.TAG_NIGHT_MODE_STATUS_ON));
            crashlytics.log(activity.getString(R.string.TAG_NIGHT_MODE_AUTO) + activity.getString(R.string.TAG_NIGHT_MODE_STATUS_ON));
        }

        //DESACTIVADO
        //Si el modo nocturno esta desactivado
        else {

            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            window.setStatusBarColor(activity.getColor(R.color.colorPrimaryVariant));

            Log.d(activity.getString(R.string.TAG_NIGHT_MODE), activity.getString(R.string.TAG_NIGHT_MODE_STATUS_OFF));
            crashlytics.log(activity.getString(R.string.TAG_NIGHT_MODE) + activity.getString(R.string.TAG_NIGHT_MODE_STATUS_OFF));
        }
    }
}
