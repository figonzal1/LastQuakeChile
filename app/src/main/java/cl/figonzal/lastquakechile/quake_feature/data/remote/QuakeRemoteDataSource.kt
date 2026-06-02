package cl.figonzal.lastquakechile.quake_feature.data.remote

import cl.figonzal.lastquakechile.core.data.remote.mapSuccess
import cl.figonzal.lastquakechile.quake_feature.data.remote.dto.QuakeDTO
import com.skydoves.sandwich.ApiResponse

class QuakeRemoteDataSource(private val quakeAPI: QuakeAPI) {
    suspend fun getQuakes(pageIndex: Int): ApiResponse<List<QuakeDTO>> =
        quakeAPI.listQuakes(pageIndex).mapSuccess { embedded?.quakes.orEmpty() }
}
