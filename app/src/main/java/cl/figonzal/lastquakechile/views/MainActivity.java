package cl.figonzal.lastquakechile.views;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;

import cl.figonzal.lastquakechile.FragmentPageAdapter;
import cl.figonzal.lastquakechile.R;
import cl.figonzal.lastquakechile.services.MyFirebaseMessagingService;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class MainActivity extends AppCompatActivity {

	private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
	private AppBarLayout mAppBarLayout;
	private ImageView mIvFoto;

	@Override
	protected void onCreate (Bundle savedInstanceState) {
		AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO);

		//MODO MANUAL
		/*if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
			setTheme(R.style.DarkAppTheme);
		} else {
			setTheme(R.style.AppTheme);
		}*/


		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		//DETECTAR MODO AUTO
		int nightModeFlags =
				getResources().getConfiguration().uiMode &
						Configuration.UI_MODE_NIGHT_MASK;
		switch (nightModeFlags) {
			case Configuration.UI_MODE_NIGHT_YES:
				setTheme(R.style.DarkAppTheme);
				break;

			case Configuration.UI_MODE_NIGHT_NO:
				setTheme(R.style.AppTheme);
				break;
		}

		Switch hola = findViewById(R.id.switchs);

		if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
			hola.setChecked(true);
		} else {
			hola.setChecked(false);
		}
		hola.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged (CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
					recreate();
				} else {
					AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
					recreate();
				}
			}
		});


		Bundle mBundleWelcome = getIntent().getExtras();

		if (mBundleWelcome != null) {

			//Si el usuario viene desde deep link, no se realiza first check
			//Si viene desde Google play, se realiza el check
			if (!mBundleWelcome.getBoolean(getString(R.string.desde_deep_link))) {
				checkFirstRun();
			}
		}

		//Verifica si el celular tiene googleplay services activado
		checkPlayServices();

        /*
            Firebase SECTION
         */
		FirebaseMessaging.getInstance().setAutoInitEnabled(true);
		FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(this,
				new OnSuccessListener<InstanceIdResult>() {
					@Override
					public void onSuccess (InstanceIdResult instanceIdResult) {
						String token = instanceIdResult.getToken();
						Log.d(getString(R.string.TAG_FIREBASE_TOKEN), token);

						//CRASH ANALYTICS LOG
						Crashlytics.log(Log.DEBUG, getString(R.string.TAG_FIREBASE_TOKEN), token);
						Crashlytics.setUserIdentifier(token);


					}
				});

		//Llamada a creacion de canal de notificaciones
		MyFirebaseMessagingService.createNotificationChannel(getApplicationContext());

		//Buscar toolbar en resources
		Toolbar mToolbar = findViewById(R.id.tool_bar);

		//Setear el toolbar sobre el main activity
		setSupportActionBar(mToolbar);

		//Appbar layout para minimizar el collapse toolbar cuando se presiona el tab de mapa
		mAppBarLayout = findViewById(R.id.app_bar);

		//View pager para los fragments (Solo 1 fragment en esta app)
		ViewPager mViewPager = findViewById(R.id.view_pager);
		mViewPager.setAdapter(new FragmentPageAdapter(getSupportFragmentManager()));


		//Seteo de tabs.
		TabLayout mTabLayout = findViewById(R.id.tabs);
		mTabLayout.setupWithViewPager(mViewPager);
		mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {

			@Override
			public void onTabSelected (TabLayout.Tab tab) {
				if (tab.getPosition() == 1) {

					mAppBarLayout.setExpanded(false);
				} else {
					mAppBarLayout.setExpanded(true);
				}
			}

			@Override
			public void onTabUnselected (TabLayout.Tab tab) {

			}

			@Override
			public void onTabReselected (TabLayout.Tab tab) {

			}
		});

		//Setear collapsing toolbar con titulo estatico superior y animacion de colores al recoger
		// toolbar
		CollapsingToolbarLayout mCollapsingToolbarLayout = findViewById(R.id.collapsing_toolbar);
		mCollapsingToolbarLayout.setTitleEnabled(true);



		/*if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
			mCollapsingToolbarLayout.setContentScrimColor(getResources().getColor(R.color.colorPrimaryNightMode
					, getTheme()));
		} else {
			mCollapsingToolbarLayout.setContentScrimColor(getResources().getColor(R.color.colorPrimary,
					getTheme()));
		}*/

		switch (nightModeFlags) {
			case Configuration.UI_MODE_NIGHT_YES:
				mCollapsingToolbarLayout.setContentScrimColor(getResources().getColor(R.color.colorPrimaryNightMode
						, getTheme()));
				break;

			case Configuration.UI_MODE_NIGHT_NO:
				mCollapsingToolbarLayout.setContentScrimColor(getResources().getColor(R.color.colorPrimary,
						getTheme()));
				break;
		}

		//Setear imagen de toolbar con Glide
		mIvFoto = findViewById(R.id.toolbar_image);
		loadImage();


		//Suscribir automaticamente al tema (FIREBASE - Quakes)
		MyFirebaseMessagingService.checkSuscription(this);

	}

	/**
	 * Funcion encargada de cargar la imagen de fondo en el toolbar
	 */
	private void loadImage () {
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
					public boolean onLoadFailed (@Nullable GlideException e, Object model,
					                             Target<Drawable> target,
					                             boolean isFirstResource) {
						mIvFoto.setImageDrawable(getDrawable(R.drawable.not_found));
						return false;
					}

					//No es necesario usarlo (If u want)
					@Override
					public boolean onResourceReady (Drawable resource, Object model,
					                                Target<Drawable> target,
					                                DataSource dataSource,
					                                boolean isFirstResource) {

						return false;
					}
				})
				.into(mIvFoto);
	}

	/**
	 * Funcion encargada de checkear si la aplicación se ha abierto por primera vez
	 */
	private void checkFirstRun () {
		SharedPreferences mSharedPref = getPreferences(Context.MODE_PRIVATE);

		boolean mFirstRun = mSharedPref.getBoolean(getString(R.string.SHARED_PREF_FIRST_RUN),
				true);
		Crashlytics.setBool(getString(R.string.SHARED_PREF_FIRST_RUN), true);

		if (mFirstRun) {

			Log.d(getString(R.string.TAG_FIRST_RUN_STATUS),
					getString(R.string.FIRST_RUN_STATUS_RESPONSE));

			Intent intent = new Intent(MainActivity.this, WelcomeActivity.class);
			startActivity(intent);
			finish();
		}

		//Cambiar a falso, para que proxima vez no abra invitation.
		SharedPreferences.Editor editor = mSharedPref.edit();
		editor.putBoolean(getString(R.string.SHARED_PREF_FIRST_RUN), false);
		editor.apply();

		//Log
		Crashlytics.setBool(getString(R.string.SHARED_PREF_FIRST_RUN), false);
	}


	@Override
	protected void onResume () {
		super.onResume();
		checkPlayServices();
	}

	/**
	 * Funcion que verifica si el dispositivo cuenta con GooglePlayServices actualizado
	 */
	private void checkPlayServices () {
		GoogleApiAvailability mGoogleApiAvailability = GoogleApiAvailability.getInstance();
		int resultCode = mGoogleApiAvailability.isGooglePlayServicesAvailable(this);

		if (resultCode != ConnectionResult.SUCCESS) {

			//Si el error puede ser resuelto por el usuario
			if (mGoogleApiAvailability.isUserResolvableError(resultCode)) {

				Dialog dialog = mGoogleApiAvailability.getErrorDialog(this, resultCode,
						PLAY_SERVICES_RESOLUTION_REQUEST);
				dialog.setCanceledOnTouchOutside(false);
				dialog.show();
			} else {

				//El error no puede ser resuelto por el usuario y la app se cierra
				Log.d(getString(R.string.TAG_GOOGLE_PLAY),
						getString(R.string.GOOGLE_PLAY_NOSOPORTADO));
				Crashlytics.log(Log.DEBUG, getString(R.string.TAG_GOOGLE_PLAY),
						getString(R.string.GOOGLE_PLAY_NOSOPORTADO));
				finish();
			}
		} else {
			//La app puede ser utilizada, google play esta actualizado
			Log.d(getString(R.string.TAG_GOOGLE_PLAY),
					getString(R.string.GOOGLE_PLAY_ACTUALIZADO));
			Crashlytics.log(Log.DEBUG, getString(R.string.TAG_GOOGLE_PLAY),
					getString(R.string.GOOGLE_PLAY_ACTUALIZADO));
		}
	}
}
