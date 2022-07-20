package cl.figonzal.lastquakechile.core.utils

import cl.figonzal.lastquakechile.quake_feature.data.local.entity.relation.QuakeAndCoordinate
import cl.figonzal.lastquakechile.quake_feature.data.remote.dto.QuakeDTO
import cl.figonzal.lastquakechile.reports_feature.data.local.entity.CityQuakesEntity
import cl.figonzal.lastquakechile.reports_feature.data.local.entity.relation.ReportWithCityQuakes
import cl.figonzal.lastquakechile.reports_feature.data.remote.dto.ReportDTO

/**
 * Function that map dto list to full quake entity
 */
fun List<QuakeDTO>.toQuakeListEntity() = map {
    val quakeEntity = it.toEntity()
    val coordinateEntity = it.coordinate.toEntity()

    QuakeAndCoordinate(
        quakeEntity,
        coordinateEntity
    )
}

/**
 * Function that map full quake entity to domain model
 */
fun List<QuakeAndCoordinate>.toQuakeDomain() = map { it.toDomain() }

/**
 * Function that map report dto to entity
 */
fun List<ReportDTO>.toReportEntity() = map { reportDTO ->
    val reportEntity = reportDTO.toEntity()
    val topCities = reportDTO.cityQuakes.map { it.toEntity() }

    ReportWithCityQuakes(
        report = reportEntity,
        cityQuakes = topCities
    )
}

/**
 * Function that map cityQuakeEntity to domain
 */
fun List<CityQuakesEntity>.toCityQuakesDomain() = map { it.toDomain() }

/**
 * Function that map reportWithCityQuakes to domain
 */
fun List<ReportWithCityQuakes>.toReportDomain() = map { it.toDomain() }