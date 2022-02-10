package cl.figonzal.lastquakechile.quake_feature.domain.model

import java.io.Serializable

data class Coordinates(
    val latitude: Double,
    val longitude: Double
) : Serializable
