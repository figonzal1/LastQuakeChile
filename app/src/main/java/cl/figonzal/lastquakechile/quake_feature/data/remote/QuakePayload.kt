package cl.figonzal.lastquakechile.quake_feature.data.remote

import cl.figonzal.lastquakechile.quake_feature.data.remote.dto.QuakeDTO
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class QuakePayload(
    @Json(name = "quakes")
    val quakes: List<QuakeDTO>
)