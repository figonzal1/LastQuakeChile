package cl.figonzal.lastquakechile.quake_feature.data.remote.dto

import cl.figonzal.lastquakechile.quake_feature.data.local.entity.QuakeEntity
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class QuakeDTO(

    @Json(name = "quakeCode")
    val quakeCode: Int,

    @Json(name = "localDate", ignore = true)
    val localDate: String? = null,

    @Json(name = "utcDate")
    val utcDate: String,

    @Json(name = "city")
    val city: String,

    @Json(name = "reference")
    val reference: String,

    @Json(name = "magnitude")
    val magnitude: Double,

    @Json(name = "depth")
    val depth: Double,

    @Json(name = "scale")
    val scale: String,

    @Json(name = "coordinate")
    val coordinate: CoordinateDTO,

    @Json(name = "sensitive")
    val isSensitive: Boolean,

    @Json(name = "verified")
    val isVerified: Boolean,

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
