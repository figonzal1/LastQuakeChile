package cl.figonzal.lastquakechile.reports_feature.data.local.entity.relation

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Relation
import cl.figonzal.lastquakechile.core.utils.toCityQuakesDomain
import cl.figonzal.lastquakechile.reports_feature.data.local.entity.CityQuakesEntity
import cl.figonzal.lastquakechile.reports_feature.data.local.entity.ReportEntity
import cl.figonzal.lastquakechile.reports_feature.domain.model.Report

@Entity
data class ReportWithCityQuakes(
    @Embedded
    val report: ReportEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "reportId"
    )
    val cityQuakes: List<CityQuakesEntity>
) {
    fun toDomain() = Report(
        reportMonth = report.reportMonth,
        nSensitive = report.nSensitive,
        nQuakes = report.nQuakes,
        promMagnitude = report.promMagnitude,
        promDepth = report.promDepth,
        maxMagnitude = report.maxMagnitude,
        minDepth = report.minDepth,
        cityQuakes = cityQuakes.toCityQuakesDomain()
    )
}