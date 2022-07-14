package cl.figonzal.lastquakechile.quake_feature.data.remote

import cl.figonzal.lastquakechile.quake_feature.data.remote.dto.QuakeDTO
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Result object for retrofit call
 */
@JsonClass(generateAdapter = true)
data class QuakeResult(
    @field:Json(name = "sismos")
    val quakes: List<QuakeDTO>
)