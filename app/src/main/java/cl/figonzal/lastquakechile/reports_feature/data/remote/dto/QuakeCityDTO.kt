package cl.figonzal.lastquakechile.reports_feature.data.remote.dto

import cl.figonzal.lastquakechile.reports_feature.data.local.entity.QuakeCityEntity

/**
 * Here make changes if API for reports changes
 */
data class QuakeCityDTO(
    private val ciudad: String,
    private val n_sismos: Int
) {
    fun toQuakeCityEntity(): QuakeCityEntity {

        return QuakeCityEntity(
            city = ciudad,
            nQuakes = n_sismos
        )
    }
}