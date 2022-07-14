package cl.figonzal.lastquakechile.reports_feature.data.remote.dto

import cl.figonzal.lastquakechile.reports_feature.data.local.entity.CityQuakesEntity
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Here make changes if API for reports changes too
 */
@JsonClass(generateAdapter = true)
data class CityQuakesDTO(

    @field:Json(name = "ciudad")
    val city: String,

    @field:Json(name = "n_sismos")
    val nQuakes: Int
) {
    fun toEntity() = CityQuakesEntity(
        city = city,
        nQuakes = nQuakes
    )
}