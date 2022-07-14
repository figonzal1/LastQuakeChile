package cl.figonzal.lastquakechile.core.utils

import android.content.Context
import androidx.preference.PreferenceManager
import cl.figonzal.lastquakechile.R
import cl.figonzal.lastquakechile.quake_feature.data.remote.QuakeAPI
import cl.figonzal.lastquakechile.reports_feature.data.remote.ReportAPI
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.moshi.MoshiConverterFactory

private val okHttpClient = OkHttpClient().newBuilder()
    .addInterceptor(HttpLoggingInterceptor().apply {

        //HTTP LOGGER
        level = HttpLoggingInterceptor.Level.NONE
    }).build()

fun provideApiService(url: String): Retrofit =
    Retrofit.Builder()
        .baseUrl(url)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        .build()

fun provideQuakeAPI(apiService: Retrofit): QuakeAPI = apiService.create(QuakeAPI::class.java)

fun provideReportAPI(apiService: Retrofit): ReportAPI = apiService.create(ReportAPI::class.java)

fun provideLimitedList(context: Context): Int =
    PreferenceManager.getDefaultSharedPreferences(context).getInt(
        context.getString(R.string.shared_pref_list_quake_limit),
        15
    )