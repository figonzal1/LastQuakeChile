package cl.figonzal.lastquakechile.quake_feature.data.remote

import cl.figonzal.lastquakechile.quake_feature.data.remote.dto.QuakeDTO
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
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

@JsonClass(generateAdapter = true)
data class QuakeResult(
    @Json(name = "_embedded")
    val embedded: QuakePayload
)

@JsonClass(generateAdapter = true)
data class QuakePayload(
    @Json(name = "quakes")
    val quakes: List<QuakeDTO>
)
