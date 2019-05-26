package cl.figonzal.lastquakechile.views;

import android.animation.IntEvaluator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.crashlytics.android.Crashlytics;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareHashtag;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import cl.figonzal.lastquakechile.R;
import cl.figonzal.lastquakechile.services.QuakeUtils;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class QuakeDetailsActivity extends AppCompatActivity implements OnMapReadyCallback {


	private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
	/*
			ATRIBUTOS MAPA
		 */
	private GoogleMap mGoogleMap;
	private Bundle mMapViewBundle;
	private MapView mMapView;

	private ShareDialog mShareDialog;
	private SharePhotoContent mSharePhotoContent;
	private SharePhoto mSharePhoto;
	private CallbackManager mCallBackManager;
	private Uri mBitmapUri;
	private TextView mTvCiudad, mTvReferencia, mTvEscala, mTvMagnitud, mTvProfundidad, mTvFecha,
			mTvHora, mTvGms, mFabTextFB, mFabTextWSP, mFabTextGM, mTvEstado;
	private ImageView mIvMapa, mIvSensible, mIvMagColor, mIvEstado;
	private String mCiudad;
	private String mReferencia;
	private String mDmsLat;
	private String mDmsLong;
	private String mFechaLocal;
	private String mEscala;
	private String mFotoUrl;
	private String mEstado;
	private String mLatitud;
	private String mLongitud;
	private Double mMagnitud, mProfundidad;
	private Map<String, Long> mTiempos;
	private boolean mSensible;
	private Bitmap mBitmapFB;
	private boolean mIsFabOpen = false;
	private FloatingActionButton mFabShare, mFabFB, mFabWSP, mFabGM;
	private View mOverlay;
	private int pulseCount = 4;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		QuakeUtils.checkNightMode(this);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_quake_details);

		mMapViewBundle = null;
		if (savedInstanceState != null) {
			mMapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
		}
		mMapView = findViewById(R.id.map);
		mMapView.onCreate(mMapViewBundle);
		mMapView.getMapAsync(this);

		//Setting toolbar
		Toolbar mToolbar = findViewById(R.id.tool_bar_detail);
		setSupportActionBar(mToolbar);

		//Muestra la flecha en toolbar para volver atras
		Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

		//Obtener datos desde intent
		Bundle mBundle = getIntent().getExtras();

		//TEXTVIEWS
		mTvCiudad = findViewById(R.id.tv_ciudad_detail);
		mTvReferencia = findViewById(R.id.tv_referencia_detail);
		mTvEscala = findViewById(R.id.tv_escala);
		mTvMagnitud = findViewById(R.id.tv_magnitud_detail);
		mTvProfundidad = findViewById(R.id.tv_epicentro);
		mTvFecha = findViewById(R.id.tv_fecha);
		mTvGms = findViewById(R.id.tv_gms);
		mTvHora = findViewById(R.id.tv_hora_detail);
		mTvEstado = findViewById(R.id.tv_estado);

		//IMAGE VIEWS
		mIvSensible = findViewById(R.id.iv_sensible_detail);
		mIvMagColor = findViewById(R.id.iv_mag_color_detail);
		mIvEstado = findViewById(R.id.iv_estado);
		mIvMapa = findViewById(R.id.iv_map_quake);

		//SETEO DE FLOATING BUTTONS
		mFabShare = findViewById(R.id.fab_share);
		mFabFB = findViewById(R.id.fab_fb);
		mFabWSP = findViewById(R.id.fab_wsp);
		mFabGM = findViewById(R.id.fab_gmail);

		//Overlay
		mOverlay = findViewById(R.id.quake_details_container);
		mOverlay.setVisibility(View.GONE);

		if (mBundle != null) {

			//OBTENCION DE INFO DESDE INTENT
			mCiudad = mBundle.getString(getString(R.string.INTENT_CIUDAD));
			mReferencia = mBundle.getString(getString(R.string.INTENT_REFERENCIA));
			mMagnitud = mBundle.getDouble(getString(R.string.INTENT_MAGNITUD));
			mProfundidad = mBundle.getDouble(getString(R.string.INTENT_PROFUNDIDAD));
			mEscala = mBundle.getString(getString(R.string.INTENT_ESCALA));
			//TODO: Agregar mSensible a los detalles del sismo
			mSensible = mBundle.getBoolean(getString(R.string.INTENT_SENSIBLE));
			mFotoUrl = mBundle.getString(getString(R.string.INTENT_LINK_FOTO));
			mEstado = mBundle.getString(getString(R.string.INTENT_ESTADO));
			mLatitud = mBundle.getString(getString(R.string.INTENT_LATITUD));
			mLongitud = mBundle.getString(getString(R.string.INTENT_LONGITUD));

			//Calculo de Grados,Minutos y segundos de
			//mLatitud y mLongitud
			calculateGMS(mLatitud, mLongitud);

			//Configuración de fechas locales y utc
			dateConfig(mBundle);

			//Seteo de textview
			setTextViews();

			//Seteo de floating buttons
			setFloatingButtons();
		}
	}

	/**
	 * Funcion encargada de la logica de los botones flotantes
	 */
	private void setFloatingButtons() {

		//Boton compartir central
		mFabShare.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				Log.d("FAB SHARE", "CLICKED");

				if (!mIsFabOpen) {
					showFabMenu();
				} else {
					closeFabMenu();
				}
			}
		});

		//Logica de overlay
		mOverlay.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				Log.d(getString(R.string.TAG_OVERLAY_DETAILS),
						getString(R.string.TAG_OVERLAY_DETAILS_RESULT));
				Crashlytics.log(Log.DEBUG, getString(R.string.TAG_OVERLAY_DETAILS),
						getString(R.string.TAG_OVERLAY_DETAILS_RESULT));

				if (mIsFabOpen) {
					closeFabMenu();
				}
			}
		});


		//Boton compartir facebook
		mFabFB.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent mIntent =
						getPackageManager().getLaunchIntentForPackage(getString(R.string.PACKAGE_NAME_FB));

				//Si no existe el paquete
				if (mIntent == null) {
					QuakeUtils.doInstallation(getString(R.string.PACKAGE_NAME_FB),
							getApplicationContext());
				} else {

					//Si esta instalada hacer share
					Log.d(getString(R.string.TAG_INTENT_SHARE),
							getString(R.string.TAG_INTENT_SHARE_FB));
					Crashlytics.log(Log.DEBUG, getString(R.string.TAG_INTENT_SHARE),
							getString(R.string.TAG_INTENT_SHARE_FB));

					mCallBackManager = CallbackManager.Factory.create();
					mShareDialog = new ShareDialog(QuakeDetailsActivity.this);


					//Share foto del sismo
					mSharePhoto = new SharePhoto.Builder()
							.setBitmap(mBitmapFB)
							.build();

					mSharePhotoContent = new SharePhotoContent.Builder()
							.addPhoto(mSharePhoto)
							.setShareHashtag(new ShareHashtag.Builder()
									.setHashtag("#SismoChile")
									.build())
							.build();

					mShareDialog.registerCallback(mCallBackManager,
							new FacebookCallback<Sharer.Result>() {
								@Override
								public void onSuccess(Sharer.Result result) {
									Toast.makeText(getApplicationContext(),
											getString(R.string.TAG_TOAST_SHARE_FB_OK),
											Toast.LENGTH_LONG).show();
									Log.d(getString(R.string.TAG_INTENT_SHARE_FB_LOG),
											getString(R.string.TAG_INTENT_SHARE_FB_OK_MESSAGE));
									Crashlytics.log(Log.DEBUG,
											getString(R.string.TAG_INTENT_SHARE_FB_LOG)
											, getString(R.string.TAG_INTENT_SHARE_FB_OK_MESSAGE));
								}

								@Override
								public void onCancel() {
									Toast.makeText(getApplicationContext(),
											getString(R.string.TAG_TOAST_SHARE_FB_CANCEL),
											Toast.LENGTH_LONG).show();
									Log.d(getString(R.string.TAG_INTENT_SHARE_FB_LOG),
											getString(R.string.TAG_INTENT_SHARE_FB_CANCEL_MESSAGE));
									Crashlytics.log(Log.DEBUG,
											getString(R.string.TAG_INTENT_SHARE_FB_LOG)
											,
											getString(R.string.TAG_INTENT_SHARE_FB_CANCEL_MESSAGE));
								}

								@Override
								public void onError(FacebookException error) {
									Toast.makeText(getApplicationContext(),
											getString(R.string.TAG_TOAST_SHARE_FB_ERROR),
											Toast.LENGTH_LONG).show();
									Log.d(getString(R.string.TAG_INTENT_SHARE_FB_LOG),
											getString(R.string.TAG_INTENT_SHARE_FB_ERROR_MESSAGE) + "-" + error);
									Crashlytics.log(Log.DEBUG,
											getString(R.string.TAG_INTENT_SHARE_FB_LOG)
											,
											getString(R.string.TAG_INTENT_SHARE_FB_ERROR_MESSAGE) + "-" + error);
								}
							});

					if (ShareDialog.canShow(SharePhotoContent.class)) {
						mShareDialog.show(mSharePhotoContent);
						Log.d(getString(R.string.TAG_INTENT_SHARE_DIALOG),
								getString(R.string.TAG_INTENT_SHARE_DIALOG_MESSAGE));
						Crashlytics.log(Log.DEBUG, getString(R.string.TAG_INTENT_SHARE_DIALOG),
								getString(R.string.TAG_INTENT_SHARE_DIALOG_MESSAGE));
					}
				}
			}
		});

		mFabWSP.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent mIntent =
						getPackageManager().getLaunchIntentForPackage(getString(R.string.PACKAGE_NAME_WSP));

				//Si no existe el paquete
				if (mIntent == null) {
					QuakeUtils.doInstallation(getString(R.string.PACKAGE_NAME_WSP),
							getApplicationContext());
				} else {
					Log.d(getString(R.string.TAG_INTENT_SHARE),
							getString(R.string.TAG_INTENT_SHARE_WSP));
					Crashlytics.log(Log.DEBUG, getString(R.string.TAG_INTENT_SHARE),
							getString(R.string.TAG_INTENT_SHARE_WSP));

					Intent wspIntent = new Intent();
					wspIntent.setAction(Intent.ACTION_SEND);
					wspIntent.setPackage(getString(R.string.PACKAGE_NAME_WSP));
					wspIntent.putExtra(Intent.EXTRA_TEXT, String.format(Locale.US,
							"[Alerta sísmica]\n\n" +
									"Información sismológica\n" +
									"Ciudad: %1$s\n" +
									"Hora Local: %2$s\n" +
									"Magnitud: %3$.1f %4$s\n" +
									"Profundidad: %5$.1f Km\n" +
									"Georeferencia: %6$s\n\n" +
									"Para más información descarga la app LastQuakeChile aquí\n" +
									"%7$s"
							, mCiudad, mFechaLocal, mMagnitud, mEscala, mProfundidad, mReferencia,
							getString(R.string.DEEP_LINK)
					));
					wspIntent.putExtra(Intent.EXTRA_STREAM, mBitmapUri);
					wspIntent.setType("image/*");
					//wspIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

					startActivity(wspIntent);
				}
			}
		});

		mFabGM.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				Intent mIntent =
						getPackageManager().getLaunchIntentForPackage(getString(R.string.PACKAGE_NAME_GMAIL));

				//Si no existe el paquete
				if (mIntent == null) {
					QuakeUtils.doInstallation(getString(R.string.PACKAGE_NAME_GMAIL),
							getApplicationContext());
				} else {

					Log.d(getString(R.string.TAG_INTENT_SHARE),
							getString(R.string.TAG_INTENT_SHARE_GM));
					Crashlytics.log(Log.DEBUG, getString(R.string.TAG_INTENT_SHARE),
							getString(R.string.TAG_INTENT_SHARE_GM));

					Intent gmIntent = new Intent();
					gmIntent.setAction(Intent.ACTION_SEND);
					gmIntent.setPackage(getString(R.string.PACKAGE_NAME_GMAIL));
					gmIntent.putExtra(Intent.EXTRA_SUBJECT, String.format(Locale.US, "[Alerta " +
							"sísmica] - %1$.1f Richter en %2$s", mMagnitud, mCiudad));
					gmIntent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(
							String.format(Locale.US,
									"<h3>\n" +
											"  Información sismológica\n" +
											"</h3>\n" +
											"\n" +
											"<table>\n" +
											"  <tr><td>Hora Local: </td><td>%1$s</td></tr><br>\n" +
											"  <tr><td>Ciudad: </td><td>%2$s</td></tr><br>\n" +
											"  <tr><td>Magnitud: </td><td>%3$.1f " +
											"%4$s</td></tr><br>\n" +
											"  <tr><td>Profundidad: </td><td>%5$.1f " +
											"Km</td></tr><br>\n" +
											"  <tr><td>Georeferencia: " +
											"</td><td>%6$s</td></tr><br>\n" +
											"  <tr><td>Latitud: </td><td>%7$s</td></tr><br>\n" +
											"  <tr><td>Longitud: </td><td>%8$s</td></tr><br>\n" +
											"  <tr><td>Posicion GMS: </td><td>%9$s - " +
											"%10$s</td></tr><br>\n" +
											" \n" +
											"</table>\n" +
											"\n" +
											"<h5>\n" +
											"  Para más información descarga la app " +
											"LastQuakeChile" +
											" aquí %11$s \n" +
											"</h5>"
									, mFechaLocal, mCiudad, mMagnitud, mEscala, mProfundidad,
									mReferencia, mLatitud, mLongitud, mDmsLat, mDmsLong,
									getString(R.string.DEEP_LINK))));

					gmIntent.putExtra(Intent.EXTRA_STREAM, mBitmapUri);
					gmIntent.setType("image/*");

					startActivity(gmIntent);
				}
			}
		});
	}

	/**
	 * Funcion encargada de la logica del calculo de grados,minutos y segundo, tanto de lalatitud
	 * como de la mLongitud
	 *
	 * @param latitud  Latitud del sismo
	 * @param longitud Longitud del sismo
	 */
	private void calculateGMS(String latitud, String longitud) {

		//Conversion de mLatitud a dms
		double mLatUbicacion = Double.parseDouble(Objects.requireNonNull(latitud));
		if (mLatUbicacion < 0) {
			mDmsLat = getString(R.string.coordenadas_sur);
		} else {
			mDmsLat = getString(R.string.coordenadas_norte);
		}

		//Calculo de lat to GMS
		Map<String, Double> mMapLatDMS = QuakeUtils.latLonToDMS(mLatUbicacion);
		Double mLatGradosDMS = mMapLatDMS.get("grados");
		Double mLatMinutosDMS = mMapLatDMS.get("minutos");
		Double mLatSegundosDMS = mMapLatDMS.get("segundos");
		mDmsLat = String.format(Locale.US, "%.1f° %.1f' %.1f'' %s", mLatGradosDMS,
				mLatMinutosDMS, mLatSegundosDMS, mDmsLat);

		double mLongUbicacion = Double.parseDouble(Objects.requireNonNull(longitud));
		if (mLongUbicacion < 0) {
			mDmsLong = getString(R.string.coordenadas_oeste);
		} else {
			mDmsLong = getString(R.string.coordenadas_este);
		}

		//Calculo de long to GMS
		Map<String, Double> mMapLongDMS = QuakeUtils.latLonToDMS(mLongUbicacion);
		Double mLongGradosDMS = mMapLongDMS.get("grados");
		Double mLongMinutosDMS = mMapLongDMS.get("minutos");
		Double mLongSegundosDMS = mMapLongDMS.get("segundos");
		mDmsLong = String.format(Locale.US, "%.1f° %.1f' %.1f'' %s", mLongGradosDMS,
				mLongMinutosDMS, mLongSegundosDMS, mDmsLong);
	}

	/**
	 * Funcion encargada de la logica de las fechas de los sismos.
	 *
	 * @param b Bundle con los datos
	 */
	private void dateConfig(Bundle b) {

		//SECCION CONVERSION DE TIEMPO UTC-LOCAL (DEPENDIENDO SI VIENE DE ADAPTER O DE
		// NOTIFICACION)
		mFechaLocal = b.getString(getString(R.string.INTENT_FECHA_LOCAL));
		String mFechaUtc = b.getString(getString(R.string.INTENT_FECHA_UTC));


		//SI INTENT VIENE DE ADAPTER
		//Convertir mFechaLocal a Date
		//Calcular DHMS de Date fecha_local
		if (mFechaLocal != null) {
			Date mFechaLocal = QuakeUtils.stringToDate(this, this.mFechaLocal);
			mTiempos = QuakeUtils.dateToDHMS(mFechaLocal);
		}

		//Si el intent viene de notificacion
		//Convertir sFechaUtc a Date fecha_utc
		//Convertir Date fecha_utc a Date fecha_local
		//Calcular DHMS de Date fecha_local
		else {

			if (mFechaUtc != null) {
				Date mDateFechaUtc = QuakeUtils.stringToDate(this, mFechaUtc);
				Date mDateFechaLocal = QuakeUtils.utcToLocal(mDateFechaUtc);
				mTiempos = QuakeUtils.dateToDHMS(mDateFechaLocal);

				//Setear string que será usado en textviews de detalle con la fecha transformada
				// de utc a local desde notificacion
				mFechaLocal = QuakeUtils.dateToString(this, mDateFechaLocal);

			}
		}

	}


	/**
	 * Funcion que abre el floating button menu
	 */
	private void showFabMenu() {
		mIsFabOpen = true;

		mFabFB.show();
		mFabWSP.show();
		mFabGM.show();

		mFabTextFB = findViewById(R.id.fab_text_fb);
		mFabTextWSP = findViewById(R.id.fab_text_wsp);
		mFabTextGM = findViewById(R.id.fab_text_gm);

		//Seteado de text en alpha 0 y visible
		mFabTextFB.setAlpha(0f);
		mFabTextFB.setVisibility(View.VISIBLE);
		mFabTextWSP.setAlpha(0f);
		mFabTextWSP.setVisibility(View.VISIBLE);
		mFabTextGM.setAlpha(0f);
		mFabTextGM.setVisibility(View.VISIBLE);

		//trasnlaciones de fabs y textos
		mFabFB.animate().translationY(-getResources().getDimension(R.dimen.standard_65));
		mFabWSP.animate().translationY(-getResources().getDimension(R.dimen.standard_130));
		mFabGM.animate().translationY(-getResources().getDimension(R.dimen.standard_195));
		mFabTextFB.animate().translationY(-getResources().getDimension(R.dimen.standard_65));
		mFabTextWSP.animate().translationY(-getResources().getDimension(R.dimen.standard_130));
		mFabTextGM.animate().translationY(-getResources().getDimension(R.dimen.standard_195));

		//Animacion de alpha para textos
		mFabTextWSP.animate().alpha(1.0f).setDuration(500);
		mFabTextFB.animate().alpha(1.0f).setDuration(500);
		mFabTextGM.animate().alpha(1.0f).setDuration(500);

		mOverlay.setAlpha(0f);
		mOverlay.setVisibility(View.VISIBLE);
		mOverlay.animate().alpha(0.85f).setDuration(500);

		Log.d(getString(R.string.TAG_FAB_MENU), getString(R.string.TAG_FAB_MENU_OPEN));
		Crashlytics.log(Log.DEBUG, getString(R.string.TAG_FAB_MENU),
				getString(R.string.TAG_FAB_MENU_OPEN));
	}

	/**
	 * Funcion que cierra el floating button menu
	 */
	private void closeFabMenu() {
		mIsFabOpen = false;
		mFabFB.animate().translationY(0);
		mFabWSP.animate().translationY(0);
		mFabGM.animate().translationY(0);

		mFabGM.hide();
		mFabWSP.hide();
		mFabFB.hide();

		mFabTextFB.animate().translationY(0);
		mFabTextFB.setVisibility(View.GONE);
		mFabTextGM.animate().translationY(0);
		mFabTextGM.setVisibility(View.GONE);
		mFabTextWSP.animate().translationY(0);
		mFabTextWSP.setVisibility(View.GONE);

		//Animacion de alpha para textos
		mFabTextWSP.animate().alpha(0.0f).setDuration(500);
		mFabTextFB.animate().alpha(0.0f).setDuration(500);
		mFabTextGM.animate().alpha(0.0f).setDuration(500);

		//Animacion CLOSE de mOverlay
		mOverlay.setAlpha(0.85f);
		mOverlay.animate().alpha(0.0f).setDuration(500);
		mOverlay.setVisibility(View.GONE);

		Log.d(getString(R.string.TAG_FAB_MENU), getString(R.string.TAG_FAB_MENU_CLOSE));
		Crashlytics.log(Log.DEBUG, getString(R.string.TAG_FAB_MENU),
				getString(R.string.TAG_FAB_MENU_CLOSE));

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		// Respond to the action bar's Up/Home button
		if (item.getItemId() == android.R.id.home) {

			Log.d(getString(R.string.TAG_INTENT_DETALLE_HOME_UP),
					getString(R.string.TAG_INTENT_DETALLE_HOME_UP_RESPONSE));

			Crashlytics.log(Log.DEBUG, getString(R.string.TAG_INTENT_DETALLE_HOME_UP),
					getString(R.string.TAG_INTENT_DETALLE_HOME_UP_RESPONSE));

			Intent intent = new Intent(this, MainActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onBackPressed() {

		if (!mIsFabOpen) {
			super.onBackPressed();
		} else {
			closeFabMenu();
		}
	}

	/*
		Funcion que permite el callback de facebook
	*/
	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		mCallBackManager.onActivityResult(requestCode, resultCode, data);
	}


	/**
	 * Funcion que permite setear los textview del detalle con la información procesada
	 */
	private void setTextViews() {
		//Setear titulo de mCiudad en activity
		Objects.requireNonNull(getSupportActionBar()).setTitle(mCiudad);

		//Setear nombre mCiudad
		mTvCiudad.setText(mCiudad);

		//Setear mReferencia
		mTvReferencia.setText(mReferencia);

		//Setear mMagnitud en en circulo de color
		mTvMagnitud.setText(String.format(getString(R.string.magnitud), mMagnitud));

		//Setear el color de background dependiendo de mMagnitud del sismo
		mIvMagColor.setColorFilter(getColor(QuakeUtils.getMagnitudeColor(mMagnitud, false)));

		//Setear mProfundidad
		mTvProfundidad.setText(String.format(Locale.US,
				getString(R.string.quake_details_profundidad), mProfundidad));

		//Setear fecha
		mTvFecha.setText(mFechaLocal);

		//Setear posicionamiento
		mTvGms.setText(String.format(getString(R.string.format_coordenadas), mDmsLat, mDmsLong));

		//SETEO DE ESTADO
		if (mEstado != null) {
			QuakeUtils.setStatusImage(getApplicationContext(), mEstado, mTvEstado, mIvEstado);
		}

		//SETEO DE HORA
		if (mTiempos != null) {
			QuakeUtils.setTimeToTextView(getApplicationContext(), mTiempos, mTvHora);
		}

		//SETEO DE ESCALA
		if (mEscala != null) {
			QuakeUtils.setEscala(getApplicationContext(), mEscala, mTvEscala);
		}

		//SETEO SISMO SENSIBLE
		if (mSensible) {
			mIvSensible.setVisibility(View.VISIBLE);
		}

		//SETEO DE IMAGEN MAPA
		final Uri uri = Uri.parse(mFotoUrl);
		Glide.with(this)
				.load(uri)
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
						mIvMapa.setImageDrawable(getDrawable(R.drawable.not_found));
						Log.d(getString(R.string.TAG_INTENT_SHARE_BITMAP),
								getString(R.string.TAG_INTENT_SHARE_BITMAP_MESSAGE_FAIL));
						Crashlytics.log(Log.DEBUG, getString(R.string.TAG_INTENT_SHARE_BITMAP),
								getString(R.string.TAG_INTENT_SHARE_BITMAP_MESSAGE_FAIL));
						return false;
					}

					//No es necesario usarlo (If u want)
					@Override
					public boolean onResourceReady(Drawable resource, Object model,
												   Target<Drawable> target, DataSource dataSource
							, boolean isFirstResource) {
						mBitmapUri = QuakeUtils.getLocalBitmapUri(resource,
								getApplicationContext());
						mBitmapFB = ((BitmapDrawable) resource).getBitmap();
						Log.d(getString(R.string.TAG_INTENT_SHARE_BITMAP),
								getString(R.string.TAG_INTENT_SHARE_BITMAP_MESSAGE));
						Crashlytics.log(Log.DEBUG, getString(R.string.TAG_INTENT_SHARE_BITMAP),
								getString(R.string.TAG_INTENT_SHARE_BITMAP_MESSAGE));
						return false;
					}
				})
				.into(mIvMapa);


	}

	@Override
	public void onMapReady(GoogleMap googleMap) {

		mGoogleMap = googleMap;

		//NIGHT MODE MAPA
		int nightModeFlags = getResources().getConfiguration().uiMode &
				Configuration.UI_MODE_NIGHT_MASK;
		if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
			googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getApplicationContext(),
					R.raw.map_night_mode));
		}

		mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
		mGoogleMap.setMinZoomPreference(5.0f);
		mGoogleMap.getUiSettings().setZoomGesturesEnabled(false);
		mGoogleMap.getUiSettings().setZoomControlsEnabled(true);

		mGoogleMap.getUiSettings().setTiltGesturesEnabled(false);
		mGoogleMap.getUiSettings().setScrollGesturesEnabled(false);

		mGoogleMap.getUiSettings().setMapToolbarEnabled(false);
		mGoogleMap.getUiSettings().setRotateGesturesEnabled(false);
		mGoogleMap.getUiSettings().setCompassEnabled(false);

		LatLng mLatLong = new LatLng(Double.parseDouble(mLatitud), Double.parseDouble(mLongitud));

		int mIdColor = QuakeUtils.getMagnitudeColor(mMagnitud, true);

		mGoogleMap.addCircle(new CircleOptions()
				.center(mLatLong)
				.radius(90000)
				.fillColor(getColor(mIdColor))
				.strokeColor(getColor(R.color.grey_dark_alpha)));

		final Circle circle_anim = mGoogleMap.addCircle(new CircleOptions()
				.center(mLatLong)
				.radius(90000)
				.strokeWidth(1)
				.strokeColor(getColor(R.color.grey_dark_alpha)));


		ValueAnimator animator = ValueAnimator.ofInt(0, 90000);
		animator.setRepeatMode(ValueAnimator.RESTART);
		animator.setRepeatCount(ValueAnimator.INFINITE);
		animator.setDuration(4000);
		animator.setEvaluator(new IntEvaluator());
		animator.setInterpolator(new AccelerateDecelerateInterpolator());
		animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				float animatedFraction = animation.getAnimatedFraction();
				circle_anim.setRadius(animatedFraction * 140000);
			}
		});
		animator.start();

		final Circle circle_anim2 = mGoogleMap.addCircle(new CircleOptions()
				.center(mLatLong)
				.radius(90000)
				.strokeWidth(1)
				.strokeColor(getColor(R.color.grey_dark_alpha)));


		ValueAnimator animator2 = ValueAnimator.ofInt(0, 90000);
		animator2.setRepeatMode(ValueAnimator.RESTART);
		animator2.setRepeatCount(ValueAnimator.INFINITE);
		animator2.setDuration(4000);
		animator2.setStartDelay(1000);
		animator2.setEvaluator(new IntEvaluator());
		animator2.setInterpolator(new AccelerateDecelerateInterpolator());
		animator2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				float animatedFraction = animation.getAnimatedFraction();
				circle_anim2.setRadius(animatedFraction * 140000);
			}
		});
		animator2.start();

		mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mLatLong, 6.0f));

	}

	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {

		Bundle mMapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
		if (mMapViewBundle == null) {
			mMapViewBundle = new Bundle();
			outState.putBundle(MAPVIEW_BUNDLE_KEY, mMapViewBundle);
		}

		mMapView.onSaveInstanceState(mMapViewBundle);

		super.onSaveInstanceState(outState);
	}

	@Override
	public void onResume() {
		super.onResume();
		mMapView.onResume();

	}

	@Override
	public void onPause() {
		super.onPause();
		mMapView.onPause();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mMapView.onDestroy();
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		mMapView.onLowMemory();
	}

}

