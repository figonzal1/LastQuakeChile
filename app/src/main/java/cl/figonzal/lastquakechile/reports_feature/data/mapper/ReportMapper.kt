package cl.figonzal.lastquakechile.reports_feature.data.mapper

import cl.figonzal.lastquakechile.reports_feature.data.local.entity.relation.ReportWithCityQuakes
import cl.figonzal.lastquakechile.reports_feature.data.remote.dto.ReportDTO

fun List<ReportDTO>.toReportListEntity() = map { reportDTO ->
    ReportWithCityQuakes(
        report = reportDTO.toEntity(),
        cityQuakes = reportDTO.cityQuakes.map { it.toEntity() }
    )
}

fun List<ReportWithCityQuakes>.toReportListDomain() = map { it.toDomain() }
