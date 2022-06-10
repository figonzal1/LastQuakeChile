package cl.figonzal.lastquakechile.reports_feature.domain.use_case

import cl.figonzal.lastquakechile.core.utils.Resource
import cl.figonzal.lastquakechile.reports_feature.domain.model.Report
import cl.figonzal.lastquakechile.reports_feature.domain.repository.ReportRepository
import kotlinx.coroutines.flow.Flow

class GetReportsUseCase(
    private val repository: ReportRepository
) {

    operator fun invoke(): Flow<Resource<List<Report>>> {
        return repository.getReports()
    }

}