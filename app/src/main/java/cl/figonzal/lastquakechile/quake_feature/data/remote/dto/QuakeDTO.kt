package cl.figonzal.lastquakechile.quake_feature.data.remote.dto

import cl.figonzal.lastquakechile.quake_feature.data.local.entity.QuakeEntity
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class QuakeDTO(

    @param:Json(name = "quakeCode")
    val quakeCode: Int,

    @param:Json(name = "localDate", ignore = true)
    val localDate: String? = null,

    @param:Json(name = "utcDate")
    val utcDate: String,

    @param:Json(name = "city")
    val city: String,

    @param:Json(name = "reference")
    val reference: String,

    @param:Json(name = "magnitude")
    val magnitude: Double,

    @param:Json(name = "depth")
    val depth: Double,

    @param:Json(name = "scale")
    val scale: String,

    @param:Json(name = "coordinate")
    val coordinate: CoordinateDTO,

    @param:Json(name = "sensitive")
    val isSensitive: Boolean,

    @param:Json(name = "verified")
    val isVerified: Boolean
) {
    fun toEntity() = QuakeEntity(
        quakeCode = quakeCode,
        utcDate = utcDate,
        city = city,
        reference = reference,
        magnitude = magnitude,
        depth = depth,
        scale = scale,
        isSensitive = isSensitive,
        isVerified = isVerified
    )
}
