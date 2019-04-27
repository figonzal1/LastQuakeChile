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
			if (key.equals("pref_manual_night_mode")) {

				//Si el modo manual esta activado
				if (sharedPreferences.getBoolean("pref_manual_night_mode", false)) {
					AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
					activity.setTheme(R.style.DarkAppTheme);
					activity.recreate();

					//Preference pref = findPreference("pref_auto_night_mode");
					//pref.setEnabled(false);
					SharedPreferences.Editor edit = sharedPreferences.edit();
					edit.putBoolean("pref_auto_night_mode", false);
					edit.apply();

				} else {
					AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
					activity.setTheme(R.style.AppTheme);
					activity.recreate();
				}
			}
			//Preferencia modo noche automatico
			else if (key.equals("pref_auto_night_mode")) {

				//Si automatico esta activado, preguntar el estado del modo
				if (sharedPreferences.getBoolean("pref_auto_night_mode", false)) {
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
				}
				//Si auto esta desactivado, tema claro por defecto
				else {
					AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
					activity.setTheme(R.style.AppTheme);
					activity.recreate();
				}
			}


			if (key.equals("pref_suscrito_quake")) {

				//Si esta suscrito
				if (sharedPreferences.getBoolean("pref_suscrito_quake", true)) {

					MyFirebaseMessagingService.checkSuscription(Objects.requireNonNull(getActivity()));
					/*FirebaseMessaging.getInstance().subscribeToTopic(activity.getString(R
					.string.FIREBASE_TOPIC_NAME))
					 */
							/*.addOnCompleteListener(new OnCompleteListener<Void>() {
								@Override
								public void onComplete (@NonNull Task<Void> task) {
									if (task.isSuccessful()) {

										Log.d(activity.getString(R.string
										.TAG_FIREBASE_SUSCRIPTION),
												activity.getString(R.string
												.TAG_FIREBASE_SUSCRIPTION_RESPONSE1));

										/*SharedPreferences.Editor editor = sharedPreferences
										.edit();
										 */
					//editor.putBoolean(activity.getString(R.string
					// .FIREBASE_SUSCRITO),
					//		true);
					//editor.apply();*/

					//CRASH ANALYTIC LOG
										/*Crashlytics.setBool(activity.getString(R.string
										.FIREBASE_SUSCRITO)
												, true);
										Crashlytics.log(Log.DEBUG,
												activity.getString(R.string
												.TAG_FIREBASE_SUSCRIPTION),
												activity.getString(R.string
												.TAG_FIREBASE_SUSCRIPTION_RESPONSE1));
									}
								}
							});*/
				} else {

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