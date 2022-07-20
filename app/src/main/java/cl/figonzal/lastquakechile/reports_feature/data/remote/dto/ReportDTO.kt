package cl.figonzal.lastquakechile.reports_feature.data.remote.dto

import cl.figonzal.lastquakechile.reports_feature.data.local.entity.ReportEntity
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Here make changes if API for reports changes too
 */
@JsonClass(generateAdapter = true)
data class ReportDTO(

    @Json(name = "reportMonth")
    val reportMonth: String,

    @Json(name = "promMagnitude")
    val meanMagnitude: Double,

    @Json(name = "promDepth")
    val meanDepth: Double,

    @Json(name = "maxMagnitude")
    val maxMagnitude: Double,

    @Json(name = "minDepth")
    val minDepth: Double,

    @Json(name = "nsensitive")
    val nSensitives: Int,

    @Json(name = "nquakes")
    val nQuakes: Int,

    @Json(name = "cityQuakes")
    val cityQuakes: List<CityQuakesDTO>,

    @Json(name = "_links", ignore = true)
    val links: Any? = null
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