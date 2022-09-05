package cl.figonzal.lastquakechile.quake_feature.data.local.entity.relation

import androidx.room.Embedded
import androidx.room.Relation
import cl.figonzal.lastquakechile.core.utils.localDateTimeToString
import cl.figonzal.lastquakechile.core.utils.stringToLocalDateTime
import cl.figonzal.lastquakechile.core.utils.utcToLocalDate
import cl.figonzal.lastquakechile.quake_feature.data.local.entity.CoordinateEntity
import cl.figonzal.lastquakechile.quake_feature.data.local.entity.QuakeEntity
import cl.figonzal.lastquakechile.quake_feature.domain.model.Coordinate
import cl.figonzal.lastquakechile.quake_feature.domain.model.Quake

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
    fun toDomain() = Quake(
        quakeCode = quakeEntity.quakeCode,
        localDate = quakeEntity.utcDate.stringToLocalDateTime()
            .utcToLocalDate()
            .localDateTimeToString(),
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
