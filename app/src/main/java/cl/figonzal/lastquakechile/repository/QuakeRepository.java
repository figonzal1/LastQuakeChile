package cl.figonzal.lastquakechile.repository;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import cl.figonzal.lastquakechile.R;
import cl.figonzal.lastquakechile.handlers.DateGsonDeserializer;
import cl.figonzal.lastquakechile.model.QuakeModel;
import cl.figonzal.lastquakechile.services.VolleySingleton;
import cl.figonzal.lastquakechile.viewmodel.SingleLiveEvent;
import timber.log.Timber;


public class QuakeRepository implements NetworkRepository<QuakeModel> {

    private static final String TAG_GET_QUAKES = "ListadoSismos";
    private static QuakeRepository instance;
    private final Context appContext;

    //SISMOS
    private List<QuakeModel> mQuakeList = new ArrayList<>();
    private final MutableLiveData<List<QuakeModel>> mQuakeMutableList = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoadingQuake = new MutableLiveData<>();
    private final SingleLiveEvent<String> responseMsgErrorList = new SingleLiveEvent<>();

    private boolean volleyError = false;
    private int contador_request = 0;

    private QuakeRepository(Context appContext) {
        this.appContext = appContext;
    }

    public static QuakeRepository getIntance(Context context) {

        if (instance == null) {
            instance = new QuakeRepository(context);
        }
        return instance;
    }

    /**
     * Funcion encargada de cargar la lista de sismos desde la red en sismologia.cl
     *
     * @return MutableLiveData con los sismos
     */
    @NonNull
    @Override
    public MutableLiveData<List<QuakeModel>> getData() {
        sendGetQuakes();
        return mQuakeMutableList;
    }

    /**
     * Function encargada de enviar el estado Loading al viewmodel
     *
     * @return MutableLibeData de status loading
     */
    @NonNull
    @Override
    public MutableLiveData<Boolean> isLoading() {
        return isLoadingQuake;
    }

    /**
     * Funcion encargada de enviar el status data al viewmodel
     *
     * @return MutableLiveData de status data
     */
    @NonNull
    @Override
    public SingleLiveEvent<String> getMsgErrorList() {
        return responseMsgErrorList;
    }

    /**
     * Funcion encargada de crear la request HTTP hacia el servidor y parsear el JSON con los
     * sismos
     */
    private void sendGetQuakes() {

        mQuakeList.clear();

        Response.Listener<String> listener = response -> {

            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(Date.class, new DateGsonDeserializer(appContext))
                    .create();
            JsonObject sismos = gson.fromJson(response, JsonObject.class);

            mQuakeList = gson.fromJson(sismos.get("sismos"), new TypeToken<List<QuakeModel>>() {
            }.getType());

            mQuakeMutableList.postValue(mQuakeList);
            isLoadingQuake.postValue(false);

            //LOGS
            Timber.i(appContext.getString(R.string.CONNECTION_OK_RESPONSE));

            volleyError = false;
            contador_request = 0;
        };

        Response.ErrorListener errorListener = error -> {

            isLoadingQuake.postValue(false);

            volleyError = true;

            if (contador_request == 2) {

                //Reiniciar parametros para empezar el proceso otra vez hacia produccion
                volleyError = false;
                contador_request = 0;

                if (error instanceof TimeoutError) {

                    Timber.e(appContext.getString(R.string.TAG_VOLLEY_ERROR_TIMEOUT));

                    responseMsgErrorList.postValue(appContext.getString(R.string.VIEWMODEL_TIMEOUT_ERROR));
                }

                //Error de conexion a internet
                else if (error instanceof NetworkError) {

                    Timber.e(appContext.getString(R.string.TAG_VOLLEY_ERROR_NETWORK));

                    responseMsgErrorList.postValue(appContext.getString(R.string.VIEWMODEL_NOCONNECTION_ERROR));
                }

                //Error de servidor
                else if (error instanceof ServerError) {

                    Timber.e(appContext.getString(R.string.TAG_VOLLEY_ERROR_SERVER));

                    responseMsgErrorList.postValue(appContext.getString(R.string.VIEWMODEL_SERVER_ERROR));
                }

            } else {
                VolleySingleton.getInstance(appContext).cancelPendingRequests(TAG_GET_QUAKES);
                sendGetQuakes();
            }

        };

        /*
         * SECCION CONEXION DE RESPALDOS
         */
        SharedPreferences sharedPreferences = appContext.getSharedPreferences(appContext.getString(R.string.SHARED_PREF_MASTER_KEY), Context.MODE_PRIVATE);
        String limite = String.valueOf(sharedPreferences.getInt(appContext.getString(R.string.SHARED_PREF_LIST_QUAKE_NUMBER), 0));

        Timber.i("Limite: %s", limite);

        if (limite.equals("0")) {

            limite = "15";

            Timber.i("Limite: %s", limite);
        }

        //Si servidor oficial arroja error, conectar a dev
        StringRequest mRequest;

        if (volleyError) {

            contador_request += 1;

            mRequest = new StringRequest(
                    Request.Method.GET,
                    String.format(Locale.US, appContext.getString(R.string.URL_GET_DEV), limite),
                    listener,
                    errorListener);

            Timber.i(appContext.getString(R.string.TAG_CONNECTION_SERVER_RESPALDO_RESPONSE));
        }

        //Si servidor oficial funciona conectarse a él
        else {

            contador_request += 1;
            mRequest = new StringRequest(
                    Request.Method.GET,
                    String.format(Locale.US, appContext.getString(R.string.URL_GET_PROD_QUAKES), limite),
                    listener,
                    errorListener);

            Timber.i(appContext.getString(R.string.TAG_CONNECTION_SERVER_OFICIAL_RESPONSE));

        }
        isLoadingQuake.postValue(true);

        //3 seg de espera y 0 retries
        mRequest.setRetryPolicy(new DefaultRetryPolicy(
                3000,
                0,
                2));
        VolleySingleton.getInstance(appContext).addToRequestQueue(mRequest, TAG_GET_QUAKES);
    }
}
