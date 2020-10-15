package cl.figonzal.lastquakechile.views.activities;

import android.animation.Animator;
import android.animation.IntEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import cl.figonzal.lastquakechile.R;
import cl.figonzal.lastquakechile.managers.DateManager;
import cl.figonzal.lastquakechile.managers.PackageManager;
import cl.figonzal.lastquakechile.managers.ViewsManager;
import cl.figonzal.lastquakechile.model.QuakeModel;
import cl.figonzal.lastquakechile.services.NightModeService;
import timber.log.Timber;

public class QuakeDetailsActivity extends AppCompatActivity implements OnMapReadyCallback {


    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";

    /*
     * Atributos Mapa
     */
    private GoogleMap mGoogleMap;
    private Bundle mMapViewBundle;
    private MapView mMapView;

    private Uri mBitmapUri;
    private TextView mTvCiudad;
    private TextView mTvReferencia;
    private TextView mTvEscala;
    private TextView mTvMagnitud;
    private TextView mTvProfundidad;
    private TextView mTvFecha;
    private TextView mTvHora;
    private TextView mTvGms;
    private TextView mFabTextWSP;
    private TextView mFabTextGM;
    private TextView mTvEstado;
    private ImageView mIvSensible, mIvMagColor, mIvEstado;
    private String mDmsLat;
    private String mDmsLong;
    private String mFechaLocal;
    private Map<String, Long> mTiempos;
    private boolean mIsFabOpen = false;
    private FloatingActionButton mFabShare;
    private FloatingActionButton mFabWSP;
    private FloatingActionButton mFabGM;
    private View mOverlay;

