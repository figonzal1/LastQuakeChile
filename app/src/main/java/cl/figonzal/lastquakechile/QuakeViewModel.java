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
import com.crashlytics.android.Crashlytics;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;


/**
 * Clase ideada para cargar UI bajo el cambio de orientacion de pantalla
 * independiente de la creacion de una nueva activity
 */
public class QuakeViewModel extends AndroidViewModel {

    private MutableLiveData<List<QuakeModel>> liveDataQuakes;   //Permite la carga de sismos al inicio y al refresh del toolbar
    private List<QuakeModel> quakeModelList;                    //Lista de sismos que se agrega despues al MutableLive

    //Mutable Live data muestra dos veces el aviso cuando hay rotacion
    //SingleLive event solo permite un evento en el fragment e ignora cualquier otro
    private MutableLiveData<String> statusData;

    //Contructor para usar context dentro de la clase ViewModel
    public QuakeViewModel(@NonNull Application application) {
        super(application);
    }

    /**
     * Funcion Singleton encargada de crear un MutableLiveData con una lista de sismos, para posterior uso con observable
     *
     * @return retorna un mutablelivedata de listado de sismos
     */
    public MutableLiveData<List<QuakeModel>> getMutableQuakeList() {

        if (liveDataQuakes == null) {
            liveDataQuakes = new MutableLiveData<>();
            loadQuakes();

        }
        return liveDataQuakes;
    }
    /**
     * La funcion fuerza el refresh de los datos del mutable
     */
    public void refreshMutableQuakeList() {
        if (liveDataQuakes != null) {
            loadQuakes();
        }
    }

    /**
     * Funcion que permite enviar un mensaje de estado cuando hay error de servidor
     *
     * @return Retorna el MutableLiveData del mensaje estado
     */
    public MutableLiveData<String> getStatusData() {

        if (statusData == null) {
            statusData = new MutableLiveData<>();
        }
        return statusData;
    }

    /**
     * Funcion que realiza la busqueda sobre quakeModelList con el Parametro ortorgado
     *
     * @param s Texto que ingresa el usuario en la busqueda
     * @return Lista Filtrada
     */
    public List<QuakeModel> doSearch(String s) {

        //Lista utilizada para el searchView
        List<QuakeModel> filteredList = new ArrayList<>();
        for (QuakeModel l : quakeModelList) {

            //Filtrar por lugar de referencia
            if (l.getReferencia().toLowerCase().contains(s)) {
                filteredList.add(l);
            }

            //Filtrar por magnitud de sismo
            if (l.getMagnitud().toString().contains(s)) {
                filteredList.add(l);
            }
        }
        return filteredList;
    }

