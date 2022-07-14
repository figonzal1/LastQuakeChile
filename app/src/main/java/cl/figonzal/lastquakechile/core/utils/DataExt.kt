package cl.figonzal.lastquakechile.core.utils

import cl.figonzal.lastquakechile.quake_feature.data.local.entity.QuakeEntity
import cl.figonzal.lastquakechile.quake_feature.data.remote.dto.QuakeDTO
import cl.figonzal.lastquakechile.reports_feature.data.local.entity.CityQuakesEntity
import cl.figonzal.lastquakechile.reports_feature.data.local.entity.ReportWithCityQuakesEntity
import cl.figonzal.lastquakechile.reports_feature.data.remote.dto.CityQuakesDTO
import cl.figonzal.lastquakechile.reports_feature.data.remote.dto.ReportDTO

/**
 * QUAKES
 */
fun List<QuakeDTO>.toQuakeListEntity() = map { it.toEntity() }
fun List<QuakeEntity>.toQuakeListDomain() = map { it.toDomain() }

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