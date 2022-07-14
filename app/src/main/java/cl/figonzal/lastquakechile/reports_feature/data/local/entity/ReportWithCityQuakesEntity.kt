package cl.figonzal.lastquakechile.reports_feature.data.local.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Relation
import cl.figonzal.lastquakechile.core.utils.toCityQuakesDomain
import cl.figonzal.lastquakechile.reports_feature.domain.model.Report

@Entity
data class ReportWithCityQuakesEntity(
    @Embedded
    val report: ReportEntity,

    @Relation(
        parentColumn = "id", entityColumn = "idReport", entity = CityQuakesEntity::class
    )
    val topCities: List<CityQuakesEntity>
) {
    fun toDomain() = Report(
        reportMonth = report.reportMonth,
        nSensitive = report.nSensitive,
        nQuakes = report.nQuakes,
        promMagnitude = report.promMagnitude,
        promDepth = report.promDepth,
        maxMagnitude = report.maxMagnitude,
        minDepth = report.minDepth,
        topCities = topCities.toCityQuakesDomain()
    )
}