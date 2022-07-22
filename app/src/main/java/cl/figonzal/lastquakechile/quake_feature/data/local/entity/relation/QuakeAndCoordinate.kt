package cl.figonzal.lastquakechile.quake_feature.data.local.entity.relation

import androidx.room.Embedded
import androidx.room.Relation
import cl.figonzal.lastquakechile.quake_feature.data.local.entity.CoordinateEntity
import cl.figonzal.lastquakechile.quake_feature.data.local.entity.QuakeEntity
import cl.figonzal.lastquakechile.quake_feature.domain.model.Coordinate
import cl.figonzal.lastquakechile.quake_feature.domain.model.Quake
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

data class QuakeAndCoordinate(

    @Embedded
    var quakeEntity: QuakeEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "quakeId",
        entity = CoordinateEntity::class
    )
    val coordinateEntity: CoordinateEntity

) {
    fun toDomain(): Quake {

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

        return Quake(
            quakeCode = quakeEntity.quakeCode,
            localDate = LocalDateTime.parse(quakeEntity.utcDate, formatter).atZone(ZoneOffset.UTC)
                .withZoneSameInstant(
                    ZoneId.systemDefault()
                ).toLocalDateTime(),
            city = quakeEntity.city,
            reference = quakeEntity.reference,
            magnitude = quakeEntity.magnitude,
            depth = quakeEntity.depth,
            scale = quakeEntity.scale,
            isSensitive = quakeEntity.isSensitive,
            isVerified = quakeEntity.isVerified,
            coordinate = Coordinate(coordinateEntity.latitude, coordinateEntity.longitude)
        )
    }
}