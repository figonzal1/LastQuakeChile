package cl.figonzal.lastquakechile.quake_feature.domain.model

import java.io.Serializable
import java.time.LocalDateTime

data class Quake(
    val quakeCode: Int,
    val localDate: LocalDateTime,
    val city: String,
    val reference: String,
    val magnitude: Double,
    val depth: Double,
    val scale: String,
    val isSensitive: Boolean,
    val isVerified: Boolean,
    val coordinate: Coordinate
) : Serializable