package cl.figonzal.lastquakechile.quake_feature.data.remote

import cl.figonzal.lastquakechile.core.data.remote.Embedded
import com.skydoves.sandwich.ApiResponse


class QuakeRemoteDataSource(
    private val quakeAPI: QuakeAPI
) {

    suspend fun getQuakes(limit: Int): ApiResponse<Embedded<QuakePayload>> {
        return quakeAPI.listQuakes(limit)
    }
}
