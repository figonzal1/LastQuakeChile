package cl.figonzal.lastquakechile.repository;

import android.app.Application;
import android.arch.lifecycle.MutableLiveData;
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

import cl.figonzal.lastquakechile.QuakeModel;
import cl.figonzal.lastquakechile.R;
import cl.figonzal.lastquakechile.services.QuakeUtils;
import cl.figonzal.lastquakechile.services.VolleySingleton;


public class QuakeRepository {

    private static QuakeRepository intance;
    private MutableLiveData<List<QuakeModel>> quakeMutableList = new MutableLiveData<>();
    private List<QuakeModel> quakeList = new ArrayList<>();
    private Application application;
    private MutableLiveData<String> statusData = new MutableLiveData<>();

    /**
     * Funcion singleton que permite instanciar el repositorio
     *
     * @return Instancia de repositorio
     */
    public static QuakeRepository getIntance(Application application) {

        if (intance == null) {
            intance = new QuakeRepository(application);
        }
        return intance;
    }

    /**
     * Contructor que permite instanciar el contexto de la acitivity
     *
     * @param application Permite acceder a los strings
     */
    private QuakeRepository(Application application) {
        this.application = application;
    }

    /**
     * Funcion encargada de cargar la lista de sismos desde la red en sismologia.cl
     *
     * @return MutableLiveData con los sismos
     */
    public MutableLiveData<List<QuakeModel>> getQuakeList() {
        loadQuakes();
        return quakeMutableList;
    }

    public MutableLiveData<String> getStatusData() {
        return statusData;
    }

    /**
     * Funcion encargada de crear la request HTTP hacia el servidor y parsear el JSON con los sismos
     */
    private void loadQuakes() {

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, application.getString(R.string.URL_GET_PROD), null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                //Parseando la informacion desde heroku get_quakes.php
                try {

                    JSONArray jsonArray = response.getJSONArray("quakes");

                    for (int i = 0; i < jsonArray.length(); i++) {

                        JSONObject object = jsonArray.getJSONObject(i);

                        QuakeModel model = new QuakeModel();

                        SimpleDateFormat format = new SimpleDateFormat(application.getString(R.string.DATETIME_FORMAT), Locale.US);
                        format.setTimeZone(TimeZone.getDefault());


                        //OBTENER UTC DESDE PHP CONVERTIRLO A LOCAL DEL DISPOSITIVO

                        Date utc_date = format.parse(object.getString(application.getString(R.string.KEY_FECHA_UTC)));
                        Date local_date = QuakeUtils.utcToLocal(utc_date);

                        Log.d("DATETIME", "UTC: " + format.format(utc_date) + "- LOCAL: " + format.format(local_date));

                        model.setFecha_local(local_date);
                        model.setLatitud(object.getString(application.getString(R.string.KEY_LATITUD)));
                        model.setLongitud(object.getString(application.getString(R.string.KEY_LONGITUD)));
                        model.setMagnitud(object.getDouble(application.getString(R.string.KEY_MAGNITUD)));
                        model.setEscala(object.getString(application.getString(R.string.KEY_ESCALA)));
                        model.setProfundidad(object.getDouble(application.getString(R.string.KEY_PROFUNDIDAD)));
                        model.setAgencia(object.getString(application.getString(R.string.KEY_AGENCIA)));
                        model.setReferencia(object.getString(application.getString(R.string.KEY_REFERENCIA)));
                        model.setImagen_url(object.getString(application.getString(R.string.KEY_IMAGEN_URL)));
                        model.setEstado(object.getString(application.getString(R.string.KEY_ESTADO)));

                        switch (object.getInt(application.getString(R.string.KEY_SENSIBLE))) {

                            case 0:
                                model.setSensible(false);
                                break;
                            case 1:
                                model.setSensible(true);
                                break;
                        }

                        quakeList.add(model);

                    }

                    quakeMutableList.postValue(quakeList);

                } catch (JSONException e) {

                    Log.d(application.getString(R.string.TAG_JSON_GENERAL_ERROR), e.getMessage());
                    Crashlytics.log(Log.DEBUG, application.getString(R.string.TAG_JSON_GENERAL_ERROR), e.getMessage());
                } catch (ParseException e) {

                    Log.d(application.getString(R.string.TAG_JSON_PARSE_ERROR), e.getMessage());
                    Crashlytics.log(Log.DEBUG, application.getString(R.string.TAG_JSON_PARSE_ERROR), e.getMessage());
                }

                //LOGS
                Log.d(application.getString(R.string.TAG_CONNECTION_OK), application.getString(R.string.CONNECTION_OK_RESPONSE));
                Crashlytics.log(Log.DEBUG, application.getString(R.string.TAG_CONNECTION_OK), application.getString(R.string.CONNECTION_OK_RESPONSE));
                Crashlytics.setBool(application.getString(R.string.CONNECTED), true);


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                if (error instanceof TimeoutError) {
                    Log.d(application.getString(R.string.TAG_VOLLEY_ERROR), application.getString(R.string.TAG_VOLLEY_ERROR_TIMEOUT));
                    Crashlytics.log(Log.DEBUG, application.getString(R.string.TAG_VOLLEY_ERROR), application.getString(R.string.TAG_VOLLEY_ERROR_TIMEOUT));
                    //statusData.postValue(application.getString(R.string.VIEWMODEL_TIMEOUT_ERROR));

                } else if (error instanceof NoConnectionError) {
                    Log.d(application.getString(R.string.TAG_VOLLEY_ERROR), application.getString(R.string.TAG_VOLLEY_ERROR_CONNECTION));
                    Crashlytics.log(Log.DEBUG, application.getString(R.string.TAG_VOLLEY_ERROR), application.getString(R.string.TAG_VOLLEY_ERROR_CONNECTION));
                    statusData.postValue(application.getString(R.string.VIEWMODEL_NOCONNECTION_ERROR));

                } else if (error instanceof AuthFailureError) {
                    Log.d(application.getString(R.string.TAG_VOLLEY_ERROR), application.getString(R.string.TAG_VOLLEY_ERROR_AUTH));

                } else if (error instanceof ServerError) {
                    Log.d(application.getString(R.string.TAG_VOLLEY_ERROR), application.getString(R.string.TAG_VOLLEY_ERROR_SERVER));
                    Crashlytics.log(Log.DEBUG, application.getString(R.string.TAG_VOLLEY_ERROR), application.getString(R.string.TAG_VOLLEY_ERROR_SERVER));
                    statusData.postValue(application.getString(R.string.VIEWMODEL_SERVER_ERROR));

                } else if (error instanceof NetworkError) {
                    Log.d(application.getString(R.string.TAG_VOLLEY_ERROR), application.getString(R.string.TAG_VOLLEY_ERROR_NETWORK));

                } else if (error instanceof ParseError) {
                    Log.d(application.getString(R.string.TAG_VOLLEY_ERROR), application.getString(R.string.TAG_VOLLEY_ERROR_PARSE));

                }
                VolleySingleton.getInstance(application).cancelPendingRequests("TAG");

            }
        });

        jsonObjectRequest.setShouldCache(true);
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(2500, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingleton.getInstance(application).addToRequestQueue(jsonObjectRequest, "TAG");
    }
}
