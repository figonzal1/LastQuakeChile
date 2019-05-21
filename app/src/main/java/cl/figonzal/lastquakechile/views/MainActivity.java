package cl.figonzal.lastquakechile.views;

import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;
import androidx.viewpager.widget.ViewPager;
import cl.figonzal.lastquakechile.FragmentPageAdapter;
import cl.figonzal.lastquakechile.R;
import cl.figonzal.lastquakechile.services.MyFirebaseMessagingService;
import cl.figonzal.lastquakechile.services.QuakeUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class MainActivity extends AppCompatActivity {


	private AppBarLayout mAppBarLayout;
	private ImageView mIvFoto;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//Setear configuracion por defecto
		PreferenceManager.setDefaultValues(this, R.xml.root_preferences, false);

		//Checkear si preferencias tiene modo noche
		QuakeUtils.checkNightMode(this);

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);


		Bundle mBundleWelcome = getIntent().getExtras();
		if (mBundleWelcome != null) {
			//Si el usuario viene desde deep link, no se realiza first check
			//Si viene desde Google play, se realiza el check
			if (!mBundleWelcome.getBoolean(getString(R.string.desde_deep_link))) {
				QuakeUtils.checkFirstRun(this, true);
			}
		}

		//Verifica si el celular tiene googleplay services activado
		QuakeUtils.checkPlayServices(this);

		//FIREBASE SECTION
		FirebaseMessaging.getInstance().setAutoInitEnabled(true);
		FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(this,
				new OnSuccessListener<InstanceIdResult>() {
					@Override
					public void onSuccess(InstanceIdResult instanceIdResult) {
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
		mViewPager.setAdapter(new FragmentPageAdapter(getSupportFragmentManager(),
				getApplicationContext()));


		//Seteo de tabs.
		TabLayout mTabLayout = findViewById(R.id.tabs);
		mTabLayout.setupWithViewPager(mViewPager);
		mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {

			@Override
			public void onTabSelected(TabLayout.Tab tab) {
				if (tab.getPosition() == 1) {

					mAppBarLayout.setExpanded(false);
				} else {
					mAppBarLayout.setExpanded(true);
				}
			}

			@Override
			public void onTabUnselected(TabLayout.Tab tab) {

			}

			@Override
			public void onTabReselected(TabLayout.Tab tab) {

			}
		});

		//Setear collapsing toolbar con titulo estatico superior y animacion de colores al recoger
		// toolbar
		CollapsingToolbarLayout mCollapsingToolbarLayout = findViewById(R.id.collapsing_toolbar);
		mCollapsingToolbarLayout.setTitleEnabled(true);

		int modeNightType = getResources().getConfiguration().uiMode &
				Configuration.UI_MODE_NIGHT_MASK;

		//Detecta modo noche automatico como YES
		if (modeNightType == Configuration.UI_MODE_NIGHT_YES) {
			mCollapsingToolbarLayout.setContentScrimColor(getResources().getColor(R.color.colorPrimaryNightMode
					, getTheme()));
		} else if (modeNightType == Configuration.UI_MODE_NIGHT_NO) {
			mCollapsingToolbarLayout.setContentScrimColor(getResources().getColor(R.color.colorPrimary,
					getTheme()));
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
	private void loadImage() {
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
					public boolean onLoadFailed(@Nullable GlideException e, Object model,
					                            Target<Drawable> target,
					                            boolean isFirstResource) {
						mIvFoto.setImageDrawable(getDrawable(R.drawable.not_found));
						return false;
					}

					//No es necesario usarlo (If u want)
					@Override
					public boolean onResourceReady(Drawable resource, Object model,
					                               Target<Drawable> target,
					                               DataSource dataSource,
					                               boolean isFirstResource) {

						return false;
					}
				})
				.into(mIvFoto);
	}


	@Override
	protected void onResume() {
		super.onResume();
		QuakeUtils.checkPlayServices(this);
	}


}
