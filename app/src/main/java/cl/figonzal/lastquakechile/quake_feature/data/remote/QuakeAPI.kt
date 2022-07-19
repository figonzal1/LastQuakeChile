package cl.figonzal.lastquakechile.quake_feature.data.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Interface used for retrofit
 */
interface QuakeAPI {

    @GET("/api/v1/quakes")
    suspend fun listQuakes(
        @Query(value = "sort") sort: String = "utcDate,desc",
    ): Response<QuakeResult>
}