    /**
     * Funcion encargada de setear la nueva lista sobre el antiguo MutableLiveData
     *
     * @param filteredList con sismos filtrados
     */
    public void setFilteredList(List<QuakeModel> filteredList) {

        if (liveDataQuakes != null) {
            liveDataQuakes.postValue(filteredList);
        }

        //livedataFilteredQuakes = new MutableLiveData<>();
        //livedataFilteredQuakes.postValue(filteredList);
    }
    /**
     * Funcion encargada de crear la request HTTP hacia el servidor y parsear el JSON con los sismos
     */
    private void loadQuakes() {


        //Codigo que retorna la data desde internet
        quakeModelList = new ArrayList<>();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, getApplication().getString(R.string.URL_GET_PROD), null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                //Parseando la informacion desde heroku get_quakes.php
                try {

                    JSONArray jsonArray = response.getJSONArray("quakes");

                    for (int i = 0; i < jsonArray.length(); i++) {

                        JSONObject object = jsonArray.getJSONObject(i);

                        QuakeModel model = new QuakeModel();

                        SimpleDateFormat format = new SimpleDateFormat(getApplication().getString(R.string.DATETIME_FORMAT), Locale.US);
                        format.setTimeZone(TimeZone.getDefault());


                        //OBTENER UTC DESDE PHP CONVERTIRLO A LOCAL DEL DISPOSITIVO

                        Date utc_date = format.parse(object.getString(getApplication().getString(R.string.KEY_FECHA_UTC)));
                        Date local_date = QuakeUtils.utcToLocal(utc_date);

                        Log.d("DATETIME", "UTC: " + format.format(utc_date) + "- LOCAL: " + format.format(local_date));

                        model.setFecha_local(local_date);
                        model.setLatitud(object.getString(getApplication().getString(R.string.KEY_LATITUD)));
                        model.setLongitud(object.getString(getApplication().getString(R.string.KEY_LONGITUD)));
                        model.setMagnitud(object.getDouble(getApplication().getString(R.string.KEY_MAGNITUD)));
                        model.setEscala(object.getString(getApplication().getString(R.string.KEY_ESCALA)));
                        model.setProfundidad(object.getDouble(getApplication().getString(R.string.KEY_PROFUNDIDAD)));
                        model.setAgencia(object.getString(getApplication().getString(R.string.KEY_AGENCIA)));
                        model.setReferencia(object.getString(getApplication().getString(R.string.KEY_REFERENCIA)));
                        model.setImagen_url(object.getString(getApplication().getString(R.string.KEY_IMAGEN_URL)));
                        model.setEstado(object.getString(getApplication().getString(R.string.KEY_ESTADO)));

                        switch (object.getInt(getApplication().getString(R.string.KEY_SENSIBLE))) {

                            case 0:
                                model.setSensible(false);
                                break;
                            case 1:
                                model.setSensible(true);
                                break;
                        }

                        quakeModelList.add(model);

                    }

                    liveDataQuakes.postValue(quakeModelList);

                } catch (JSONException e) {

                    Log.d(getApplication().getString(R.string.TAG_JSON_GENERAL_ERROR), e.getMessage());
                    Crashlytics.log(Log.DEBUG, getApplication().getString(R.string.TAG_JSON_GENERAL_ERROR), e.getMessage());
                } catch (ParseException e) {

                    Log.d(getApplication().getString(R.string.TAG_JSON_PARSE_ERROR), e.getMessage());
                    Crashlytics.log(Log.DEBUG, getApplication().getString(R.string.TAG_JSON_PARSE_ERROR), e.getMessage());
                }

                //LOGS
                Log.d(getApplication().getString(R.string.TAG_CONNECTION_OK), getApplication().getString(R.string.CONNECTION_OK_RESPONSE));
                Crashlytics.log(Log.DEBUG, getApplication().getString(R.string.TAG_CONNECTION_OK), getApplication().getString(R.string.CONNECTION_OK_RESPONSE));
                Crashlytics.setBool(getApplication().getString(R.string.CONNECTED), true);


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                if (error instanceof TimeoutError) {
                    Log.d(getApplication().getString(R.string.TAG_VOLLEY_ERROR), getApplication().getString(R.string.TAG_VOLLEY_ERROR_TIMEOUT));
                    Crashlytics.log(Log.DEBUG, getApplication().getString(R.string.TAG_VOLLEY_ERROR), getApplication().getString(R.string.TAG_VOLLEY_ERROR_TIMEOUT));
                    statusData.postValue(getApplication().getString(R.string.VIEWMODEL_TIMEOUT_ERROR));

                } else if (error instanceof NoConnectionError) {
                    Log.d(getApplication().getString(R.string.TAG_VOLLEY_ERROR), getApplication().getString(R.string.TAG_VOLLEY_ERROR_CONNECTION));
                    Crashlytics.log(Log.DEBUG, getApplication().getString(R.string.TAG_VOLLEY_ERROR), getApplication().getString(R.string.TAG_VOLLEY_ERROR_CONNECTION));
                    statusData.postValue(getApplication().getString(R.string.VIEWMODEL_NOCONNECTION_ERROR));

                } else if (error instanceof AuthFailureError) {
                    Log.d(getApplication().getString(R.string.TAG_VOLLEY_ERROR), getApplication().getString(R.string.TAG_VOLLEY_ERROR_AUTH));

                } else if (error instanceof ServerError) {
                    Log.d(getApplication().getString(R.string.TAG_VOLLEY_ERROR), getApplication().getString(R.string.TAG_VOLLEY_ERROR_SERVER));
                    Crashlytics.log(Log.DEBUG, getApplication().getString(R.string.TAG_VOLLEY_ERROR), getApplication().getString(R.string.TAG_VOLLEY_ERROR_SERVER));
                    statusData.postValue(getApplication().getString(R.string.VIEWMODEL_SERVER_ERROR));

                } else if (error instanceof NetworkError) {
                    Log.d(getApplication().getString(R.string.TAG_VOLLEY_ERROR), getApplication().getString(R.string.TAG_VOLLEY_ERROR_NETWORK));

                } else if (error instanceof ParseError) {
                    Log.d(getApplication().getString(R.string.TAG_VOLLEY_ERROR), getApplication().getString(R.string.TAG_VOLLEY_ERROR_PARSE));

                }
                VolleySingleton.getInstance(getApplication()).cancelPendingRequests("TAG");

            }
        });

        jsonObjectRequest.setShouldCache(true);
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(2500, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingleton.getInstance(getApplication()).addToRequestQueue(jsonObjectRequest, "TAG");
    }


    @Override
    protected void onCleared() {
        super.onCleared();
    }
}
