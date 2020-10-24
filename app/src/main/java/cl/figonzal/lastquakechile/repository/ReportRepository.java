package cl.figonzal.lastquakechile.repository;

import android.app.Application;

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
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

import cl.figonzal.lastquakechile.R;
import cl.figonzal.lastquakechile.model.ReportModel;
import cl.figonzal.lastquakechile.services.VolleySingleton;
import cl.figonzal.lastquakechile.viewmodel.SingleLiveEvent;
import timber.log.Timber;

public class ReportRepository implements NetworkRepository<ReportModel> {

    private static final String TAG_GET_REPORTS = "ListadoReportes";
    private static ReportRepository instance;
    private final Application mApplication;

    //REPORTES
    private List<ReportModel> reportModelList = new ArrayList<>();
    private final MutableLiveData<List<ReportModel>> reportMutableLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoadingReports = new MutableLiveData<>();
    private final SingleLiveEvent<String> responseMsgErrorList = new SingleLiveEvent<>();

    private ReportRepository(Application application) {
        this.mApplication = application;
    }

    public static ReportRepository getIntance(Application application) {

        if (instance == null) {

            instance = new ReportRepository(application);
        }

        return instance;
    }

    @NonNull
    @Override
    public MutableLiveData<List<ReportModel>> getData() {
        sendGetReports();

        return reportMutableLiveData;
    }

    @NonNull
    @Override
    public MutableLiveData<Boolean> isLoading() {
        return isLoadingReports;
    }

    @NonNull
    @Override
    public SingleLiveEvent<String> getMsgErrorList() {
        return responseMsgErrorList;
    }

    private void sendGetReports() {

        Response.Listener<String> listener = response -> {

            reportModelList.clear();

            Gson gson = new Gson();

            JsonObject reportes = gson.fromJson(response, JsonObject.class);

            reportModelList = gson.fromJson(reportes.get("reportes"), new TypeToken<List<ReportModel>>() {
            }.getType());

            reportMutableLiveData.postValue(reportModelList);
            isLoadingReports.postValue(false);
        };

        final Response.ErrorListener errorListener = error -> {

            isLoadingReports.postValue(false);

            if (error instanceof TimeoutError) {

                Timber.e(mApplication.getString(R.string.TAG_VOLLEY_ERROR_TIMEOUT));

                responseMsgErrorList.postValue(mApplication.getString(R.string.VIEWMODEL_TIMEOUT_ERROR));
            }

            //Error de conexion a internet
            else if (error instanceof NetworkError) {

                Timber.e(mApplication.getString(R.string.TAG_VOLLEY_ERROR_NETWORK));

                responseMsgErrorList.postValue(mApplication.getString(R.string.VIEWMODEL_NOCONNECTION_ERROR));
            }

            //Error de servidor
            else if (error instanceof ServerError) {

                Timber.e(mApplication.getString(R.string.TAG_VOLLEY_ERROR_SERVER));

                responseMsgErrorList.postValue(mApplication.getString(R.string.VIEWMODEL_SERVER_ERROR));
            }
        };

        String url = mApplication.getString(R.string.URL_GET_PROD_REPORTS);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, listener, errorListener);
        isLoadingReports.postValue(true);

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        VolleySingleton.getInstance(mApplication).addToRequestQueue(stringRequest, TAG_GET_REPORTS);
    }
}
