package cl.figonzal.lastquakechile.quake_feature.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Quake(
    val quakeCode: Int,
    val localDate: String,
    val city: String,
    val reference: String,
    val magnitude: Double,
    val depth: Double,
    val scale: String,
    val isSensitive: Boolean,
    val isVerified: Boolean,
    val coordinate: Coordinate
) : Parcelable