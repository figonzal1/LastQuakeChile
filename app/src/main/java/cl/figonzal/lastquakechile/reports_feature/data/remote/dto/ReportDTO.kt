package cl.figonzal.lastquakechile.reports_feature.data.remote.dto

import cl.figonzal.lastquakechile.model.QuakesCity
import cl.figonzal.lastquakechile.reports_feature.domain.model.Report

/**
 * Here make changes if API for reports changes
 */
data class ReportDTO(
    private val mes_reporte: String,
    private val n_sensible: Int,
    private val n_sismos: Int,
    private val prom_magnitud: Double,
    private val prom_profundidad: Double,
    private val max_magnitude: Double,
    private val min_profundidad: Double,
    private val top_ciudades: List<QuakesCity>

) {

    fun toDomainReport(): Report {

        return Report(
            reportMonth = mes_reporte,
            nSensitive = n_sensible,
            nQuakes = n_sismos,
            promMagnitud = prom_magnitud,
            promDepth = prom_profundidad,
            maxMagnitude = max_magnitude,
            minDepth = min_profundidad,
            topCities = top_ciudades
        )
    }
}