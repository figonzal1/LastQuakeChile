package cl.figonzal.lastquakechile.repository;

import android.app.Application;
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
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cl.figonzal.lastquakechile.R;
import cl.figonzal.lastquakechile.model.QuakesCity;
import cl.figonzal.lastquakechile.model.ReportModel;
import cl.figonzal.lastquakechile.services.SingleLiveEvent;
import cl.figonzal.lastquakechile.services.VolleySingleton;

public class ReportRepository {

    private static final String TAG_GET_REPORTS = "ListadoReportes";
    private static ReportRepository instance;
    private final Application mApplication;

    //REPORTES
    private final List<ReportModel> reportModelList = new ArrayList<>();
    private final MutableLiveData<List<ReportModel>> reportMutableLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoadingReports = new MutableLiveData<>();
    private final SingleLiveEvent<String> responseMsgErrorList = new SingleLiveEvent<>();

    private static FirebaseCrashlytics crashlytics;

    private ReportRepository(Application application) {
        this.mApplication = application;
    }

    public static ReportRepository getIntance(Application application) {

        if (instance == null) {

            instance = new ReportRepository(application);
            crashlytics = FirebaseCrashlytics.getInstance();
        }

        return instance;
    }

    public MutableLiveData<List<ReportModel>> getReports() {

        sendGetReports();

        return reportMutableLiveData;
    }

    private void sendGetReports() {

        Response.Listener<String> response = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                reportModelList.clear();

                //Log.d("REPONSE", response);
                try {

                    JSONObject jsonObject = new JSONObject(response);

                    JSONArray jsonReports = jsonObject.getJSONArray("reportes");

                    for (int i = 0; i < jsonReports.length(); i++) {

                        JSONObject jsonReport = jsonReports.getJSONObject(i);

                        ReportModel reportModel = new ReportModel();
                        reportModel.setN_sismos(jsonReport.getInt("n_sismos"));
                        reportModel.setN_sensibles(jsonReport.getInt("n_sensibles"));
                        reportModel.setMes_reporte(jsonReport.getString("mes_reporte"));

                        reportModel.setProm_magnitud(jsonReport.getDouble("prom_magnitud"));
                        reportModel.setProm_profundidad(jsonReport.getDouble("prom_profundidad"));
                        reportModel.setMax_magnitud(jsonReport.getDouble("max_magnitud"));
                        reportModel.setMin_profundidad(jsonReport.getDouble("min_profundidad"));

                        JSONArray jsonArray = jsonReport.getJSONArray("top_ciudades");

                        List<QuakesCity> quakesCityList = new ArrayList<>();

                        for (int j = 0; j < jsonArray.length(); j++) {

                            JSONObject jsonCity = jsonArray.getJSONObject(j);

                            QuakesCity city = new QuakesCity();
                            city.setCiudad(jsonCity.getString("ciudad"));
                            city.setN_sismos(jsonCity.getInt("n_sismos"));

                            quakesCityList.add(city);
                        }

                        reportModel.setQuakesCities(quakesCityList);

                        reportModelList.add(reportModel);
                    }

                    reportMutableLiveData.postValue(reportModelList);
                    isLoadingReports.postValue(false);

                } catch (JSONException e) {

                    Log.d(mApplication.getString(R.string.TAG_JSON_GENERAL_ERROR), "Json exception" + e.getMessage());
                    crashlytics.log(mApplication.getString(R.string.TAG_JSON_GENERAL_ERROR) + e.getMessage());
                }
            }
        };

        final Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                isLoadingReports.postValue(false);

                if (error instanceof TimeoutError) {

                    Log.d(mApplication.getString(R.string.TAG_VOLLEY_ERROR), mApplication.getString(R.string.TAG_VOLLEY_ERROR_TIMEOUT));
                    crashlytics.log(mApplication.getString(R.string.TAG_VOLLEY_ERROR) + mApplication.getString(R.string.TAG_VOLLEY_ERROR_TIMEOUT));

                    responseMsgErrorList.postValue(mApplication.getString(R.string.VIEWMODEL_TIMEOUT_ERROR));
                }

                //Error de conexion a internet
                else if (error instanceof NetworkError) {

                    Log.d(mApplication.getString(R.string.TAG_VOLLEY_ERROR), mApplication.getString(R.string.TAG_VOLLEY_ERROR_NETWORK));

                    responseMsgErrorList.postValue(mApplication.getString(R.string.VIEWMODEL_NOCONNECTION_ERROR));
                }

                //Error de servidor
                else if (error instanceof ServerError) {

                    Log.d(mApplication.getString(R.string.TAG_VOLLEY_ERROR), mApplication.getString(R.string.TAG_VOLLEY_ERROR_SERVER));
                    crashlytics.log(mApplication.getString(R.string.TAG_VOLLEY_ERROR) + mApplication.getString(R.string.TAG_VOLLEY_ERROR_SERVER));

                    responseMsgErrorList.postValue(mApplication.getString(R.string.VIEWMODEL_SERVER_ERROR));
                }
            }
        };

        String url = mApplication.getString(R.string.URL_GET_PROD_REPORTS);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, response, errorListener);
        isLoadingReports.postValue(true);

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        VolleySingleton.getInstance(mApplication).addToRequestQueue(stringRequest, TAG_GET_REPORTS);
    }

    public MutableLiveData<Boolean> getIsLoadingReports() {
        return isLoadingReports;
    }

    public SingleLiveEvent<String> getResponseMsgErrorList() {
        return responseMsgErrorList;
    }
}
