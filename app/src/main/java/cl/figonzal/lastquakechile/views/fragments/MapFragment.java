package cl.figonzal.lastquakechile.views.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import cl.figonzal.lastquakechile.R;
import cl.figonzal.lastquakechile.managers.DateManager;
import cl.figonzal.lastquakechile.model.QuakeModel;
import cl.figonzal.lastquakechile.services.Utils;
import cl.figonzal.lastquakechile.viewmodel.QuakeListViewModel;
import cl.figonzal.lastquakechile.views.activities.QuakeDetailsActivity;


public class MapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.InfoWindowAdapter, GoogleMap.OnInfoWindowClickListener {

    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    private MapView mapView;
    private GoogleMap googleMap;
    private double mPromLat, mPromLong;
    private Bundle mMapViewBundle;
    private List<QuakeModel> mListQuakeModel;

    private FirebaseCrashlytics crashlytics;
    private DateManager dateManager;

    public static MapFragment newInstance() {
        return new MapFragment();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mapView.onCreate(savedInstanceState);
        mapView.onResume();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        crashlytics = FirebaseCrashlytics.getInstance();
        dateManager = new DateManager();

        /*mMapViewBundle = null;
        if (savedInstanceState != null) {
            mMapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        setRetainInstance(true);*/
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View mView = inflater.inflate(R.layout.fragment_map, container, false);

        QuakeListViewModel mQuakeListViewModel = new ViewModelProvider(requireActivity()).get(QuakeListViewModel.class);

        mapView = mView.findViewById(R.id.map);

        mQuakeListViewModel.showQuakeList().observe(requireActivity(), quakeModels -> {

            mListQuakeModel = quakeModels;
            mapView.getMapAsync(MapFragment.this);
        });

        return mView;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        this.googleMap = googleMap;
        this.googleMap.clear();

        //NIGHT MODE MAPA
        int nightModeFlags = requireContext().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;

        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {

            googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_night_mode));
        }

        //Setear info windows
        this.googleMap.setInfoWindowAdapter(MapFragment.this);

        //Setear info windows click listener
        this.googleMap.setOnInfoWindowClickListener(MapFragment.this);

        //Setear limites del mapa
        LatLngBounds mChile = new LatLngBounds(new LatLng(-55.15, -78.06), new LatLng(-15.6, -66.5));
        this.googleMap.setLatLngBoundsForCameraTarget(mChile);

        //Configuraciones de mapa
        this.googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        this.googleMap.setMinZoomPreference(5.0f);
        this.googleMap.getUiSettings().setZoomControlsEnabled(true);
        this.googleMap.getUiSettings().setTiltGesturesEnabled(true);
        this.googleMap.getUiSettings().setMapToolbarEnabled(true);
        this.googleMap.getUiSettings().setZoomGesturesEnabled(true);
        this.googleMap.getUiSettings().setRotateGesturesEnabled(false);
        this.googleMap.getUiSettings().setCompassEnabled(false);

        mPromLat = 0.0;
        mPromLong = 0.0;

        //Cargar pines y circulos
        cargarPins(Objects.requireNonNull(mListQuakeModel, "ListQuakeModel null"));

        //Calculo de promedios
        mPromLat /= mListQuakeModel.size();
        mPromLong /= mListQuakeModel.size();

        //Calcular punto central de pines y ubicar camara en posicion promedio
        LatLng mPuntoPromedio = new LatLng(mPromLat, mPromLong);
        this.googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mPuntoPromedio, 5.0f));

        //Log zone
        Log.d(getString(R.string.TAG_MAP_FRAGMENT), getString(R.string.TAG_MAP_READY_RESPONSE));
        crashlytics.log(getString(R.string.TAG_MAP_FRAGMENT) + getString(R.string.TAG_MAP_READY_RESPONSE));
    }

    /**
     * Funcion encargada de dibujar los pins y marcas circulares para cada sismos obtenido de la
     * lista.
     *
     * @param quakeModels Listado de sismos proveniente de viewModel
     */
    private void cargarPins(List<QuakeModel> quakeModels) {

        for (int i = 0; i < quakeModels.size(); i++) {

            //Obtener sismos i-esimo
            QuakeModel mModel = quakeModels.get(i);

            //Obtener clase latlong del sismo
            LatLng mLatLong = new LatLng(Double.parseDouble(mModel.getLatitud()), Double.parseDouble(mModel.getLongitud()));

            //Suma de lat y long
            mPromLat += Double.parseDouble(mModel.getLatitud());
            mPromLong += Double.parseDouble(mModel.getLongitud());

            //Buscar color
            int mIdColor = Utils.getMagnitudeColor(mModel.getMagnitud(), true);

            //Marcador de epicentro
            googleMap.addMarker(new MarkerOptions()
                    .position(mLatLong)
                    .alpha(0.9f)
            ).setTag(mModel);

            //Circulo en pin
            CircleOptions mCircleOptions = new CircleOptions()
                    .center(mLatLong)
                    .radius(10000 * mModel.getMagnitud())
                    .fillColor(requireContext().getColor(mIdColor))
                    .strokeColor(requireContext().getColor(R.color.grey_dark_alpha));

            googleMap.addCircle(mCircleOptions);
        }
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        @SuppressLint("InflateParams") View mView = getLayoutInflater().inflate(R.layout.info_windows, null);

        Object mObject = marker.getTag();
        QuakeModel mModel = (QuakeModel) mObject;

        TextView mTvMagnitud = mView.findViewById(R.id.tv_iw_magnitud);
        TextView mTvReferencia = mView.findViewById(R.id.tv_iw_referencia);
        ImageView mIvMagColor = mView.findViewById(R.id.iv_iw_mag_color);
        TextView mTvProfundidad = mView.findViewById(R.id.tv_iw_profundidad);
        TextView mTvHora = mView.findViewById(R.id.tv_iw_hora);
        TextView mTvEstado = mView.findViewById(R.id.tv_iw_estado);
        ImageView mIvEstado = mView.findViewById(R.id.iv_iw_estado);

        //SECCION ESTADO
        String mEstado = Objects.requireNonNull(mModel).getEstado();

        //Setear estado e imagen del estado (Preliminar o verificado)
        Utils.setStatusImage(getContext(), mEstado, mTvEstado, mIvEstado);

        //Setear referencia del sismo en infoWindow
        mTvReferencia.setText(mModel.getReferencia());

        //Setear magnitud del sismo dentro de circulo coloreado
        mTvMagnitud.setText(String.format(requireContext().getString(R.string.magnitud), mModel.getMagnitud()));

        //Colorear circulo según la magnitud del sismo
        mIvMagColor.setColorFilter(requireContext().getColor(Utils.getMagnitudeColor(mModel.getMagnitud(), false)));

        //Setear la profundidad del sismo
        mTvProfundidad.setText(String.format(getString(R.string.profundidad_info_windows), mModel.getProfundidad()));

        //Calcular tiempos (Dates a DHMS)
        Map<String, Long> mTiempos = dateManager.dateToDHMS(mModel.getFechaLocal());

        //Separar mapeo de tiempos en dias, horas,minutos,segundos.
        Long mDias = mTiempos.get(getString(R.string.UTILS_TIEMPO_DIAS));
        Long mMinutos = mTiempos.get(getString(R.string.UTILS_TIEMPO_MINUTOS));
        Long mHoras = mTiempos.get(getString(R.string.UTILS_TIEMPO_HORAS));
        Long mSegundos = mTiempos.get(getString(R.string.UTILS_TIEMPO_SEGUNDOS));

        //Condiciones días.
        if (mDias != null && mDias == 0) {

            if (mHoras != null && mHoras >= 1) {

                mTvHora.setText(String.format(getString(R.string.quake_time_hour_info_windows), mHoras));

            } else {

                mTvHora.setText(String.format(getString(R.string.quake_time_minute_info_windows), mMinutos));

                if (mMinutos != null && mMinutos < 1) {
                    mTvHora.setText(String.format(getString(R.string.quake_time_second_info_windows), mSegundos));
                }
            }
        } else if (mDias != null && mDias > 0) {

            if (mHoras != null && mHoras == 0) {

                mTvHora.setText(String.format(getString(R.string.quake_time_day_info_windows), mDias));

            } else if (mHoras != null && mHoras >= 1) {

                mTvHora.setText(String.format(getString(R.string.quake_time_day_hour_info_windows), mDias, mHoras / 24));
            }
        }

        //Log zone
        Log.d(getString(R.string.TAG_MAP_FRAGMENT), getString(R.string.TAG_INFO_WINDOWS_RESPONSE));
        crashlytics.log(getString(R.string.TAG_MAP_FRAGMENT) + getString(R.string.TAG_INFO_WINDOWS_RESPONSE));
        return mView;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {

        Object mObject = marker.getTag();
        QuakeModel mModel = (QuakeModel) mObject;

        Intent mIntent = new Intent(getContext(), QuakeDetailsActivity.class);
        Bundle mBundle = new Bundle();

        if (mModel != null) {

            mBundle.putString(getString(R.string.INTENT_CIUDAD), mModel.getCiudad());
            mBundle.putString(getString(R.string.INTENT_REFERENCIA), mModel.getReferencia());
            mBundle.putString(getString(R.string.INTENT_LATITUD), mModel.getLatitud());
            mBundle.putString(getString(R.string.INTENT_LONGITUD), mModel.getLongitud());

            //Cambiar la fecha local a string
            SimpleDateFormat mFormat = new SimpleDateFormat(getString(R.string.DATETIME_FORMAT),
                    Locale.US);
            String fecha_local = mFormat.format(mModel.getFechaLocal());

            mBundle.putString(getString(R.string.INTENT_FECHA_LOCAL), fecha_local);
            mBundle.putDouble(getString(R.string.INTENT_MAGNITUD), mModel.getMagnitud());
            mBundle.putDouble(getString(R.string.INTENT_PROFUNDIDAD), mModel.getProfundidad());
            mBundle.putString(getString(R.string.INTENT_ESCALA), mModel.getEscala());
            mBundle.putBoolean(getString(R.string.INTENT_SENSIBLE), mModel.getSensible());
            mBundle.putString(getString(R.string.INTENT_LINK_FOTO), mModel.getImagenUrl());
            mBundle.putString(getString(R.string.INTENT_ESTADO), mModel.getEstado());
            mIntent.putExtras(mBundle);

            Log.d(getString(R.string.TAG_INTENT), getString(R.string.TAG_INTENT_INFO_WINDOWS));
            crashlytics.log(getString(R.string.TAG_INTENT) + getString(R.string.TAG_INTENT_INFO_WINDOWS));

            startActivity(mIntent);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {

        Bundle mMapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);

        if (mMapViewBundle == null) {

            mMapViewBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mMapViewBundle);
        }

        mapView.onSaveInstanceState(mMapViewBundle);

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

}
