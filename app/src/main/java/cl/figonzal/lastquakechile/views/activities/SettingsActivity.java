package cl.figonzal.lastquakechile.views.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SeekBarPreference;

import cl.figonzal.lastquakechile.R;
import cl.figonzal.lastquakechile.services.NightModeService;
import cl.figonzal.lastquakechile.services.SharedPrefService;
import cl.figonzal.lastquakechile.services.notifications.QuakesNotification;
import timber.log.Timber;

public class SettingsActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        //Check modo noche
        new NightModeService(this, this.getLifecycle(), new SharedPrefService(getApplicationContext()), getWindow());

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings_menu, new SettingsFragment())
                .commit();

        Toolbar mToolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(mToolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.settings);
        }
    }

    @Override
    public void onBackPressed() {

        Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);

        super.onBackPressed();
    }

    //Settings Fragment
    public static class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

        private Activity activity;
        private SeekBarPreference seekBarPreference;
        private SharedPrefService sharedPrefService;
        private QuakesNotification quakesNotification;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            if (getActivity() != null) {

                activity = getActivity();
                sharedPrefService = new SharedPrefService(getContext());

                quakesNotification = new QuakesNotification(getContext(), sharedPrefService);
            }
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            //Seek bar
            seekBarPreference = findPreference(activity.getString(R.string.SHARED_PREF_LIST_QUAKE_NUMBER));

            if (seekBarPreference != null) {

                seekBarPreference.setMin(10);
                seekBarPreference.setMax(30);

                String limite = String.valueOf((int) sharedPrefService.getData(activity.getString(R.string.SHARED_PREF_LIST_QUAKE_NUMBER), 0));

                //Setear resumen por defecto
                if (limite.equals("0")) {

                    seekBarPreference.setValue(15);
                    seekBarPreference.setSummary(String.format(activity.getString(R.string.LIST_QUAKE_NUMBER_SUMMARY), 15));

                } else {
                    seekBarPreference.setSummary(String.format(activity.getString(R.string.LIST_QUAKE_NUMBER_SUMMARY), Integer.parseInt(limite)));
                }

            } else {
                Timber.e("Seek bar nulo");
            }
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

            /*
             * Preferencia Modo Noche
             * MANUAL
             */
            if (key.equals(activity.getString(R.string.NIGHT_MODE_MANUAL_KEY))) {

                //Si el modo manual esta activado

                boolean manualMode = (boolean) sharedPrefService.getData(activity.getString(R.string.NIGHT_MODE_MANUAL_KEY), false);

                if (manualMode) {

                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    activity.setTheme(R.style.AppTheme);
                    activity.recreate();

                    //Mostrar toast modo manual activado
                    Toast.makeText(activity.getApplicationContext(), getString(R.string.NIGHT_MODE_MANUAL_KEY_TOAST_ON), Toast.LENGTH_LONG).show();

                    //Setear automatico como false si manual esta activado
                    sharedPrefService.saveData(activity.getString(R.string.NIGHT_MODE_AUTO_KEY), false);
                }

                //Si modo manual no esta activado
                else {

                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    activity.setTheme(R.style.AppTheme);
                    activity.recreate();

                    //Mostrar toast modo manual desactivado
                    Toast.makeText(activity.getApplicationContext(), activity.getString(R.string.NIGHT_MODE_MANUAL_KEY_TOAST_OFF), Toast.LENGTH_LONG).show();
                }
            }

            //MODO AUTOMATICO
            //Preferencia modo noche automatico
            else if (key.equals(activity.getString(R.string.NIGHT_MODE_AUTO_KEY))) {

                boolean autoMode = (boolean) sharedPrefService.getData(activity.getString(R.string.NIGHT_MODE_MANUAL_KEY), false);

                //Si automatico esta activado, preguntar el estado del modo
                if (autoMode) {

                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

                    activity.setTheme(R.style.AppTheme);
                    activity.recreate();

                    //Mostrar toast con mensaje de modo noche automatico activado
                    Toast.makeText(activity.getApplicationContext(), activity.getString(R.string.NIGHT_MODE_AUTO_KEY_TOAST_ON), Toast.LENGTH_LONG).show();

                    //Setear automatico como false si manual esta activado
                    sharedPrefService.getData(activity.getString(R.string.NIGHT_MODE_MANUAL_KEY), false);
                }

                //Si auto esta desactivado, tema claro por defecto
                else {

                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    activity.setTheme(R.style.AppTheme);
                    activity.recreate();

                    //Mostrar toast con mensaje de modo noche automatico desactivado
                    Toast.makeText(activity.getApplicationContext(), activity.getString(R.string.NIGHT_MODE_AUTO_KEY_TOAST_OFF), Toast.LENGTH_LONG).show();
                }
            }


            /*
             * Preferencias de alertas
             */
            if (key.equals(activity.getString(R.string.FIREBASE_PREF_KEY))) {

                boolean mSuscrito = quakesNotification.checkSuscriptionQuakes();

                //Si el switch esta ON, lanzar toast con SUSCRITO
                if (mSuscrito) {
                    Toast.makeText(getContext(), activity.getString(R.string.FIREBASE_PREF_KEY_TOAST_ALERTAS_ON), Toast.LENGTH_LONG).show();
                }
                //Si el switch esta off lanzar toast con NO SUSCRITO
                else {
                    Toast.makeText(getContext(), activity.getString(R.string.FIREBASE_PREF_KEY_TOAST_ALERTAS_OFF), Toast.LENGTH_LONG).show();
                }

            }

            /*
             * Preferencias de numero de sismos
             */
            if (key.equals(activity.getString(R.string.SHARED_PREF_LIST_QUAKE_NUMBER))) {

                seekBarPreference.setSummary(String.format(activity.getString(R.string.LIST_QUAKE_NUMBER_SUMMARY), seekBarPreference.getValue()));
                sharedPrefService.saveData(activity.getString(R.string.SHARED_PREF_LIST_QUAKE_NUMBER), seekBarPreference.getValue());

                Timber.tag(activity.getString(R.string.TAG_FRAGMENT_SETTINGS)).i(String.format(activity.getString(R.string.TAG_FRAGMENT_SETTINGS_QUAKE_LIST_LIMIT), seekBarPreference.getValue()));
            }

        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        }
    }
}