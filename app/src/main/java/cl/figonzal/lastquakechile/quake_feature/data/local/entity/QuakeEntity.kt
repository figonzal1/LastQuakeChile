package cl.figonzal.lastquakechile.quake_feature.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class QuakeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val quakeCode: Int,
    val utcDate: String,
    val city: String,
    var reference: String,
    val magnitude: Double,
    val depth: Double,
    val scale: String,
    val isSensitive: Boolean,
    val isVerified: Boolean
)