package cl.figonzal.lastquakechile.views;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;
import java.util.Objects;

import cl.figonzal.lastquakechile.QuakeModel;
import cl.figonzal.lastquakechile.R;
import cl.figonzal.lastquakechile.viewmodel.QuakeViewModel;


public class MapFragment extends Fragment implements OnMapReadyCallback {

    private MapView mapView;
    private GoogleMap gMap;
    private List<QuakeModel> quakeModelsList;
    private double prom_lat, prom_long;

    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";

    public static MapFragment newInstance() {
        return new MapFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_map, container, false);

        mapView = v.findViewById(R.id.map);

        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        mapView.onCreate(mapViewBundle);
        mapView.getMapAsync(this);

        QuakeViewModel viewModel = ViewModelProviders.of(Objects.requireNonNull(getActivity())).get(QuakeViewModel.class);
        quakeModelsList = viewModel.getDirectQuakeList();

        return v;
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

        gMap = googleMap;
        //Setear limites del mapa
        LatLngBounds CHILE = new LatLngBounds(new LatLng(-55.15, -78.06), new LatLng(-15.6, -66.5));
        gMap.setLatLngBoundsForCameraTarget(CHILE);

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

        cargarDatos(gMap);

        //Calculo de promedios
        prom_lat /= quakeModelsList.size();
        prom_long /= quakeModelsList.size();

        LatLng punto_central = new LatLng(prom_lat, prom_long);
        gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(punto_central, 5.0f), 2000, null);
    }

    private void cargarDatos(GoogleMap googleMap) {
        for (int i = 0; i < quakeModelsList.size(); i++) {

            QuakeModel model = quakeModelsList.get(i);
            LatLng latLng = new LatLng(Double.parseDouble(model.getLatitud()), Double.parseDouble(model.getLongitud()));

            //Suma de lat y long
            prom_lat += Double.parseDouble(model.getLatitud());
            prom_long += Double.parseDouble(model.getLongitud());

            //Marcador de epicentro
            googleMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title(String.valueOf(model.getMagnitud()))
                    .alpha(0.7f)
            );

            //Circulo en pin
            CircleOptions circleOptions = new CircleOptions()
                    .center(latLng)
                    .radius(10000 * model.getMagnitud());
            googleMap.addCircle(circleOptions);
            Log.d("SISMO", model.getCiudad());

        }
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

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
        }

        mapView.onSaveInstanceState(mapViewBundle);
    }
}