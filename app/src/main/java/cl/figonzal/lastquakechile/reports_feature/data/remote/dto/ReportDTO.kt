package cl.figonzal.lastquakechile.reports_feature.data.remote.dto

import cl.figonzal.lastquakechile.reports_feature.data.local.entity.ReportEntity

/**
 * Here make changes if API for reports changes
 */
data class ReportDTO(
    private val mes_reporte: String,
    private val n_sensibles: Int,
    private val n_sismos: Int,
    private val prom_magnitud: Double,
    private val prom_profundidad: Double,
    private val max_magnitud: Double,
    private val min_profundidad: Double,
    val top_ciudades: List<QuakeCityDTO>

) {

    //Remember: DTO -> Entity -> Model domain

    fun toReportEntity(): ReportEntity {

        return ReportEntity(
            reportMonth = mes_reporte,
            nSensitive = n_sensibles,
            nQuakes = n_sismos,
            promMagnitud = prom_magnitud,
            promDepth = prom_profundidad,
            maxMagnitude = max_magnitud,
            minDepth = min_profundidad
        )
    }
}