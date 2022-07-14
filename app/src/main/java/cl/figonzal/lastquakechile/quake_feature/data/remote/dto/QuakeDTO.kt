package cl.figonzal.lastquakechile.quake_feature.data.remote.dto

import cl.figonzal.lastquakechile.quake_feature.data.local.entity.QuakeEntity
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Here make changes if API for quakes changes too
 */

@JsonClass(generateAdapter = true)
data class QuakeDTO(

    @field:Json(name = "fecha_utc")
    val utcDate: String,

    @field:Json(name = "ciudad")
    val city: String,

    @field:Json(name = "referencia")
    val reference: String,

    @field:Json(name = "magnitud")
    val magnitude: Double,

    @field:Json(name = "escala")
    val scale: String,

    @field:Json(name = "sensible")
    val sensible: String,

    @field:Json(name = "latitud")
    val latitude: Double,

    @field:Json(name = "longitud")
    val longitude: Double,

    @field:Json(name = "profundidad")
    val depth: Double,

    @field:Json(name = "imagen_url")
    val quakeCode: String,

    @field:Json(name = "estado")
    val state: String,

    //NOT USED
    @field:Json(name = "fecha_local", ignore = true)
    val localDate: String? = null,

    @field:Json(name = "agencia", ignore = true)
    val agency: String? = null
) {

    //DTO -> Entity -> Model domain
    fun toEntity() = QuakeEntity(
        quakeCode = quakeCode.toInt(),
        utcDate = utcDate,
        city = city,
        reference = reference,
        magnitude = magnitude,
        depth = depth,
        scale = scale,
        latitude = latitude,
        longitude = longitude,
        isSensitive = sensible == "1",
        isVerified = state == "verificado"
    )
}
