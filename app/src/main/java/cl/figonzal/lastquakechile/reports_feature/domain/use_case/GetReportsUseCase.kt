package cl.figonzal.lastquakechile.reports_feature.domain.use_case

import cl.figonzal.lastquakechile.reports_feature.domain.repository.ReportRepository

class GetReportsUseCase(private val repository: ReportRepository) {
    operator fun invoke() = repository.getReports()
}