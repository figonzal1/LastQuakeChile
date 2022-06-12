package cl.figonzal.lastquakechile.quake_feature.data.remote.dto

import androidx.annotation.Keep
import androidx.room.Ignore
import cl.figonzal.lastquakechile.quake_feature.data.local.entity.QuakeEntity

/**
 * Here make changes if API for quakes changes too
 */
@Keep
data class QuakeDTO(
    @Ignore
    private val fecha_local: String? = null,
    private val fecha_utc: String,
    private val ciudad: String,
    private val referencia: String,
    private val magnitud: Double,
    private val escala: String,
    private val sensible: String,
    private val latitud: Double,
    private val longitud: Double,
    private val profundidad: Double,
    @Ignore
    private val agencia: String? = null,
    private val imagen_url: String? = null,
    private val estado: String
) {

    //DTO -> Entity -> Model domain
    fun toQuakeEntity() = QuakeEntity(
        quakeCode = imagen_url?.toInt(),
        utcDate = fecha_utc,
        city = ciudad,
        reference = referencia,
        magnitude = magnitud,
        depth = profundidad,
        scale = escala,
        latitude = latitud,
        longitude = longitud,
        isSensitive = sensible == "1",
        isVerified = estado == "verificado"
    )
}
