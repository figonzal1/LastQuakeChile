package cl.figonzal.lastquakechile.quake_feature.data.remote.dto

import cl.figonzal.lastquakechile.quake_feature.data.local.entity.CoordinateEntity
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CoordinateDTO(

    @param:Json(name = "latitude")
    val latitude: Double,

    @param:Json(name = "longitude")
    val longitude: Double,
) {
    fun toEntity() = CoordinateEntity(
        latitude = latitude,
        longitude = longitude
    )
}
