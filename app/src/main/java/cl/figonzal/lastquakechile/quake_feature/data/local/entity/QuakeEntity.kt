package cl.figonzal.lastquakechile.quake_feature.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import cl.figonzal.lastquakechile.quake_feature.domain.model.Coordinates
import cl.figonzal.lastquakechile.quake_feature.domain.model.Quake
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Entity
class QuakeEntity(
    @PrimaryKey val id: Long? = null,
    val localDate: String,
    val city: String,
    val reference: String,
    val magnitude: Double,
    val depth: Double,
    val scale: String,
    val latitude: Double,
    val longitude: Double,
    val isSensitive: Boolean = false,
    val isVerified: Boolean = false
) {
    fun toDomainQuake(): Quake {

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

        return Quake(
            localDate = LocalDateTime.parse(localDate, formatter),
            city = city,
            reference = reference,
            magnitude = magnitude,
            depth = depth,
            scale = scale,
            coordinates = Coordinates(latitude, longitude),
            isSensitive = isSensitive,
            isVerified = isVerified
        )
    }
}