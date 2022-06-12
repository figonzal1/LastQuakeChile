package cl.figonzal.lastquakechile.reports_feature.data.local.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Relation
import cl.figonzal.lastquakechile.reports_feature.domain.model.Report

@Entity
data class ReportWithQuakeCityEntity(
    @Embedded
    val report: ReportEntity,

    @Relation(
        parentColumn = "id", entityColumn = "idReport", entity = QuakeCityEntity::class
    )
    val topCities: List<QuakeCityEntity>
) {
    fun toDomainReport() = Report(
        reportMonth = report.reportMonth,
        nSensitive = report.nSensitive,
        nQuakes = report.nQuakes,
        promMagnitude = report.promMagnitude,
        promDepth = report.promDepth,
        maxMagnitude = report.maxMagnitude,
        minDepth = report.minDepth,
        topCities = topCities.map {
            it.toDomainQuakeCity()
        }
    )
}