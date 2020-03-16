package cl.figonzal.lastquakechile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SeekBarPreference;

import java.util.Objects;

import cl.figonzal.lastquakechile.services.Utils;
import cl.figonzal.lastquakechile.services.notifications.FCMNotification;
import cl.figonzal.lastquakechile.views.MainActivity;

public class SettingsActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        /*
         * Checkear MODO NOCHE
         */
        Utils.checkNightMode(this, getWindow());

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings_menu, new SettingsFragment())
                .commit();

        Toolbar mToolbar = findViewById(R.id.tool_bar_settings);
        setSupportActionBar(mToolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.settings);
    }


    @Override
    protected void onResume() {
        super.onResume();
        /*
         * Checkear MODO NOCHE
         */
        Utils.checkNightMode(this, getWindow());
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);

        super.onBackPressed();
    }

    public static class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

        private Activity activity;
        private SeekBarPreference seekBarPreference;
        private SharedPreferences sharedPreferences;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            if (getActivity() != null) {
                activity = getActivity();
                sharedPreferences =
                        activity.getSharedPreferences(getString(R.string.MAIN_SHARED_PREF_KEY),
                                Context.MODE_PRIVATE);
            }
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            /*
             * SeekBar
             */
            seekBarPreference =
                    findPreference(activity.getString(R.string.SHARED_PREF_LIST_QUAKE_NUMBER));
            assert seekBarPreference != null;
            seekBarPreference.setMin(10);
            seekBarPreference.setMax(30);

            String limite =
                    String.valueOf(sharedPreferences.getInt(activity.getString(R.string.SHARED_PREF_LIST_QUAKE_NUMBER), 0));

            //Setear resumen por defecto
            if (limite.equals("0")) {
                seekBarPreference.setValue(15);
                seekBarPreference.setSummary(String.format(activity.getString(R.string.LIST_QUAKE_NUMBER_SUMMARY), "15"));
            } else {
                seekBarPreference.setSummary(String.format(activity.getString(R.string.LIST_QUAKE_NUMBER_SUMMARY), limite));
            }
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                              String key) {

            /*
             * Preferencia Modo Noche
             * MANUAL
             */
            if (key.equals(activity.getString(R.string.NIGHT_MODE_MANUAL_KEY))) {

                //Si el modo manual esta activado
                if (sharedPreferences.getBoolean(activity.getString(R.string.NIGHT_MODE_MANUAL_KEY),
                        false)) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    activity.setTheme(R.style.AppTheme);
                    activity.recreate();

                    //Mostrar toast modo manual activado
                    Toast.makeText(activity.getApplicationContext(),
                            getString(R.string.NIGHT_MODE_MANUAL_KEY_TOAST_ON),
                            Toast.LENGTH_LONG).show();

                }
                //Si modo manual no esta activado
                else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    activity.setTheme(R.style.AppTheme);
                    activity.recreate();
                    //Mostrar toast modo manual desactivado
                    Toast.makeText(activity.getApplicationContext(),
                            activity.getString(R.string.NIGHT_MODE_MANUAL_KEY_TOAST_OFF),
                            Toast.LENGTH_LONG).show();
                }
            }


            /*
             * Preferencias de alertas
             */
            if (key.equals(activity.getString(R.string.FIREBASE_PREF_KEY))) {

                boolean mSuscrito = FCMNotification.checkSuscriptionQuakes(activity);

                //Si el switch esta ON, lanzar toast con SUSCRITO
                if (mSuscrito) {
                    Toast.makeText(getContext(),
                            activity.getString(R.string.FIREBASE_PREF_KEY_TOAST_ALERTAS_ON),
                            Toast.LENGTH_LONG).show();

                }
                //Si el switch esta off lanzar toast con NO SUSCRITO
                else {
                    Toast.makeText(getContext(),
                            activity.getString(R.string.FIREBASE_PREF_KEY_TOAST_ALERTAS_OFF),
                            Toast.LENGTH_LONG).show();
                }

            }

            /*
             * Preferencias de numero de sismos
             */
            if (key.equals(activity.getString(R.string.SHARED_PREF_LIST_QUAKE_NUMBER))) {

                seekBarPreference.setSummary(String.format(activity.getString(R.string.LIST_QUAKE_NUMBER_SUMMARY), String.valueOf(seekBarPreference.getValue())));

                SharedPreferences sharedPrefListQuakes =
                        activity.getSharedPreferences(activity.getString(R.string.MAIN_SHARED_PREF_KEY), Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPrefListQuakes.edit();
                editor.putInt(activity.getString(R.string.SHARED_PREF_LIST_QUAKE_NUMBER),
                        seekBarPreference.getValue());
                editor.apply();

                Log.e(activity.getString(R.string.TAG_FRAGMENT_SETTINGS),
                        String.format(activity.getString(R.string.TAG_FRAGMENT_SETTINGS_QUAKE_LIST_LIMIT), seekBarPreference.getValue()));
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