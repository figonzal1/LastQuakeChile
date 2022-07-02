package cl.figonzal.lastquakechile.quake_feature.data.remote

import androidx.annotation.Keep
import cl.figonzal.lastquakechile.quake_feature.data.remote.dto.QuakeDTO
import com.google.gson.annotations.SerializedName

/**
 * Result object for retrofit call
 */
@Keep
data class QuakeResult(
    @SerializedName("sismos")
    val quakes: List<QuakeDTO>
)