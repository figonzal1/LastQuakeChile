package cl.figonzal.lastquakechile.core.utils

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import cl.figonzal.lastquakechile.core.data.remote.ApiError
import cl.figonzal.lastquakechile.quake_feature.data.remote.QuakeAPI
import cl.figonzal.lastquakechile.reports_feature.data.remote.ReportAPI
import com.skydoves.sandwich.StatusCode
import com.skydoves.sandwich.adapters.ApiResponseCallAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

private val okHttpClient = OkHttpClient().newBuilder()
    .addInterceptor(HttpLoggingInterceptor().apply {

        //HTTP LOGGER
        level = HttpLoggingInterceptor.Level.BODY
    })
    .connectTimeout(10, TimeUnit.SECONDS)
    .readTimeout(10, TimeUnit.SECONDS)
    .writeTimeout(10, TimeUnit.SECONDS)
    .build()

fun provideApiService(url: String): Retrofit =
    Retrofit.Builder()
        .baseUrl(url)
        .client(okHttpClient)
        .addCallAdapterFactory(ApiResponseCallAdapterFactory.create())
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

fun provideQuakeAPI(apiService: Retrofit): QuakeAPI = apiService.create(QuakeAPI::class.java)

fun provideReportAPI(apiService: Retrofit): ReportAPI = apiService.create(ReportAPI::class.java)

@SuppressLint("MissingPermission")
fun isWifiConnected(context: Context): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val capabilities = cm.getNetworkCapabilities(cm.activeNetwork)
    return capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
}

fun Context.processSandwichError(message: String, statusCode: StatusCode?): ApiError {

    var apiError = when {
        statusCode == StatusCode.NotFound -> ApiError.HttpError
        statusCode == StatusCode.RequestTimeout ||
                statusCode == StatusCode.InternalServerError ||
                statusCode == StatusCode.ServiceUnavailable ||
                statusCode == StatusCode.Unknown -> ApiError.ServerError
        message.contains("10000ms") ||
                message.contains("failed to connect", true) ||
                message.contains("unable to resolve host", true) -> ApiError.TimeoutError
        else -> ApiError.UnknownError
    }

    if (!isWifiConnected(this)) apiError = ApiError.NoWifiError

    return apiError
}