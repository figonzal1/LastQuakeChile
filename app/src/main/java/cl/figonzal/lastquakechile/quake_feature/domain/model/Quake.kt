package cl.figonzal.lastquakechile.quake_feature.domain.model

import java.time.LocalDateTime

data class Quake(
    val quakeCode: Int? = null,
    val localDate: LocalDateTime,
    val city: String,
    val reference: String,
    val magnitude: Double,
    val depth: Double,
    val scale: String,
    val coordinates: Coordinates,
    val isSensitive: Boolean = false,
    val isVerified: Boolean = false,
)