package cl.figonzal.lastquakechile.quake_feature.data.remote

import android.app.Application
import cl.figonzal.lastquakechile.ApplicationController
import cl.figonzal.lastquakechile.quake_feature.domain.model.Quake

class QuakeRemoteDataSource(
    private val application: Application
) {

    private val service: QuakeAPI by lazy {
        (application as ApplicationController).apiService.create(QuakeAPI::class.java)
    }

    suspend fun getQuakes(): List<Quake> {

        val call = service.listQuakes(2)

        return call.body()?.quakes?.map { it.toDomainQuake() } ?: emptyList()
    }
}
