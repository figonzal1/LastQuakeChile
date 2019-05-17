package cl.figonzal.lastquakechile.repository;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

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

	private static QuakeRepository sInstanceRepository;
	private final MutableLiveData<List<QuakeModel>> mQuakeMutableList = new MutableLiveData<>();
	private final List<QuakeModel> mQuakeList = new ArrayList<>();
	private final Application mApplication;
	private final MutableLiveData<String> mStatusData = new MutableLiveData<>();
	private boolean volleyError = false;
	private int contador_request = 0;

	/**
	 * Contructor que permite instanciar el contexto de la acitivity
	 *
	 * @param application Permite acceder a los strings
	 */
	private QuakeRepository (Application application) {
		this.mApplication = application;
	}

	/**
	 * Funcion singleton que permite instanciar el repositorio
	 *
	 * @return Instancia de repositorio
	 */
	public static QuakeRepository getIntance (Application application) {

		if (sInstanceRepository == null) {
			sInstanceRepository = new QuakeRepository(application);
		}
		return sInstanceRepository;
	}

	/**
	 * Funcion encargada de cargar la lista de sismos desde la red en sismologia.cl
	 *
	 * @return MutableLiveData con los sismos
	 */
	public MutableLiveData<List<QuakeModel>> getMutableQuakeList () {
		if (mQuakeList.size() > 0) {
			mQuakeList.clear();
		}
		loadQuakes();
		return mQuakeMutableList;
	}

	/**
	 * Funcion encargada de enviar el status data al viewmodel
	 *
	 * @return MutableLiveData de status data
	 */
	public MutableLiveData<String> getStatusData () {
		return mStatusData;
	}

	/**
	 * Funcion del repositorio que envia directamente el listado de sismos al viewmodel
	 *
	 * @return Lista de sismos normal
	 */
	public List<QuakeModel> getQuakeList () {
		return mQuakeList;
	}

	/**
	 * Funcion encargada de crear la request HTTP hacia el servidor y parsear el JSON con los
	 * sismos
	 */
	private void loadQuakes () {

		Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
			@Override
			public void onResponse (JSONObject response) {
				//Parseando la informacion desde heroku get_quakes.php
				try {

					JSONArray mJsonArray =
							response.getJSONArray(mApplication.getString(R.string.KEY_QUAKES));

					for (int i = 0; i < mJsonArray.length(); i++) {

						JSONObject mObject = mJsonArray.getJSONObject(i);

						QuakeModel mModel = new QuakeModel();

						SimpleDateFormat mFormat =
								new SimpleDateFormat(mApplication.getString(R.string.DATETIME_FORMAT), Locale.US);
						mFormat.setTimeZone(TimeZone.getDefault());


						//OBTENER UTC DESDE PHP CONVERTIRLO A LOCAL DEL DISPOSITIVO
						Date mUtcDate =
								mFormat.parse(mObject.getString(mApplication.getString(R.string.KEY_FECHA_UTC)));
						Date mLocalDate = QuakeUtils.utcToLocal(mUtcDate);

						Log.d("DATETIME",
								"UTC: " + mFormat.format(mUtcDate) + "- LOCAL: " + mFormat.format(mLocalDate));
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

				} catch (JSONException e) {

					Log.d(mApplication.getString(R.string.TAG_JSON_GENERAL_ERROR), e.getMessage());
					Crashlytics.log(Log.DEBUG,
							mApplication.getString(R.string.TAG_JSON_GENERAL_ERROR),
							e.getMessage());
				} catch (ParseException e) {

					Log.d(mApplication.getString(R.string.TAG_JSON_PARSE_ERROR), e.getMessage());
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
			}
		};

		Response.ErrorListener errorListener = new Response.ErrorListener() {
			@Override
			public void onErrorResponse (VolleyError error) {

				volleyError = true;
				if (contador_request >= 2) {

					if (error instanceof TimeoutError) {
						Log.d(mApplication.getString(R.string.TAG_VOLLEY_ERROR),
								mApplication.getString(R.string.TAG_VOLLEY_ERROR_TIMEOUT));
						Crashlytics.log(Log.DEBUG,
								mApplication.getString(R.string.TAG_VOLLEY_ERROR),
								mApplication.getString(R.string.TAG_VOLLEY_ERROR_TIMEOUT));
						//mStatusData.postValue(mApplication.getString(R.string
						// .VIEWMODEL_TIMEOUT_ERROR));

					} else if (error instanceof NoConnectionError) {
						Log.d(mApplication.getString(R.string.TAG_VOLLEY_ERROR),
								mApplication.getString(R.string.TAG_VOLLEY_ERROR_CONNECTION));
						Crashlytics.log(Log.DEBUG,
								mApplication.getString(R.string.TAG_VOLLEY_ERROR),
								mApplication.getString(R.string.TAG_VOLLEY_ERROR_CONNECTION));
						mStatusData.postValue(mApplication.getString(R.string.VIEWMODEL_NOCONNECTION_ERROR));

					} else if (error instanceof AuthFailureError) {
						Log.d(mApplication.getString(R.string.TAG_VOLLEY_ERROR),
								mApplication.getString(R.string.TAG_VOLLEY_ERROR_AUTH));

					} else if (error instanceof ServerError) {
						Log.d(mApplication.getString(R.string.TAG_VOLLEY_ERROR),
								mApplication.getString(R.string.TAG_VOLLEY_ERROR_SERVER));
						Crashlytics.log(Log.DEBUG,
								mApplication.getString(R.string.TAG_VOLLEY_ERROR),
								mApplication.getString(R.string.TAG_VOLLEY_ERROR_SERVER));
						mStatusData.postValue(mApplication.getString(R.string.VIEWMODEL_SERVER_ERROR));

					} else if (error instanceof NetworkError) {
						Log.d(mApplication.getString(R.string.TAG_VOLLEY_ERROR),
								mApplication.getString(R.string.TAG_VOLLEY_ERROR_NETWORK));

					} else if (error instanceof ParseError) {
						Log.d(mApplication.getString(R.string.TAG_VOLLEY_ERROR),
								mApplication.getString(R.string.TAG_VOLLEY_ERROR_PARSE));

					}
				} else {
					VolleySingleton.getInstance(mApplication).cancelPendingRequests("TAG");
					loadQuakes();
				}

			}
		};

		/*
		SECCION CONEXION DE RESPALDOS
		 */
		String limite = "15";

		//Si servidor oficial arroja error, conectar a dev
		JsonObjectRequest mRequest;
		if (volleyError) {
			contador_request += 1;
			mRequest = new JsonObjectRequest(Request.Method.GET,
					String.format(Locale.US, mApplication.getString(R.string.URL_GET_DEV), limite),
					null, listener, errorListener);

			Log.d(mApplication.getString(R.string.TAG_CONNECTION_SERVER_RESPALDO),
					mApplication.getString(R.string.TAG_CONNECTION_SERVER_RESPALDO_RESPONSE));

			Crashlytics.log(Log.DEBUG,
					mApplication.getString(R.string.TAG_CONNECTION_SERVER_RESPALDO),
					mApplication.getString(R.string.TAG_CONNECTION_SERVER_RESPALDO_RESPONSE));
		}

		//Si servidor oficial funciona conectarse a él
		else {
			contador_request += 1;
			mRequest = new JsonObjectRequest(Request.Method.GET,
					String.format(Locale.US, mApplication.getString(R.string.URL_GET_PROD),
							limite),
					null, listener, errorListener);
			Log.d(mApplication.getString(R.string.TAG_CONNECTION_SERVER_OFICIAL),
					mApplication.getString(R.string.TAG_CONNECTION_SERVER_OFICIAL_RESPONSE));

			Crashlytics.log(Log.DEBUG,
					mApplication.getString(R.string.TAG_CONNECTION_SERVER_OFICIAL),
					mApplication.getString(R.string.TAG_CONNECTION_SERVER_OFICIAL_RESPONSE));

		}
		mRequest.setShouldCache(true);
		mRequest.setRetryPolicy(new DefaultRetryPolicy(2500, 0,
				DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
		VolleySingleton.getInstance(mApplication).addToRequestQueue(mRequest, "TAG");
	}
}
