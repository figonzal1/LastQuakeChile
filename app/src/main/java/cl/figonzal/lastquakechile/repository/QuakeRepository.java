package cl.figonzal.lastquakechile.repository;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
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
import java.util.Objects;
import java.util.TimeZone;

import cl.figonzal.lastquakechile.R;
import cl.figonzal.lastquakechile.model.QuakeModel;
import cl.figonzal.lastquakechile.services.Utils;
import cl.figonzal.lastquakechile.services.VolleySingleton;


public class QuakeRepository {

    private static final String TAG_GET_REPORTS = "ListadoReportes";
    private static final String TAG_GET_QUAKES = "ListadoSismos";
    private static QuakeRepository instance;
    private final Application mApplication;

    //SISMOS
    private List<QuakeModel> mQuakeList = new ArrayList<>();
    private final MutableLiveData<List<QuakeModel>> mQuakeMutableList = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoadingQuake = new MutableLiveData<>();

    private final MutableLiveData<String> responseMsgErrorList = new MutableLiveData<>();

    private boolean volleyError = false;
    private int contador_request = 0;

    private QuakeRepository(Application application) {
        this.mApplication = application;
    }

    public static QuakeRepository getIntance(Application application) {

        if (instance == null) {
            instance = new QuakeRepository(application);
        }
        return instance;
    }

    /**
     * Funcion encargada de cargar la lista de sismos desde la red en sismologia.cl
     *
     * @return MutableLiveData con los sismos
     */
    public MutableLiveData<List<QuakeModel>> getQuakes() {
        sendGetQuakes();
        return mQuakeMutableList;
    }

