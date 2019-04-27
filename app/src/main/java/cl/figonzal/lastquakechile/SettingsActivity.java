package cl.figonzal.lastquakechile;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceFragmentCompat;

import java.util.Objects;

import cl.figonzal.lastquakechile.services.MyFirebaseMessagingService;
import cl.figonzal.lastquakechile.services.QuakeUtils;

public class SettingsActivity extends AppCompatActivity {

	@Override
	protected void onCreate (Bundle savedInstanceState) {
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
				if (sharedPreferences.getBoolean(activity.getString(R.string.NIGHT_MODE_MANUAL_KEY), false)) {
					AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
					activity.setTheme(R.style.DarkAppTheme);
					activity.recreate();

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
				}
			}

			if (key.equals(activity.getString(R.string.FIREBASE_PREF_KEY))) {
				MyFirebaseMessagingService.checkSuscription(activity);
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