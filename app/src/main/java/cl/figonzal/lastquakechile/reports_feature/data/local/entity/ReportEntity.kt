package cl.figonzal.lastquakechile.reports_feature.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity()
data class ReportEntity(
    @PrimaryKey val id: Long? = null,
    @ColumnInfo() val reportMonth: String,
    @ColumnInfo() val nSensitive: Int,
    @ColumnInfo() val nQuakes: Int,
    @ColumnInfo() val promMagnitud: Double,
    @ColumnInfo() val promDepth: Double,
    @ColumnInfo() val maxMagnitude: Double,
    @ColumnInfo() val minDepth: Double
)