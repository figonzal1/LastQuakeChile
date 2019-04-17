package cl.figonzal.lastquakechile.views;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
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

	private MapView mapView;
	private GoogleMap gMap;
	private double prom_lat, prom_long;
	private Bundle mapViewBundle;
	private List<QuakeModel> quakeModelList;

	private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";

	public static MapFragment newInstance () {
		return new MapFragment();
	}

	@Override
	public void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mapViewBundle = null;
		if (savedInstanceState != null) {
			mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
		}
		setRetainInstance(true);
	}

	@Override
	public View onCreateView (@NonNull LayoutInflater inflater, ViewGroup container,
	                          Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.fragment_map, container, false);

		QuakeViewModel viewModel =
				ViewModelProviders.of(Objects.requireNonNull(getActivity())).get(QuakeViewModel.class);

		mapView = v.findViewById(R.id.map);
		mapView.onCreate(mapViewBundle);


		viewModel.showQuakeList().observe(this, new Observer<List<QuakeModel>>() {
			@Override
			public void onChanged (@Nullable final List<QuakeModel> quakeModels) {

				quakeModelList = quakeModels;
				mapView.getMapAsync(MapFragment.this);
			}
		});
		return v;
	}

	@Override
	public void onMapReady (GoogleMap googleMap) {
		gMap = googleMap;
		gMap.clear();

		//Setear info windows
		gMap.setInfoWindowAdapter(MapFragment.this);

		//Setear info windows click listener
		gMap.setOnInfoWindowClickListener(MapFragment.this);

		//Setear limites del mapa
		LatLngBounds CHILE = new LatLngBounds(new LatLng(-55.15, -78.06),
				new LatLng(-15.6, -66.5));
		gMap.setLatLngBoundsForCameraTarget(CHILE);

		//Configuraciones de mapa
		gMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
		gMap.setMinZoomPreference(5.0f);
		gMap.getUiSettings().setZoomControlsEnabled(true);
		gMap.getUiSettings().setTiltGesturesEnabled(true);
		gMap.getUiSettings().setMapToolbarEnabled(true);
		gMap.getUiSettings().setZoomGesturesEnabled(true);
		gMap.getUiSettings().setRotateGesturesEnabled(false);
		gMap.getUiSettings().setCompassEnabled(false);

		prom_lat = 0.0;
		prom_long = 0.0;

		//Cargar pines y circulos
		cargarPins(Objects.requireNonNull(quakeModelList));

		//Calculo de promedios
		prom_lat /= quakeModelList.size();
		prom_long /= quakeModelList.size();

		//Calcular punto central de pines y ubicar camara en posicion promedio
		LatLng punto_central = new LatLng(prom_lat, prom_long);
		//gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(punto_central, 5
		// .0f),
		//	    2000, null);
		gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(punto_central, 5.0f));

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
			QuakeModel model = quakeModels.get(i);

			//Obtener clase latlong del sismo
			LatLng latLng = new LatLng(Double.parseDouble(model.getLatitud()),
					Double.parseDouble(model.getLongitud()));

			//Suma de lat y long
			prom_lat += Double.parseDouble(model.getLatitud());
			prom_long += Double.parseDouble(model.getLongitud());

			//Buscar color
			int id_color = QuakeUtils.getMagnitudeColor(model.getMagnitud(), true);

			//Marcador de epicentro
			gMap.addMarker(new MarkerOptions()
					.position(latLng)
					.alpha(0.9f)
			).setTag(model);

			//Circulo en pin
			CircleOptions circleOptions = new CircleOptions()
					.center(latLng)
					.radius(10000 * model.getMagnitud())
					.fillColor(Objects.requireNonNull(getContext()).getColor(id_color))
					.strokeColor(getContext().getColor(R.color.grey_dark_alpha));
			gMap.addCircle(circleOptions);
		}
	}

	@Override
	public View getInfoWindow (Marker marker) {
		return null;
	}

	@Override
	public View getInfoContents (Marker marker) {
		View v = getLayoutInflater().inflate(R.layout.info_windows, null);

		Object object = marker.getTag();
		QuakeModel model = (QuakeModel) object;

		TextView tv_magnitud = v.findViewById(R.id.tv_iw_magnitud);
		TextView tv_referencia = v.findViewById(R.id.tv_iw_referencia);
		ImageView iv_mag_color = v.findViewById(R.id.iv_iw_mag_color);
		TextView tv_profundidad = v.findViewById(R.id.tv_iw_profundidad);
		TextView tv_hora = v.findViewById(R.id.tv_iw_hora);
		TextView tv_estado = v.findViewById(R.id.tv_iw_estado);
		ImageView iv_estado = v.findViewById(R.id.iv_iw_estado);

		//SECCION ESTADO
		String estado = Objects.requireNonNull(model).getEstado();

		//Setear estado e imagen del estado (Preliminar o verificado)
		QuakeUtils.setStatusImage(getContext(), estado, tv_estado,
				iv_estado);

		//Setear referencia del sismo en infoWindow
		tv_referencia.setText(model.getReferencia());

		//Setear magnitud del sismo dentro de circulo coloreado
		tv_magnitud.setText(String.format(Objects.requireNonNull(getContext()).getString(R.string.magnitud), model.getMagnitud()));

		//Colorear circulo según la magnitud del sismo
		iv_mag_color.setColorFilter(getContext().getColor(QuakeUtils.getMagnitudeColor(model.getMagnitud(), false)));

		//Setear la profundidad del sismo
		tv_profundidad.setText(String.format(getString(R.string.profundidad_info_windows),
				model.getProfundidad()));

		//Calcular tiempos (Dates a DHMS)
		Map<String, Long> tiempos =
				QuakeUtils.dateToDHMS(model.getFecha_local());

		//Separar mapeo de tiempos en dias, horas,minutos,segundos.
		Long dias = tiempos.get(getString(R.string.UTILS_TIEMPO_DIAS));
		Long minutos =
				tiempos.get(getString(R.string.UTILS_TIEMPO_MINUTOS));
		Long horas = tiempos.get(getString(R.string.UTILS_TIEMPO_HORAS));
		Long segundos =
				tiempos.get(getString(R.string.UTILS_TIEMPO_SEGUNDOS));

		//Condiciones días.
		if (dias != null && dias == 0) {

			if (horas != null && horas >= 1) {
				tv_hora.setText(String.format(getString(R.string.quake_time_hour_info_windows),
						horas));
			} else {
				tv_hora.setText(String.format(getString(R.string.quake_time_minute_info_windows),
						minutos));

				if (minutos != null && minutos < 1) {
					tv_hora.setText(String.format(getString(R.string.quake_time_second_info_windows),
							segundos));
				}
			}
		} else if (dias != null && dias > 0) {

			if (horas != null && horas == 0) {
				tv_hora.setText(String.format(getString(R.string.quake_time_day_info_windows),
						dias));
			} else if (horas != null && horas >= 1) {
				tv_hora.setText(String.format(getString(R.string.quake_time_day_hour_info_windows),
						dias, horas / 24));
			}
		}

		//Log zone
		Log.d(getString(R.string.TAG_MAP_FRAGMENT), getString(R.string.TAG_INFO_WINDOWS_RESPONSE));
		Crashlytics.log(Log.DEBUG, getString(R.string.TAG_MAP_FRAGMENT),
				getString(R.string.TAG_INFO_WINDOWS_RESPONSE));
		return v;
	}

	@Override
	public void onInfoWindowClick (Marker marker) {

		Object object = marker.getTag();
		QuakeModel model = (QuakeModel) object;

		Intent intent = new Intent(getContext(), QuakeDetailsActivity.class);
		Bundle b = new Bundle();

		Log.d("ENTRE", "AQUI");
		if (model != null) {
			b.putString(getString(R.string.INTENT_CIUDAD), model.getCiudad());
			b.putString(getString(R.string.INTENT_REFERENCIA), model.getReferencia());
			b.putString(getString(R.string.INTENT_LATITUD), model.getLatitud());
			b.putString(getString(R.string.INTENT_LONGITUD), model.getLongitud());

			//Cambiar la fecha local a string
			SimpleDateFormat format = new SimpleDateFormat(getString(R.string.DATETIME_FORMAT),
					Locale.US);
			String fecha_local = format.format(model.getFecha_local());
			b.putString(getString(R.string.INTENT_FECHA_LOCAL), fecha_local);

			b.putDouble(getString(R.string.INTENT_MAGNITUD), model.getMagnitud());
			b.putDouble(getString(R.string.INTENT_PROFUNDIDAD), model.getProfundidad());
			b.putString(getString(R.string.INTENT_ESCALA), model.getEscala());
			b.putBoolean(getString(R.string.INTENT_SENSIBLE), model.getSensible());
			b.putString(getString(R.string.INTENT_LINK_FOTO), model.getImagen_url());
			b.putString(getString(R.string.INTENT_ESTADO), model.getEstado());

			intent.putExtras(b);

			Log.d(getString(R.string.TAG_INTENT), getString(R.string.TAG_INTENT_INFO_WINDOWS));
			Crashlytics.log(Log.DEBUG, getString(R.string.TAG_INTENT),
					getString(R.string.TAG_INTENT_INFO_WINDOWS));

			startActivity(intent);
		}
	}
	@Override
	public void onSaveInstanceState (@NonNull Bundle outState) {

		Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
		if (mapViewBundle == null) {
			mapViewBundle = new Bundle();
			outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
		}

		mapView.onSaveInstanceState(mapViewBundle);

		super.onSaveInstanceState(outState);
	}

	@Override
	public void onResume () {
		super.onResume();
		mapView.onResume();

	}

	@Override
	public void onPause () {
		super.onPause();
		mapView.onPause();
	}

	@Override
	public void onDestroy () {
		super.onDestroy();
		mapView.onDestroy();
	}

	@Override
	public void onLowMemory () {
		super.onLowMemory();
		mapView.onLowMemory();
	}

}
