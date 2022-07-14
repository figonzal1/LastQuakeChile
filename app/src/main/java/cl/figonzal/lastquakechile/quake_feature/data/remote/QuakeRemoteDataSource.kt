package cl.figonzal.lastquakechile.quake_feature.data.remote

import cl.figonzal.lastquakechile.core.utils.toQuakeListEntity
import cl.figonzal.lastquakechile.quake_feature.data.local.entity.QuakeEntity


class QuakeRemoteDataSource(
    private val quakeAPI: QuakeAPI
) {

    suspend fun getQuakes(limit: Int): List<QuakeEntity>? {

        val call = quakeAPI.listQuakes(limit)

        return call.body()?.quakes?.toQuakeListEntity()
    }
}
