package cl.figonzal.lastquakechile.quake_feature.data.remote.dto

import androidx.annotation.Keep
import cl.figonzal.lastquakechile.quake_feature.data.local.entity.QuakeEntity
import com.google.gson.annotations.SerializedName

/**
 * Here make changes if API for quakes changes too
 */
@Keep
data class QuakeDTO(


    @SerializedName("fecha_utc")
    private val utcDate: String,

    @SerializedName("ciudad")
    private val city: String,

    @SerializedName("referencia")
    private val reference: String,

    @SerializedName("magnitud")
    private val magnitude: Double,

    @SerializedName("escala")
    private val scale: String,

    @SerializedName("sensible")
    private val sensible: String,

    @SerializedName("latitud")
    private val latitude: Double,

    @SerializedName("longitud")
    private val longitude: Double,

    @SerializedName("profundidad")
    private val depth: Double,

    @SerializedName("imagen_url")
    private val quakeCode: String? = null,

    @SerializedName("estado")
    private val state: String,

    //NOT USED
    @SerializedName("fecha_local")
    private val localDate: String? = null,
    @SerializedName("agencia")
    private val agency: String? = null
) {

    //DTO -> Entity -> Model domain
    fun toQuakeEntity() = QuakeEntity(
        quakeCode = quakeCode?.toInt(),
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
