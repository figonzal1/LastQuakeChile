package cl.figonzal.lastquakechile.reports_feature.data.remote.dto

import cl.figonzal.lastquakechile.reports_feature.data.local.entity.CityQuakesEntity
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Here make changes if API for reports changes too
 */
@JsonClass(generateAdapter = true)
data class CityQuakesDTO(

    @Json(name = "city")
    val city: String,

    @Json(name = "nquakes")
    val nQuakes: Int,
) {
    fun toEntity() = CityQuakesEntity(
        city = city,
        nQuakes = nQuakes
    )
}