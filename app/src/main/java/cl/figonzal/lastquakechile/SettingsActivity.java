package cl.figonzal.lastquakechile;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceFragmentCompat;

import java.util.Objects;

import cl.figonzal.lastquakechile.services.MyFirebaseMessagingService;
import cl.figonzal.lastquakechile.services.QuakeUtils;

public class SettingsActivity extends AppCompatActivity {

	@Override
	public void onCreate (Bundle savedInstanceState) {
		QuakeUtils.checkNightMode(this);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings_activity);
		getSupportFragmentManager()
				.beginTransaction()
				.replace(R.id.settings, new SettingsFragment())
				.commit();

		Toolbar mToolbar = findViewById(R.id.tool_bar_settings);
		setSupportActionBar(mToolbar);

		Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle(R.string.settings);
	}


	@Override
	protected void onResume () {
		super.onResume();
		QuakeUtils.checkNightMode(this);
	}

	public static class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

		private Activity activity;

		@Override
		public void onCreatePreferences (Bundle savedInstanceState, String rootKey) {
			setPreferencesFromResource(R.xml.root_preferences, rootKey);
			if (getActivity() != null) {
				activity = getActivity();
			}
		}

		@Override
		public void onSharedPreferenceChanged (final SharedPreferences sharedPreferences,
		                                       String key) {

			//Preferencia modo noche manual
			if (key.equals(activity.getString(R.string.NIGHT_MODE_MANUAL_KEY))) {

				//Si el modo manual esta activado
				if (sharedPreferences.getBoolean(activity.getString(R.string.NIGHT_MODE_MANUAL_KEY),
						false)) {
					AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
					activity.setTheme(R.style.DarkAppTheme);
					activity.recreate();

					//Mostrar toast modo manual activado
					Toast.makeText(activity.getApplicationContext(),
							getString(R.string.NIGHT_MODE_MANUAL_KEY_TOAST_ON),
							Toast.LENGTH_LONG).show();

					//Setear automatico como false si manual esta activado
					SharedPreferences.Editor edit = sharedPreferences.edit();
					edit.putBoolean(activity.getString(R.string.NIGHT_MODE_AUTO_KEY), false);
					edit.apply();

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

			//MODO AUTOMATICO
			//Preferencia modo noche automatico
			else if (key.equals(activity.getString(R.string.NIGHT_MODE_AUTO_KEY))) {

				//Si automatico esta activado, preguntar el estado del modo
				if (sharedPreferences.getBoolean(activity.getString(R.string.NIGHT_MODE_AUTO_KEY),
						false)) {
					AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO);

					//Obtener el estado del modo
					int modeNightType = activity.getResources().getConfiguration().uiMode &
							Configuration.UI_MODE_NIGHT_MASK;

					//Detecta modo noche automatico como YES
					if (modeNightType == Configuration.UI_MODE_NIGHT_YES) {
						activity.setTheme(R.style.DarkAppTheme);
					} else if (modeNightType == Configuration.UI_MODE_NIGHT_NO) {
						activity.setTheme(R.style.AppTheme);
					}
					activity.recreate();

					//Mostrar toast con mensaje de modo noche automatico activado
					Toast.makeText(activity.getApplicationContext(),
							activity.getString(R.string.NIGHT_MODE_AUTO_KEY_TOAST_ON),
							Toast.LENGTH_LONG).show();
					//Setear automatico como false si manual esta activado
					SharedPreferences.Editor edit = sharedPreferences.edit();
					edit.putBoolean(activity.getString(R.string.NIGHT_MODE_MANUAL_KEY), false);
					edit.apply();
				}
				//Si auto esta desactivado, tema claro por defecto
				else {
					AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
					activity.setTheme(R.style.AppTheme);
					activity.recreate();

					//Mostrar toast con mensaje de modo noche automatico desactivado
					Toast.makeText(activity.getApplicationContext(),
							activity.getString(R.string.NIGHT_MODE_AUTO_KEY_TOAST_OFF),
							Toast.LENGTH_LONG).show();
				}
			}

			//PREFERENCIA DE ALERTAS
			if (key.equals(activity.getString(R.string.FIREBASE_PREF_KEY))) {
				MyFirebaseMessagingService.checkSuscription(activity);

				//Si el switch esta ON, lanzar toast con SUSCRITO
				if (sharedPreferences.getBoolean(activity.getString(R.string.FIREBASE_PREF_KEY),
						true)) {
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

		}

		@Override
		public void onResume () {
			super.onResume();
			getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
		}

		@Override
		public void onPause () {
			super.onPause();
			getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
		}
	}
}