package cl.figonzal.lastquakechile;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceFragmentCompat;

import java.util.Objects;

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
		public void onSharedPreferenceChanged (SharedPreferences sharedPreferences, String key) {

			//Preferencia modo manual
			if (key.equals("pref_manual_night_mode")) {

				//Si el modo manual esta activado
				if (sharedPreferences.getBoolean("pref_manual_night_mode", false)) {
					AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
					activity.setTheme(R.style.DarkAppTheme);
					activity.recreate();

					//Preference pref = findPreference("pref_auto_night_mode");
					//pref.setSelectable(false);
				} else {
					AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
					activity.setTheme(R.style.AppTheme);
					activity.recreate();
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