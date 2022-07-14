package cl.figonzal.lastquakechile.reports_feature.data.remote.dto

import cl.figonzal.lastquakechile.reports_feature.data.local.entity.ReportEntity
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Here make changes if API for reports changes too
 */
@JsonClass(generateAdapter = true)
data class ReportDTO(

    @field:Json(name = "mes_reporte")
    val reportMonth: String,

    @field:Json(name = "n_sensibles")
    val nSensitives: Int,

    @field:Json(name = "n_sismos")
    val nQuakes: Int,

    @field:Json(name = "prom_magnitud")
    val meanMagnitude: Double,

    @field:Json(name = "prom_profundidad")
    val meanDepth: Double,

    @field:Json(name = "max_magnitud")
    val maxMagnitude: Double,

    @field:Json(name = "min_profundidad")
    val minDepth: Double,

    @field:Json(name = "top_ciudades")
    val topCities: List<CityQuakesDTO>

) {

    fun toEntity() = ReportEntity(
        reportMonth = reportMonth,
        nSensitive = nSensitives,
        nQuakes = nQuakes,
        promMagnitude = meanMagnitude,
        promDepth = meanDepth,
        maxMagnitude = maxMagnitude,
        minDepth = minDepth
    )
}