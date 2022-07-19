package cl.figonzal.lastquakechile.quake_feature.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class QuakeEntity(
    @PrimaryKey val id: Long? = null,
    val quakeCode: Int,
    val utcDate: String,
    val city: String,
    val reference: String,
    val magnitude: Double,
    val depth: Double,
    val scale: String,
    val isSensitive: Boolean,
    val isVerified: Boolean
)