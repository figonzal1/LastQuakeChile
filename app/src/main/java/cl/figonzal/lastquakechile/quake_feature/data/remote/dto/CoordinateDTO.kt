package cl.figonzal.lastquakechile.quake_feature.data.remote.dto

import cl.figonzal.lastquakechile.quake_feature.data.local.entity.CoordinateEntity
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CoordinateDTO(

    @field:Json(name = "latitude")
    val latitude: Double,

    @field:Json(name = "longitude")
    val longitude: Double,

    @field:Json(name = "_links", ignore = true)
    val links: Any? = null
) {
    fun toEntity() = CoordinateEntity(
        latitude = latitude,
        longitude = longitude
    )
}
