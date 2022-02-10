package cl.figonzal.lastquakechile.quake_feature.data.remote

import android.app.Application
import cl.figonzal.lastquakechile.core.ApplicationController
import cl.figonzal.lastquakechile.quake_feature.data.local.entity.QuakeEntity

class QuakeRemoteDataSource(
    private val application: Application
) {

    private val service: QuakeAPI by lazy {
        (application as ApplicationController).apiService.create(QuakeAPI::class.java)
    }

    suspend fun getQuakes(): List<QuakeEntity> {

        val call = service.listQuakes(15)

        return call.body()?.quakes?.map { it.toQuakeEntity() }!!
    }
}
