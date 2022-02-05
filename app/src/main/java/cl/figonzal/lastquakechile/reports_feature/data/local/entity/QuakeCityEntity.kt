package cl.figonzal.lastquakechile.reports_feature.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import cl.figonzal.lastquakechile.model.QuakesCity

@Entity
data class QuakeCityEntity(
    @PrimaryKey val id: Long? = null,
    val city: String,
    val nQuakes: Int,
    var idReport: Long? = null
) {

    //TODO: Mejorar
    fun toDomainQuakeCity(): QuakesCity {
        val qC = QuakesCity()

        qC.n_sismos = nQuakes
        qC.ciudad = city

        return qC
    }
}