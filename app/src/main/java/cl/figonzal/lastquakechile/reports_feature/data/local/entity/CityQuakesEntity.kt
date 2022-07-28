package cl.figonzal.lastquakechile.reports_feature.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import cl.figonzal.lastquakechile.reports_feature.domain.model.CityQuakes

@Entity
data class CityQuakesEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val city: String,
    val nQuakes: Int,
    var reportId: Long? = null
) {

    fun toDomain() = CityQuakes(
        city = city,
        nQuakes = nQuakes
    )
}