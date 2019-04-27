package cl.figonzal.lastquakechile.views;

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
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.crashlytics.android.Crashlytics;
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

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import cl.figonzal.lastquakechile.QuakeModel;
import cl.figonzal.lastquakechile.R;
import cl.figonzal.lastquakechile.services.QuakeUtils;
import cl.figonzal.lastquakechile.viewmodel.QuakeViewModel;


public class MapFragment extends Fragment implements OnMapReadyCallback,
		GoogleMap.InfoWindowAdapter,
		GoogleMap.OnInfoWindowClickListener {

	private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
	private MapView mMapView;
	private GoogleMap mGoogleMap;
	private double mPromLat, mPromLong;
	private Bundle mMapViewBundle;
	private List<QuakeModel> mListQuakeModel;

	public static MapFragment newInstance () {
		return new MapFragment();
	}

	@Override
	public void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mMapViewBundle = null;
		if (savedInstanceState != null) {
			mMapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
		}
		setRetainInstance(true);
	}

	@Override
	public View onCreateView (@NonNull LayoutInflater inflater, ViewGroup container,
	                          Bundle savedInstanceState) {

		View mView = inflater.inflate(R.layout.fragment_map, container, false);

		QuakeViewModel mQuakeViewModel =
				ViewModelProviders.of(Objects.requireNonNull(getActivity())).get(QuakeViewModel.class);

		mMapView = mView.findViewById(R.id.map);
		mMapView.onCreate(mMapViewBundle);


		mQuakeViewModel.showQuakeList().observe(this, new Observer<List<QuakeModel>>() {
			@Override
			public void onChanged (@Nullable final List<QuakeModel> quakeModels) {

				mListQuakeModel = quakeModels;
				mMapView.getMapAsync(MapFragment.this);
			}
		});
		return mView;
	}

	@Override
	public void onMapReady (GoogleMap googleMap) {
		mGoogleMap = googleMap;
		mGoogleMap.clear();

		//NIGHT MODE MAPA
		int nightModeFlags =
				Objects.requireNonNull(getContext()).getResources().getConfiguration().uiMode &
						Configuration.UI_MODE_NIGHT_MASK;
		if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
			googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(),
							R.raw.map_night_mode));
		}

		//Setear info windows
		mGoogleMap.setInfoWindowAdapter(MapFragment.this);

		//Setear info windows click listener
		mGoogleMap.setOnInfoWindowClickListener(MapFragment.this);

		//Setear limites del mapa
		LatLngBounds mChile = new LatLngBounds(new LatLng(-55.15, -78.06),
				new LatLng(-15.6, -66.5));
		mGoogleMap.setLatLngBoundsForCameraTarget(mChile);

		//Configuraciones de mapa
		mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
		mGoogleMap.setMinZoomPreference(5.0f);
		mGoogleMap.getUiSettings().setZoomControlsEnabled(true);
		mGoogleMap.getUiSettings().setTiltGesturesEnabled(true);
		mGoogleMap.getUiSettings().setMapToolbarEnabled(true);
		mGoogleMap.getUiSettings().setZoomGesturesEnabled(true);
		mGoogleMap.getUiSettings().setRotateGesturesEnabled(false);
		mGoogleMap.getUiSettings().setCompassEnabled(false);

		mPromLat = 0.0;
		mPromLong = 0.0;

		//Cargar pines y circulos
		cargarPins(Objects.requireNonNull(mListQuakeModel));

		//Calculo de promedios
		mPromLat /= mListQuakeModel.size();
		mPromLong /= mListQuakeModel.size();

		//Calcular punto central de pines y ubicar camara en posicion promedio
		LatLng mPuntoPromedio = new LatLng(mPromLat, mPromLong);
		//mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(punto_central, 5
		// .0f),
		//	    2000, null);
		mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mPuntoPromedio, 5.0f));

		//Log zone
		Log.d(getString(R.string.TAG_MAP_FRAGMENT),
				getString(R.string.TAG_MAP_READY_RESPONSE));
		Crashlytics.log(Log.DEBUG, getString(R.string.TAG_MAP_FRAGMENT),
				getString(R.string.TAG_MAP_READY_RESPONSE));
	}

	/**
	 * Funcion encargada de dibujar los pins y marcas circulares para cada sismos obtenido de la
	 * lista.
	 *
	 * @param quakeModels Listado de sismos proveniente de viewModel
	 */
	private void cargarPins (List<QuakeModel> quakeModels) {

		for (int i = 0; i < quakeModels.size(); i++) {

			//Obtener sismos i-esimo
			QuakeModel mModel = quakeModels.get(i);

			//Obtener clase latlong del sismo
			LatLng mLatLong = new LatLng(Double.parseDouble(mModel.getLatitud()),
					Double.parseDouble(mModel.getLongitud()));

			//Suma de lat y long
			mPromLat += Double.parseDouble(mModel.getLatitud());
			mPromLong += Double.parseDouble(mModel.getLongitud());

			//Buscar color
			int mIdColor = QuakeUtils.getMagnitudeColor(mModel.getMagnitud(), true);

			//Marcador de epicentro
			mGoogleMap.addMarker(new MarkerOptions()
					.position(mLatLong)
					.alpha(0.9f)
			).setTag(mModel);

			//Circulo en pin
			CircleOptions mCircleOptions = new CircleOptions()
					.center(mLatLong)
					.radius(10000 * mModel.getMagnitud())
					.fillColor(Objects.requireNonNull(getContext()).getColor(mIdColor))
					.strokeColor(getContext().getColor(R.color.grey_dark_alpha));
			mGoogleMap.addCircle(mCircleOptions);
		}
	}

	@Override
	public View getInfoWindow (Marker marker) {
		return null;
	}

	@Override
	public View getInfoContents (Marker marker) {
		View mView = getLayoutInflater().inflate(R.layout.info_windows, null);

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
		QuakeUtils.setStatusImage(getContext(), mEstado, mTvEstado,
				mIvEstado);

		//Setear referencia del sismo en infoWindow
		mTvReferencia.setText(mModel.getReferencia());

		//Setear magnitud del sismo dentro de circulo coloreado
		mTvMagnitud.setText(String.format(Objects.requireNonNull(getContext()).getString(R.string.magnitud), mModel.getMagnitud()));

		//Colorear circulo según la magnitud del sismo
		mIvMagColor.setColorFilter(getContext().getColor(QuakeUtils.getMagnitudeColor(mModel.getMagnitud(), false)));

		//Setear la profundidad del sismo
		mTvProfundidad.setText(String.format(getString(R.string.profundidad_info_windows),
				mModel.getProfundidad()));

		//Calcular tiempos (Dates a DHMS)
		Map<String, Long> mTiempos =
				QuakeUtils.dateToDHMS(mModel.getFechaLocal());

		//Separar mapeo de tiempos en dias, horas,minutos,segundos.
		Long mDias = mTiempos.get(getString(R.string.UTILS_TIEMPO_DIAS));
		Long mMinutos =
				mTiempos.get(getString(R.string.UTILS_TIEMPO_MINUTOS));
		Long mHoras = mTiempos.get(getString(R.string.UTILS_TIEMPO_HORAS));
		Long mSegundos =
				mTiempos.get(getString(R.string.UTILS_TIEMPO_SEGUNDOS));

		//Condiciones días.
		if (mDias != null && mDias == 0) {

			if (mHoras != null && mHoras >= 1) {
				mTvHora.setText(String.format(getString(R.string.quake_time_hour_info_windows),
						mHoras));
			} else {
				mTvHora.setText(String.format(getString(R.string.quake_time_minute_info_windows),
						mMinutos));

				if (mMinutos != null && mMinutos < 1) {
					mTvHora.setText(String.format(getString(R.string.quake_time_second_info_windows),
							mSegundos));
				}
			}
		} else if (mDias != null && mDias > 0) {

			if (mHoras != null && mHoras == 0) {
				mTvHora.setText(String.format(getString(R.string.quake_time_day_info_windows),
						mDias));
			} else if (mHoras != null && mHoras >= 1) {
				mTvHora.setText(String.format(getString(R.string.quake_time_day_hour_info_windows),
						mDias, mHoras / 24));
			}
		}

		//Log zone
		Log.d(getString(R.string.TAG_MAP_FRAGMENT), getString(R.string.TAG_INFO_WINDOWS_RESPONSE));
		Crashlytics.log(Log.DEBUG, getString(R.string.TAG_MAP_FRAGMENT),
				getString(R.string.TAG_INFO_WINDOWS_RESPONSE));
		return mView;
	}

	@Override
	public void onInfoWindowClick (Marker marker) {

		Object mObject = marker.getTag();
		QuakeModel mModel = (QuakeModel) mObject;

		Intent mIntent = new Intent(getContext(), QuakeDetailsActivity.class);
		Bundle mBundle = new Bundle();

		Log.d("ENTRE", "AQUI");
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
			Crashlytics.log(Log.DEBUG, getString(R.string.TAG_INTENT),
					getString(R.string.TAG_INTENT_INFO_WINDOWS));

			startActivity(mIntent);
		}
	}

	@Override
	public void onSaveInstanceState (@NonNull Bundle outState) {

		Bundle mMapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
		if (mMapViewBundle == null) {
			mMapViewBundle = new Bundle();
			outState.putBundle(MAPVIEW_BUNDLE_KEY, mMapViewBundle);
		}

		mMapView.onSaveInstanceState(mMapViewBundle);

		super.onSaveInstanceState(outState);
	}

	@Override
	public void onResume () {
		super.onResume();
		mMapView.onResume();

	}

	@Override
	public void onPause () {
		super.onPause();
		mMapView.onPause();
	}

	@Override
	public void onDestroy () {
		super.onDestroy();
		mMapView.onDestroy();
	}

	@Override
	public void onLowMemory () {
		super.onLowMemory();
		mMapView.onLowMemory();
	}

}
