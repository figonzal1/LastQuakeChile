package cl.figonzal.lastquakechile.core.utils

import cl.figonzal.lastquakechile.quake_feature.data.local.entity.relation.QuakeAndCoordinate
import cl.figonzal.lastquakechile.quake_feature.data.remote.dto.QuakeDTO
import cl.figonzal.lastquakechile.reports_feature.data.local.entity.CityQuakesEntity
import cl.figonzal.lastquakechile.reports_feature.data.local.entity.ReportWithCityQuakesEntity
import cl.figonzal.lastquakechile.reports_feature.data.remote.dto.CityQuakesDTO
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
fun List<QuakeAndCoordinate>.toQuakeDomain() = map {
    it.toDomain()
}

/**
 * REPORTS
 */
fun List<CityQuakesDTO>.toCityQuakesEntity() = map { it.toEntity() }
fun List<ReportDTO>.toReportEntity() = map {
    val reportEntity = it.toEntity()
    val topCities = it.topCities.toCityQuakesEntity()

    ReportWithCityQuakesEntity(
        report = reportEntity,
        topCities = topCities
    )
}

fun List<CityQuakesEntity>.toCityQuakesDomain() = map { it.toDomain() }
fun List<ReportWithCityQuakesEntity>.toReportDomain() = map {
    it.toDomain()
}