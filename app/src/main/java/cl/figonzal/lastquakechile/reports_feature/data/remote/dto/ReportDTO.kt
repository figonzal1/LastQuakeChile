package cl.figonzal.lastquakechile.reports_feature.data.remote.dto

import cl.figonzal.lastquakechile.reports_feature.data.local.entity.ReportEntity
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Here make changes if API for reports changes too
 */
@JsonClass(generateAdapter = true)
data class ReportDTO(

    @param:Json(name = "reportMonth")
    val reportMonth: String,

    @param:Json(name = "promMagnitude")
    val meanMagnitude: Double,

    @param:Json(name = "promDepth")
    val meanDepth: Double,

    @param:Json(name = "maxMagnitude")
    val maxMagnitude: Double,

    @param:Json(name = "minDepth")
    val minDepth: Double,

    @param:Json(name = "nsensitive")
    val nSensitives: Int,

    @param:Json(name = "nquakes")
    val nQuakes: Int,

    @param:Json(name = "cityQuakes")
    val cityQuakes: List<CityQuakesDTO>,
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