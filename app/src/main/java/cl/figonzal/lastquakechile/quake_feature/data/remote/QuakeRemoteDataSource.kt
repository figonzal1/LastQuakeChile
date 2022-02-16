package cl.figonzal.lastquakechile.quake_feature.data.remote

import android.app.Application
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import cl.figonzal.lastquakechile.R
import cl.figonzal.lastquakechile.core.ApplicationController
import cl.figonzal.lastquakechile.quake_feature.data.local.entity.QuakeEntity

class QuakeRemoteDataSource(
    private val application: Application
) {

    //Leer preference settings
    private val sharedPreferences: SharedPreferences? =
        PreferenceManager.getDefaultSharedPreferences(application.applicationContext)
    private val limit =
        sharedPreferences?.getInt(
            application.getString(R.string.SHARED_PREF_LIST_QUAKE_NUMBER),
            15
        )

    private val service: QuakeAPI by lazy {
        (application as ApplicationController).apiService.create(QuakeAPI::class.java)
    }

    suspend fun getQuakes(): List<QuakeEntity> {

        val call = limit?.let { service.listQuakes(it) }

        return call?.body()?.quakes?.map { it.toQuakeEntity() }!!
    }
}