    private DateManager dateManager;
    private ViewsManager viewsManager;
    private PackageManager packageManager;
    private QuakeModel quakeModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quake_details);

        //Check night mode
        new NightModeService(this, this.getLifecycle(), getWindow());

        mMapViewBundle = null;
        if (savedInstanceState != null) {
            mMapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }

        initResources();
    }

    private void initResources() {

        dateManager = new DateManager();
        viewsManager = new ViewsManager();
        packageManager = new PackageManager();

        mMapView = findViewById(R.id.map);
        mMapView.onCreate(mMapViewBundle);
        mMapView.getMapAsync(this);

        //Setting toolbar
        Toolbar mToolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(mToolbar);

        //Muestra la flecha en toolbar para volver atras
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

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

        //SETEO DE FLOATING BUTTONS
        mFabShare = findViewById(R.id.fab_share);
        mFabWSP = findViewById(R.id.fab_wsp);
        mFabGM = findViewById(R.id.fab_gmail);

        //Overlay
        mOverlay = findViewById(R.id.overlay);
        mOverlay.setVisibility(View.GONE);

        handleBundles();
    }

    private void handleBundles() {

        //Obtener datos desde intent
        Bundle mBundle = getIntent().getExtras();

        if (mBundle != null) {

            //OBTENCION DE INFO DESDE INTENT
            quakeModel = new QuakeModel();

            quakeModel.setCiudad(mBundle.getString(getString(R.string.INTENT_CIUDAD)));
            quakeModel.setReferencia(mBundle.getString(getString(R.string.INTENT_REFERENCIA)));
            quakeModel.setMagnitud(mBundle.getDouble(getString(R.string.INTENT_MAGNITUD)));
            quakeModel.setProfundidad(mBundle.getDouble(getString(R.string.INTENT_PROFUNDIDAD)));
            quakeModel.setEscala(mBundle.getString(getString(R.string.INTENT_ESCALA)));
            quakeModel.setSensible(mBundle.getBoolean(getString(R.string.INTENT_SENSIBLE)));
            quakeModel.setEstado(mBundle.getString(getString(R.string.INTENT_ESTADO)));
            quakeModel.setLatitud(mBundle.getString(getString(R.string.INTENT_LATITUD)));
            quakeModel.setLongitud(mBundle.getString(getString(R.string.INTENT_LONGITUD)));

            //Calculo de Grados,Minutos y segundos de
            //mLatitud y mLongitud
            calculateGMS(quakeModel.getLatitud(), quakeModel.getLongitud());

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
        mFabShare.setOnClickListener(v -> {

            Timber.i(getString(R.string.TAG_FAB_SHARE_STATUS) + ": " + getString(R.string.TAG_FAB_SHARE_STATUS_CLICKED));

            if (!mIsFabOpen) {

                makeSnapshot();

                showFabMenu();
            } else {
                closeFabMenu();
            }
        });

        //Logica de overlay
        mOverlay.setOnClickListener(v -> {

            Timber.i(getString(R.string.TAG_OVERLAY_DETAILS) + ": " + getString(R.string.TAG_OVERLAY_DETAILS_RESULT));

            if (mIsFabOpen) {
                closeFabMenu();
            }
        });

        mFabWSP.setOnClickListener(v -> {

            Intent mIntent = getPackageManager().getLaunchIntentForPackage(getString(R.string.PACKAGE_NAME_WSP));

            //Si no existe el paquete
            if (mIntent == null) {

                packageManager.doInstallation(getString(R.string.PACKAGE_NAME_WSP), getApplicationContext());

            } else {

                Timber.i(getString(R.string.TAG_INTENT_SHARE) + ": " + getString(R.string.TAG_INTENT_SHARE_WSP));

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
                        , quakeModel.getCiudad(), mFechaLocal, quakeModel.getMagnitud(), quakeModel.getEscala(), quakeModel.getProfundidad(), quakeModel.getReferencia(),
                        getString(R.string.DEEP_LINK)
                ));
                wspIntent.putExtra(Intent.EXTRA_STREAM, mBitmapUri);
                wspIntent.setType("image/*");

                startActivity(wspIntent);
            }
        });

        mFabGM.setOnClickListener(v -> {

            Intent mIntent = getPackageManager().getLaunchIntentForPackage(getString(R.string.PACKAGE_NAME_GMAIL));

            //Si no existe el paquete
            if (mIntent == null) {

                packageManager.doInstallation(getString(R.string.PACKAGE_NAME_GMAIL), getApplicationContext());

            } else {

                Timber.i(getString(R.string.TAG_INTENT_SHARE) + ": " + getString(R.string.TAG_INTENT_SHARE_GM));

                Intent gmIntent = new Intent();
                gmIntent.setAction(Intent.ACTION_SEND);
                gmIntent.setPackage(getString(R.string.PACKAGE_NAME_GMAIL));
                gmIntent.putExtra(Intent.EXTRA_SUBJECT, String.format(Locale.US, "[Alerta " +
                        "sísmica] - %1$.1f Richter en %2$s", quakeModel.getMagnitud(), quakeModel.getCiudad()));
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
                                , mFechaLocal, quakeModel.getCiudad(), quakeModel.getMagnitud(), quakeModel.getEscala(), quakeModel.getProfundidad(),
                                quakeModel.getReferencia(), quakeModel.getLatitud(), quakeModel.getLongitud(), mDmsLat, mDmsLong,
                                getString(R.string.DEEP_LINK))));

                gmIntent.putExtra(Intent.EXTRA_STREAM, mBitmapUri);
                gmIntent.setType("image/*");

                startActivity(gmIntent);
            }
        });
    }

    private void makeSnapshot() {

        if (mGoogleMap != null) {

            //Tomar screenshot del mapa para posterior funcion de compartir
            mGoogleMap.snapshot(bitmap -> {
                try {

                    Timber.i("Snapshot google play");

                    mBitmapUri = getLocalBitmapUri(bitmap, getApplicationContext());
                } catch (IOException e) {
                    Timber.e(e, "Error screenshot map: %s", e.getMessage());
                }
            });
        }
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
        double mLatUbicacion = Double.parseDouble(latitud);
        double mLongUbicacion = Double.parseDouble(longitud);

        if (mLatUbicacion < 0) {
            mDmsLat = getString(R.string.coordenadas_sur);
        } else {
            mDmsLat = getString(R.string.coordenadas_norte);
        }

        if (mLongUbicacion < 0) {
            mDmsLong = getString(R.string.coordenadas_oeste);
        } else {
            mDmsLong = getString(R.string.coordenadas_este);
        }

        //Calculo de lat to GMS
        Map<String, Double> mMapLatDMS = latLonToDMS(mLatUbicacion);
        Double mLatGradosDMS = mMapLatDMS.get("grados");
        Double mLatMinutosDMS = mMapLatDMS.get("minutos");
        Double mLatSegundosDMS = mMapLatDMS.get("segundos");
        mDmsLat = String.format(Locale.US, "%.1f° %.1f' %.1f'' %s", mLatGradosDMS, mLatMinutosDMS, mLatSegundosDMS, mDmsLat);

        //Calculo de long to GMS
        Map<String, Double> mMapLongDMS = latLonToDMS(mLongUbicacion);
        Double mLongGradosDMS = mMapLongDMS.get("grados");
        Double mLongMinutosDMS = mMapLongDMS.get("minutos");
        Double mLongSegundosDMS = mMapLongDMS.get("segundos");
        mDmsLong = String.format(Locale.US, "%.1f° %.1f' %.1f'' %s", mLongGradosDMS, mLongMinutosDMS, mLongSegundosDMS, mDmsLong);
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

            Date mFechaLocal;
            try {
                mFechaLocal = dateManager.stringToDate(this, this.mFechaLocal);

                quakeModel.setFechaLocal(mFechaLocal);
                mTiempos = dateManager.dateToDHMS(mFechaLocal);
            } catch (ParseException e) {
                Timber.e(e, "Parse exception error: %s", e.getMessage());
            }

        }

        //Si el intent viene de notificacion
        //Convertir sFechaUtc a Date fecha_utc
        //Convertir Date fecha_utc a Date fecha_local
        //Calcular DHMS de Date fecha_local
        else {

            if (mFechaUtc != null) {

                Date mDateFechaUtc;
                try {
                    mDateFechaUtc = dateManager.stringToDate(this, mFechaUtc);
                    Date mDateFechaLocal = dateManager.utcToLocal(mDateFechaUtc);
                    quakeModel.setFechaLocal(mDateFechaLocal);
                    mTiempos = dateManager.dateToDHMS(mDateFechaLocal);

                    //Setear string que será usado en textviews de detalle con la fecha transformada
                    // de utc a local desde notificacion
                    mFechaLocal = dateManager.dateToString(this, mDateFechaLocal);

                } catch (ParseException e) {
                    Timber.e(e, "Parse exception error: %s", e.getMessage());
                }
            }
        }

    }


    /**
     * Funcion que abre el floating button menu
     */
    private void showFabMenu() {
        mIsFabOpen = true;

        //mFabFB.show();
        mFabWSP.show();
        mFabGM.show();

        //mFabTextFB = findViewById(R.id.fab_text_fb);
        mFabTextWSP = findViewById(R.id.fab_text_wsp);
        mFabTextGM = findViewById(R.id.fab_text_gm);

        //Seteado de text en alpha 0 y visible
        mFabTextWSP.setAlpha(0f);
        mFabTextWSP.setVisibility(View.VISIBLE);
        mFabTextGM.setAlpha(0f);
        mFabTextGM.setVisibility(View.VISIBLE);

        //trasnlaciones de fabs y textos
        mFabWSP.animate().translationY(-getResources().getDimension(R.dimen.standard_65));
        mFabGM.animate().translationY(-getResources().getDimension(R.dimen.standard_130));
        mFabTextWSP.animate().translationY(-getResources().getDimension(R.dimen.standard_65));
        mFabTextGM.animate().translationY(-getResources().getDimension(R.dimen.standard_130));

        //Animacion de alpha para textos
        mFabTextWSP.animate().alpha(1.0f).setDuration(500);
        mFabTextGM.animate().alpha(1.0f).setDuration(500);

        mOverlay.setAlpha(0f);

        mOverlay.animate().alpha(0.85f).setDuration(500).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                mOverlay.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        Timber.i(getString(R.string.TAG_FAB_MENU) + ": " + getString(R.string.TAG_FAB_MENU_OPEN));
    }

    /**
     * Funcion que cierra el floating button menu
     */
    private void closeFabMenu() {

        mIsFabOpen = false;
        mFabWSP.animate().translationY(0);
        mFabGM.animate().translationY(0);

        mFabGM.hide();
        mFabWSP.hide();

        mFabTextGM.animate().translationY(0);
        mFabTextGM.setVisibility(View.GONE);
        mFabTextWSP.animate().translationY(0);
        mFabTextWSP.setVisibility(View.GONE);

        //Animacion de alpha para textos
        mFabTextWSP.animate().alpha(0.0f).setDuration(500);
        mFabTextGM.animate().alpha(0.0f).setDuration(500);

        //Animacion CLOSE de mOverlay
        mOverlay.setAlpha(0.85f);
        mOverlay.animate().alpha(0.0f).setDuration(500).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mOverlay.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        Timber.i(getString(R.string.TAG_FAB_MENU) + ": " + getString(R.string.TAG_FAB_MENU_CLOSE));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Respond to the action bar's Up/Home button
        if (item.getItemId() == android.R.id.home) {

            Timber.i(getString(R.string.TAG_INTENT_DETALLE_HOME_UP_RESPONSE));

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

    /**
     * Funcion que permite setear los textview del detalle con la información procesada
     */
    private void setTextViews() {

        //Setear titulo de mCiudad en activity
        ActionBar actionBar = Objects.requireNonNull(getSupportActionBar());
        actionBar.setTitle(quakeModel.getCiudad());

        //Setear nombre mCiudad
        mTvCiudad.setText(quakeModel.getCiudad());

        //Setear mReferencia
        mTvReferencia.setText(quakeModel.getReferencia());

        //Setear mMagnitud en en circulo de color
        mTvMagnitud.setText(String.format(getString(R.string.magnitud), quakeModel.getMagnitud()));

        //Setear el color de background dependiendo de mMagnitud del sismo
        mIvMagColor.setColorFilter(getColor(viewsManager.getMagnitudeColor(quakeModel.getMagnitud(), false)));

        //Setear mProfundidad
        mTvProfundidad.setText(String.format(Locale.US, getString(R.string.quake_details_profundidad), quakeModel.getProfundidad()));

        //Setear fecha
        mTvFecha.setText(mFechaLocal);

        //Setear posicionamiento
        mTvGms.setText(String.format(getString(R.string.format_coordenadas), mDmsLat, mDmsLong));

        //SETEO DE ESTADO
        if (quakeModel.getEstado() != null) {
            viewsManager.setStatusImage(getApplicationContext(), quakeModel.getEstado(), mTvEstado, mIvEstado);
        }

        //SETEO DE HORA
        if (mTiempos != null) {
            viewsManager.setTimeToTextView(getApplicationContext(), mTiempos, mTvHora);
        }

        //SETEO DE ESCALA
        if (quakeModel.getEscala() != null) {
            viewsManager.setEscala(getApplicationContext(), quakeModel.getEscala(), mTvEscala);
        }

        //SETEO SISMO SENSIBLE
        if (quakeModel.getSensible()) {
            mIvSensible.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mGoogleMap = googleMap;

        //NIGHT MODE MAPA
        int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;

        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {

            googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getApplicationContext(), R.raw.map_night_mode));
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

        LatLng mLatLong = new LatLng(Double.parseDouble(quakeModel.getLatitud()), Double.parseDouble(quakeModel.getLongitud()));

        int mIdColor = viewsManager.getMagnitudeColor(quakeModel.getMagnitud(), true);

        //Circulo grande con color segun magnitud
        mGoogleMap.addCircle(new CircleOptions()
                .center(mLatLong)
                .radius(90000)
                .fillColor(getColor(mIdColor))
                .strokeColor(getColor(R.color.grey_dark_alpha)));

        mGoogleMap.addCircle(new CircleOptions()
                .center(mLatLong)
                .radius(3000)
                .fillColor(getColor(R.color.grey_dark_alpha))
                .strokeColor(Color.TRANSPARENT)
        );

        //Circulo gris animado
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
        animator.addUpdateListener(animation -> {

            float animatedFraction = animation.getAnimatedFraction();
            circle_anim.setRadius(animatedFraction * 140000);
        });
        animator.start();

        //Circulo gris animado
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
        animator2.addUpdateListener(animation -> {
            float animatedFraction = animation.getAnimatedFraction();
            circle_anim2.setRadius(animatedFraction * 140000);
        });
        animator2.start();

        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mLatLong, 6.0f));

        //Callback en espera de mapa completamente cargado
        mGoogleMap.setOnMapLoadedCallback(() -> {
        });
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

    /**
     * Funcion que permite cambiaar latitud o longitud a DMS
     *
     * @param input Longitud o Latitud
     * @return grados, minutos, segundos en un Map
     */
    private Map<String, Double> latLonToDMS(double input) {

        Map<String, Double> mDMS = new HashMap<>();

        double abs = Math.abs(input);

        double mLatGradosLet = Math.floor(abs); //71
        double mMinutes = Math.floor((((abs - mLatGradosLet) * 3600) / 60)); // 71.43 -71 = 0.43
        // =25.8 = 25
        //(71.43 - 71)*3600 /60 - (71.43-71)*3600/60 = 25.8 - 25 =0.8
        double mSeconds = ((((abs - mLatGradosLet) * 3600) / 60) - mMinutes) * 60;

        mDMS.put("grados", Math.floor(Math.abs(input)));
        mDMS.put("minutos", (double) Math.round(mMinutes));
        mDMS.put("segundos", (double) Math.round(mSeconds));

        return mDMS;
    }

    /**
     * Funcion encargada se guardar en directorio de celular una imagen bitmap
     *
     * @param bitmap  Bitmap de la imagen
     * @param context Contexto necesario para usar recursos
     * @return Path de la imagen
     */
    public Uri getLocalBitmapUri(Bitmap bitmap, Context context) throws IOException {

        Calendar c = Calendar.getInstance();
        c.setTime(quakeModel.getFechaLocal());
        int date = (int) c.getTimeInMillis();

        File mFile = new File(context.getCacheDir(), "share_" + quakeModel.getCiudad().toLowerCase() + "_" + date + ".jpeg");

        if (mFile.exists()) {
            Timber.i("Share image exist");
        } else {
            Timber.i("Share image not exist");

            FileOutputStream out = new FileOutputStream(mFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.close();
        }
        return FileProvider.getUriForFile(context, "cl.figonzal.lastquakechile.fileprovider", mFile);
    }
}

