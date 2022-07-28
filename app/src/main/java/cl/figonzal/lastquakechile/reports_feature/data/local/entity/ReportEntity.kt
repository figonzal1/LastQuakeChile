package cl.figonzal.lastquakechile.reports_feature.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ReportEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val reportMonth: String,
    val nSensitive: Int,
    val nQuakes: Int,
    val promMagnitude: Double,
    val promDepth: Double,
    val maxMagnitude: Double,
    val minDepth: Double
)