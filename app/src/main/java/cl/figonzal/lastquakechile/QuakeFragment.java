package cl.figonzal.lastquakechile;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class QuakeFragment extends Fragment {

    private List<QuakeModel> quakeModelList;

    public QuakeFragment() {
        // Required empty public constructor
    }

    public static QuakeFragment newInstance() {
        return new QuakeFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        quakeModelList = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_quake,container,false);

        //Setear el recycle view
        RecyclerView rv = v.findViewById(R.id.recycle_view);
        rv.setHasFixedSize(true);

        //Setear el layout de la lista
        LinearLayoutManager ly = new LinearLayoutManager(getContext());
        rv.setLayoutManager(ly);



        //Setear el adapter con la lista de quakes
        QuakeAdapter adapter = new QuakeAdapter(quakeModelList);
        rv.setAdapter(adapter);

        //Cargar Sismos
        cargarSismos();

        return v;
    }

    /*
        Funcion que devuelve el listado de sismos.
     */
    private void cargarSismos() {

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, getString(R.string.URL_GET), null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                //Parseando la infromacion desde heroku get_quakes.php

                try {
                    //TODO: Pasar a string el quakes
                    JSONArray jsonArray = response.getJSONArray("quakes");

                    for (int i = 0; i < jsonArray.length(); i++) {

                        JSONObject object = jsonArray.getJSONObject(i);

                        QuakeModel model = new QuakeModel();

                        SimpleDateFormat format = new SimpleDateFormat(getString(R.string.DATETIME_FORMAT), Locale.US);
                        Date local_date = format.parse(object.getString(getString(R.string.KEY_FECHA_LOCAL)));
                        Date utc_date = format.parse(object.getString(getString(R.string.KEY_FECHA_UTC)));

                        model.setFecha_local(local_date);
                        model.setFecha_utc(utc_date);
                        model.setLatitud(object.getString(getString(R.string.KEY_LATITUD)));
                        model.setLongitud(object.getString(getString(R.string.KEY_LONGITUD)));
                        model.setMagnitud(object.getDouble(getString(R.string.KEY_MAGNITUD)));
                        model.setAgencia(object.getString(getString(R.string.KEY_AGENCIA)));
                        model.setReferencia(object.getString(getString(R.string.KEY_REFERENCIA)));
                        model.setImagen_url(object.getString(getString(R.string.KEY_IMAGEN_URL)));

                        quakeModelList.add(model);
                    }

                } catch (JSONException e) {
                    Log.d("JSON_ERROR",e.getMessage());
                } catch (ParseException e) {
                    Log.d("JSON_PARSE_ERROR",e.getMessage());
                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Log.d("VOLLEY_RESPONSE", error.getMessage());
            }
        });

        jsonObjectRequest.setShouldCache(false);
        VolleySingleton.getInstance(getContext()).addToRequestQueue(jsonObjectRequest);
    }
}