    /**
     * Funcion encargada de crear la request HTTP hacia el servidor y parsear el JSON con los
     * sismos
     */
    private void sendGetQuakes() {

        mQuakeList.clear();

        Response.Listener<String> listener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //Parseando la informacion desde heroku get_quakes.php
                try {

                    JSONObject jsonObject = new JSONObject(response);

                    JSONArray mJsonArray = jsonObject.getJSONArray(mApplication.getString(R.string.KEY_QUAKES));

                    for (int i = 0; i < mJsonArray.length(); i++) {

                        JSONObject mObject = mJsonArray.getJSONObject(i);

                        QuakeModel mModel = new QuakeModel();

                        SimpleDateFormat mFormat =
                                new SimpleDateFormat(mApplication.getString(R.string.DATETIME_FORMAT), Locale.US);
                        mFormat.setTimeZone(TimeZone.getDefault());


                        //OBTENER UTC DESDE PHP CONVERTIRLO A LOCAL DEL DISPOSITIVO
                        Date mUtcDate =
                                mFormat.parse(mObject.getString(mApplication.getString(R.string.KEY_FECHA_UTC)));
                        assert mUtcDate != null;
                        Date mLocalDate = Utils.utcToLocal(mUtcDate);

                        //LOCAL CALCULADO, NO PROVIENE DE CAMPO EN PHP
                        mModel.setFechaLocal(mLocalDate);

                        mModel.setCiudad(mObject.getString(mApplication.getString(R.string.KEY_CIUDAD)));
                        mModel.setLatitud(mObject.getString(mApplication.getString(R.string.KEY_LATITUD)));
                        mModel.setLongitud(mObject.getString(mApplication.getString(R.string.KEY_LONGITUD)));
                        mModel.setMagnitud(mObject.getDouble(mApplication.getString(R.string.KEY_MAGNITUD)));
                        mModel.setEscala(mObject.getString(mApplication.getString(R.string.KEY_ESCALA)));
                        mModel.setProfundidad(mObject.getDouble(mApplication.getString(R.string.KEY_PROFUNDIDAD)));
                        mModel.setAgencia(mObject.getString(mApplication.getString(R.string.KEY_AGENCIA)));
                        mModel.setReferencia(mObject.getString(mApplication.getString(R.string.KEY_REFERENCIA)));
                        mModel.setImagenUrl(mObject.getString(mApplication.getString(R.string.KEY_IMAGEN_URL)));
                        mModel.setEstado(mObject.getString(mApplication.getString(R.string.KEY_ESTADO)));

                        switch (mObject.getInt(mApplication.getString(R.string.KEY_SENSIBLE))) {

                            case 0:
                                mModel.setSensible(false);
                                break;
                            case 1:
                                mModel.setSensible(true);
                                break;
                        }
                        mQuakeList.add(mModel);
                    }
                    mQuakeMutableList.postValue(mQuakeList);
                    isLoadingQuake.postValue(false);

                } catch (JSONException e) {

                    Log.d(mApplication.getString(R.string.TAG_JSON_GENERAL_ERROR), Objects.requireNonNull(e.getMessage()));
                    Crashlytics.log(Log.DEBUG,
                            mApplication.getString(R.string.TAG_JSON_GENERAL_ERROR),
                            e.getMessage());
                } catch (ParseException e) {

                    Log.d(mApplication.getString(R.string.TAG_JSON_PARSE_ERROR), Objects.requireNonNull(e.getMessage()));
                    Crashlytics.log(Log.DEBUG,
                            mApplication.getString(R.string.TAG_JSON_PARSE_ERROR), e.getMessage());
                }

                //LOGS
                Log.d(mApplication.getString(R.string.TAG_CONNECTION_OK),
                        mApplication.getString(R.string.CONNECTION_OK_RESPONSE));
                Crashlytics.log(Log.DEBUG, mApplication.getString(R.string.TAG_CONNECTION_OK),
                        mApplication.getString(R.string.CONNECTION_OK_RESPONSE));
                Crashlytics.setBool(mApplication.getString(R.string.CONNECTED), true);

                volleyError = false;
                contador_request = 0;
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                isLoadingQuake.postValue(false);
                volleyError = true;
                if (contador_request == 2) {

                    //Reiniciar parametros para empezar el proceso otra vez hacia produccion
                    volleyError = false;
                    contador_request = 0;

                    if (error instanceof TimeoutError) {
                        Log.d(mApplication.getString(R.string.TAG_VOLLEY_ERROR), mApplication.getString(R.string.TAG_VOLLEY_ERROR_TIMEOUT));
                        Crashlytics.log(Log.DEBUG, mApplication.getString(R.string.TAG_VOLLEY_ERROR), mApplication.getString(R.string.TAG_VOLLEY_ERROR_TIMEOUT));
                        responseMsgErrorList.postValue(mApplication.getString(R.string.VIEWMODEL_TIMEOUT_ERROR));
                    }

                    //Error de conexion a internet
                    else if (error instanceof NetworkError) {
                        Log.d(mApplication.getString(R.string.TAG_VOLLEY_ERROR), mApplication.getString(R.string.TAG_VOLLEY_ERROR_NETWORK));
                        responseMsgErrorList.postValue(mApplication.getString(R.string.VIEWMODEL_NOCONNECTION_ERROR));
                    }

                    //Error de servidor
                    else if (error instanceof ServerError) {
                        Log.d(mApplication.getString(R.string.TAG_VOLLEY_ERROR),
                                mApplication.getString(R.string.TAG_VOLLEY_ERROR_SERVER));
                        Crashlytics.log(Log.DEBUG,
                                mApplication.getString(R.string.TAG_VOLLEY_ERROR),
                                mApplication.getString(R.string.TAG_VOLLEY_ERROR_SERVER));
                        responseMsgErrorList.postValue(mApplication.getString(R.string.VIEWMODEL_SERVER_ERROR));

                    }

                } else {
                    VolleySingleton.getInstance(mApplication).cancelPendingRequests("TAG");
                    sendGetQuakes();
                }

            }
        };

        /*
         * SECCION CONEXION DE RESPALDOS
         */
        SharedPreferences sharedPreferences = mApplication.getSharedPreferences(mApplication.getString(R.string.MAIN_SHARED_PREF_KEY), Context.MODE_PRIVATE);
        String limite =
                String.valueOf(sharedPreferences.getInt(mApplication.getString(R.string.SHARED_PREF_LIST_QUAKE_NUMBER), 0));

        Log.d(mApplication.getString(R.string.TAG_RESPOSITORY_QUAKE_LIMIT), limite);

        if (limite.equals("0")) {
            limite = "15";
            Log.d(mApplication.getString(R.string.TAG_RESPOSITORY_QUAKE_LIMIT), limite);
        }

        //Si servidor oficial arroja error, conectar a dev
        StringRequest mRequest;
        if (volleyError) {
            contador_request += 1;
            mRequest = new StringRequest(
                    Request.Method.GET,
                    String.format(Locale.US, mApplication.getString(R.string.URL_GET_DEV), limite),
                    listener,
                    errorListener);

            Log.d(mApplication.getString(R.string.TAG_CONNECTION_SERVER_RESPALDO),
                    mApplication.getString(R.string.TAG_CONNECTION_SERVER_RESPALDO_RESPONSE));

            Crashlytics.log(Log.DEBUG,
                    mApplication.getString(R.string.TAG_CONNECTION_SERVER_RESPALDO),
                    mApplication.getString(R.string.TAG_CONNECTION_SERVER_RESPALDO_RESPONSE));
        }

        //Si servidor oficial funciona conectarse a él
        else {
            contador_request += 1;
            mRequest = new StringRequest(
                    Request.Method.GET,
                    String.format(Locale.US, mApplication.getString(R.string.URL_GET_PROD), limite),
                    listener,
                    errorListener);
            Log.d(mApplication.getString(R.string.TAG_CONNECTION_SERVER_OFICIAL),
                    mApplication.getString(R.string.TAG_CONNECTION_SERVER_OFICIAL_RESPONSE));

            Crashlytics.log(Log.DEBUG,
                    mApplication.getString(R.string.TAG_CONNECTION_SERVER_OFICIAL),
                    mApplication.getString(R.string.TAG_CONNECTION_SERVER_OFICIAL_RESPONSE));

        }
        /*String url = String.format(Locale.US, mApplication.getString(R.string.URL_GET_PROD), limite);
        //TEST DEV DIRECTO
        StringRequest mRequest = new StringRequest(
                Request.Method.GET,
                url,
                listener,
                errorListener);*/
        isLoadingQuake.postValue(true);

        mRequest.setRetryPolicy(new DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingleton.getInstance(mApplication).addToRequestQueue(mRequest, TAG_GET_QUAKES);
    }


    /**
     * Funcion del repositorio que envia directamente el listado de sismos al viewmodel
     *
     * @return Lista de sismos normal
     */
    public List<QuakeModel> getQuakeList() {
        return mQuakeList;
    }

    /**
     * Funcion encargada de enviar el status data al viewmodel
     *
     * @return MutableLiveData de status data
     */
    public MutableLiveData<String> getResponseMsgErrorList() {
        return responseMsgErrorList;
    }

    public MutableLiveData<Boolean> getIsLoading() {
        return isLoadingQuake;
    }
}
