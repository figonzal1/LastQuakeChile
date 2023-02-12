package cl.figonzal.lastquakechile.reports_feature.domain.repository

import cl.figonzal.lastquakechile.core.data.remote.StatusAPI
import cl.figonzal.lastquakechile.reports_feature.domain.model.Report
import kotlinx.coroutines.flow.Flow

/**
 * Interface for repository
 */
interface ReportRepository {

    fun getReports(pageIndex: Int): Flow<StatusAPI<List<Report>>>
    fun getFirstPage(pageIndex: Int): Flow<StatusAPI<List<Report>>>
    fun getNextPages(pageIndex: Int): Flow<StatusAPI<List<Report>>>

}