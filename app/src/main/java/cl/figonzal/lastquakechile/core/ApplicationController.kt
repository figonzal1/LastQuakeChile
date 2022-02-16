package cl.figonzal.lastquakechile.core

import android.app.Application
import cl.figonzal.lastquakechile.BuildConfig
import cl.figonzal.lastquakechile.R
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import timber.log.Timber.DebugTree

class ApplicationController : Application() {

    override fun onCreate() {
        super.onCreate()

        when {
            BuildConfig.DEBUG -> Timber.plant(DebugTree())
            else -> Timber.plant(CrashlyticsTree())
        }
    }

    val database by lazy { AppDatabase.getDatabase(this) }

    /*
    RETROFIT 2
     */
    private val okHttpClient = OkHttpClient().newBuilder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.NONE
        }).build()

    val apiService: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(this.getString(R.string.BASE_URL))
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}