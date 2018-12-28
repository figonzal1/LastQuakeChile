package cl.figonzal.lastquakechile;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
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


/**
 * Clase ideada para cargar UI bajo el cambio de orientacion de pantalla
 * independiente de la creacion de una nueva activity
 */
public class QuakeViewModel extends AndroidViewModel {

    private MutableLiveData<List<QuakeModel>> liveDataQuakes;
    private List<QuakeModel> quakeModelList;

    //Mutable Live data muestra dos veces el aviso cuando hay rotacion
    //SingleLive event solo permite un evento en el fragment e ignora cualquier otro
    private MutableLiveData<String> statusData;

    //Contructor para usar context dentro de la clase ViewModel
    QuakeViewModel(@NonNull Application application) {
        super(application);
    }

    /**
     * Funcion Singleton encargada de crear un MutableLiveData con una lista de sismos, para posterior uso con observable
     *
     * @return retorna un mutablelivedata de listado de sismos
     */
    MutableLiveData<List<QuakeModel>> getQuakeList() {

        if (liveDataQuakes == null) {
            liveDataQuakes = new MutableLiveData<>();
            loadQuakes();

        }
        return liveDataQuakes;
    }

    void refreshQuakeList() {
        if (liveDataQuakes != null) {
            loadQuakes();
        }
    }

    MutableLiveData<String> getStatusData() {

        if (statusData == null) {
            statusData = new MutableLiveData<>();
        }
        return statusData;
    }

    /**
     * Funcion encargada de crear la request HTTP hacia el servidor y parsear el JSON con los sismos
     */
    private void loadQuakes() {


        //Codigo que retorna la data desde internet
        quakeModelList = new ArrayList<>();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, getApplication().getString(R.string.URL_GET), null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                //Parseando la informacion desde heroku get_quakes.php
                try {

                    JSONArray jsonArray = response.getJSONArray("quakes");

                    for (int i = 0; i < jsonArray.length(); i++) {

                        JSONObject object = jsonArray.getJSONObject(i);

                        QuakeModel model = new QuakeModel();

                        SimpleDateFormat format = new SimpleDateFormat(getApplication().getString(R.string.DATETIME_FORMAT), Locale.US);
                        Date local_date = format.parse(object.getString(getApplication().getString(R.string.KEY_FECHA_LOCAL)));
                        Date utc_date = format.parse(object.getString(getApplication().getString(R.string.KEY_FECHA_UTC)));

                        model.setFecha_local(local_date);
                        model.setFecha_utc(utc_date);
                        model.setLatitud(object.getString(getApplication().getString(R.string.KEY_LATITUD)));
                        model.setLongitud(object.getString(getApplication().getString(R.string.KEY_LONGITUD)));
                        model.setMagnitud(object.getDouble(getApplication().getString(R.string.KEY_MAGNITUD)));
                        model.setAgencia(object.getString(getApplication().getString(R.string.KEY_AGENCIA)));
                        model.setReferencia(object.getString(getApplication().getString(R.string.KEY_REFERENCIA)));
                        model.setImagen_url(object.getString(getApplication().getString(R.string.KEY_IMAGEN_URL)));

                        quakeModelList.add(model);

                    }

                    liveDataQuakes.postValue(quakeModelList);

                } catch (JSONException e) {
                    Log.d("JSON_GENERAL_ERROR", e.getMessage());
                } catch (ParseException e) {
                    Log.d("JSON_PARSE_ERROR", e.getMessage());
                }

                Log.d("CONNECTION_OK", "Conexion correcta");


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                if (error instanceof TimeoutError) {
                    Log.d(getApplication().getString(R.string.TAG_VOLLEY_ERROR), getApplication().getString(R.string.TAG_VOLLEY_ERROR_TIMEOUT));
                    statusData.postValue("Servidor no responde. Intente más tarde");

                } else if (error instanceof NoConnectionError) {
                    Log.d(getApplication().getString(R.string.TAG_VOLLEY_ERROR), getApplication().getString(R.string.TAG_VOLLEY_ERROR_CONNECTION));

                } else if (error instanceof AuthFailureError) {
                    Log.d(getApplication().getString(R.string.TAG_VOLLEY_ERROR), getApplication().getString(R.string.TAG_VOLLEY_ERROR_AUTH));

                } else if (error instanceof ServerError) {
                    Log.d(getApplication().getString(R.string.TAG_VOLLEY_ERROR), getApplication().getString(R.string.TAG_VOLLEY_ERROR_SERVER));

                } else if (error instanceof NetworkError) {
                    Log.d(getApplication().getString(R.string.TAG_VOLLEY_ERROR), getApplication().getString(R.string.TAG_VOLLEY_ERROR_NETWORK));

                } else if (error instanceof ParseError) {
                    Log.d(getApplication().getString(R.string.TAG_VOLLEY_ERROR), getApplication().getString(R.string.TAG_VOLLEY_ERROR_PARSE));

                }
                VolleySingleton.getInstance(getApplication()).cancelRequestQueue("DATA");

            }
        });

        jsonObjectRequest.setShouldCache(false);
        jsonObjectRequest.setTag("DATA");
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(3000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingleton.getInstance(getApplication()).addToRequestQueue(jsonObjectRequest);
    }


    @Override
    protected void onCleared() {
        super.onCleared();
    }
}
