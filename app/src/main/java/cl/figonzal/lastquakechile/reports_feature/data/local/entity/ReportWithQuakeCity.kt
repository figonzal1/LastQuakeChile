package cl.figonzal.lastquakechile.reports_feature.data.local.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Relation
import cl.figonzal.lastquakechile.reports_feature.domain.model.Report

@Entity
data class ReportWithQuakeCity(
    @Embedded
    val report: ReportEntity,

    @Relation(
        parentColumn = "id", entityColumn = "idReport", entity = QuakeCityEntity::class
    )
    val topCities: List<QuakeCityEntity>
) {
    fun toDomainReport(): Report {
        return Report(
            reportMonth = report.reportMonth,
            nSensitive = report.nSensitive,
            nQuakes = report.nQuakes,
            promMagnitud = report.promMagnitud,
            promDepth = report.promDepth,
            maxMagnitude = report.maxMagnitude,
            minDepth = report.minDepth,
            topCities = topCities.map {
                it.toDomainQuakeCity()
            }
        )
    }
}