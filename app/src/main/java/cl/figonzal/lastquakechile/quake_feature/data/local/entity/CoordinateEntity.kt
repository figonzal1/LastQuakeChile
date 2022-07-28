package cl.figonzal.lastquakechile.quake_feature.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class CoordinateEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val latitude: Double,
    val longitude: Double,
    var quakeId: Long? = null
)
