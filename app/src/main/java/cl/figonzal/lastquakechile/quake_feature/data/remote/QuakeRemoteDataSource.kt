package cl.figonzal.lastquakechile.quake_feature.data.remote

import cl.figonzal.lastquakechile.core.utils.toQuakeListEntity
import cl.figonzal.lastquakechile.quake_feature.data.local.entity.relation.QuakeAndCoordinate


class QuakeRemoteDataSource(
    private val quakeAPI: QuakeAPI
) {

    suspend fun getQuakes(limit: Int): List<QuakeAndCoordinate>? {

        val call = quakeAPI.listQuakes()

        return call.body()?.embedded?.quakes?.toQuakeListEntity()
    }
}
