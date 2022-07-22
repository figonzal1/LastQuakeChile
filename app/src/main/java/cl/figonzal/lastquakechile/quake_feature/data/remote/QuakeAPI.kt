package cl.figonzal.lastquakechile.quake_feature.data.remote

import cl.figonzal.lastquakechile.core.data.remote.Embedded
import com.skydoves.sandwich.ApiResponse
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Interface used for retrofit
 */
interface QuakeAPI {

    @GET("/api/v1/quakes")
    suspend fun listQuakes(
        @Query(value = "size") limit: Int = 15,
        @Query(value = "sort") sort: String = "utcDate,desc",
    ): ApiResponse<Embedded<QuakePayload>>
}