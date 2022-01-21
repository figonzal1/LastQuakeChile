package cl.figonzal.lastquakechile.reports_feature.domain.use_case

import cl.figonzal.lastquakechile.reports_feature.domain.model.Report
import cl.figonzal.lastquakechile.reports_feature.domain.repository.ReportRepository
import kotlinx.coroutines.flow.Flow

class GetReportsUseCase(
    private val report: ReportRepository
) {

    operator fun invoke(): Flow<List<Report>> {
        return report.getReports()
    }

}