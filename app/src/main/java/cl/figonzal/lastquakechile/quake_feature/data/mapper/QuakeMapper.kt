package cl.figonzal.lastquakechile.quake_feature.data.mapper

import cl.figonzal.lastquakechile.quake_feature.data.local.entity.relation.QuakeAndCoordinate
import cl.figonzal.lastquakechile.quake_feature.data.remote.dto.QuakeDTO
import java.util.Locale

fun List<QuakeDTO>.toQuakeListEntity() = map {
    QuakeAndCoordinate(it.toEntity(), it.coordinate.toEntity())
}

fun List<QuakeAndCoordinate>.toQuakeListDomain() = mapNotNull { it.toDomain() }

fun List<QuakeAndCoordinate>.translateReference(): List<QuakeAndCoordinate> {
    if (Locale.getDefault().language == "en") {
        onEach { quake ->
            val split = quake.quakeEntity.reference
                .trim()
                .split(" ")
                .toMutableList()
                .apply {
                    removeAt(2)
                    this[2] = when (this[2]) {
                        "O" -> "W"
                        "NO" -> "NW"
                        "SO" -> "SW"
                        else -> this[2]
                    }
                    this[3] = "of"
                }
            quake.quakeEntity.reference = split.joinToString(separator = " ")
        }
    }
    return this
}
