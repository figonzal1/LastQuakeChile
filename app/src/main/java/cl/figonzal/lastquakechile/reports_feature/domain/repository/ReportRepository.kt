package cl.figonzal.lastquakechile.reports_feature.domain.repository

import cl.figonzal.lastquakechile.core.domain.DomainResult
import cl.figonzal.lastquakechile.reports_feature.domain.model.Report
import kotlinx.coroutines.flow.Flow

interface ReportRepository {
    fun getReports(pageIndex: Int): Flow<DomainResult<List<Report>>>
}
