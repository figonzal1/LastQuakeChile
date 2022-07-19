package cl.figonzal.lastquakechile.quake_feature.data.remote.dto

import cl.figonzal.lastquakechile.quake_feature.data.local.entity.QuakeEntity
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class QuakeDTO(

    @field:Json(name = "quakeCode")
    val quakeCode: Int,

    @field:Json(name = "localDate", ignore = true)
    val localDate: String? = null,

    @field:Json(name = "utcDate")
    val utcDate: String,

    @field:Json(name = "city")
    val city: String,

    @field:Json(name = "reference")
    val reference: String,

    @field:Json(name = "magnitude")
    val magnitude: Double,

    @field:Json(name = "depth")
    val depth: Double,

    @field:Json(name = "scale")
    val scale: String,

    @field:Json(name = "coordinate")
    val coordinate: CoordinateDTO,

    @field:Json(name = "isSensitive")
    val isSensitive: Boolean,

    @field:Json(name = "isVerified")
    val isVerified: Boolean,

    @field:Json(name = "createdDate", ignore = true)
    val createdDate: String? = null,

    @field:Json(name = "lastModified", ignore = true)
    val lastModified: String? = null,

    @field:Json(name = "_links")
    val links: Any? = null

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
