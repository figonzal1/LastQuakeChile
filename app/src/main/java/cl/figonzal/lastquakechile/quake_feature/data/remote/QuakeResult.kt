package cl.figonzal.lastquakechile.quake_feature.data.remote

import cl.figonzal.lastquakechile.quake_feature.data.remote.dto.QuakeDTO
import com.google.gson.annotations.SerializedName

/**
 * Result object for retrofit call
 */
data class QuakeResult(
    @SerializedName("sismos")
    val quakes: List<QuakeDTO>
)