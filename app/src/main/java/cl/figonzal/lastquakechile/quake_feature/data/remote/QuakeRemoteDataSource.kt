package cl.figonzal.lastquakechile.quake_feature.data.remote

import com.skydoves.sandwich.ApiResponse


class QuakeRemoteDataSource(
    private val quakeAPI: QuakeAPI
) {

    suspend fun getQuakes(limit: Int): ApiResponse<QuakeResult> {
        return quakeAPI.listQuakes(limit)
    }
}
