package cl.figonzal.lastquakechile.quake_feature.data.remote.dto

import cl.figonzal.lastquakechile.quake_feature.domain.model.Coordinates
import cl.figonzal.lastquakechile.quake_feature.domain.model.Quake
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Here make changes if API for reports changes
 */
data class QuakeDTO(
    private val fecha_local: String,
    private val fecha_utc: String,
    private val ciudad: String,
    private val referencia: String,
    private val magnitud: Double,
    private val escala: String,
    private val sensible: String,
    private val latitud: Double,
    private val longitud: Double,
    private val profundidad: Double,
    private val agencia: String,
    private val imagen_url: String,
    private val estado: String
) {

    //DTO -> Entity -> MOdel domain
    //TODO: Make entity

    fun toDomainQuake(): Quake {

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

        return Quake(
            localDate = LocalDateTime.parse(fecha_local, formatter),
            city = ciudad,
            reference = referencia,
            magnitude = magnitud,
            depth = profundidad,
            scale = escala,
            coordinates = Coordinates(latitud, longitud),
            isSensitive = sensible == "1",
            isVerified = estado == "Verificado"
        )
    }
}
