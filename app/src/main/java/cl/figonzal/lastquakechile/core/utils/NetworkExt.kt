package cl.figonzal.lastquakechile.core.utils

import cl.figonzal.lastquakechile.quake_feature.data.remote.QuakeAPI
import cl.figonzal.lastquakechile.reports_feature.data.remote.ReportAPI
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private val okHttpClient = OkHttpClient().newBuilder()
    .addInterceptor(HttpLoggingInterceptor().apply {

        //HTTP LOGGER
        level = HttpLoggingInterceptor.Level.NONE
    }).build()

fun provideApiService(): Retrofit =
    Retrofit.Builder()
        .baseUrl("https://lastquakechile-server-prod.herokuapp.com")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

fun provideQuakeAPI(apiService: Retrofit): QuakeAPI {
    return apiService.create(QuakeAPI::class.java)
}

fun provideReportAPI(apiService: Retrofit): ReportAPI {
    return apiService.create(ReportAPI::class.java)
}