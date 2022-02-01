package cl.figonzal.lastquakechile.reports_feature.domain.repository

import cl.figonzal.lastquakechile.reports_feature.domain.model.Report
import cl.figonzal.lastquakechile.reports_feature.utils.Resource
import kotlinx.coroutines.flow.Flow

/**
 * Interface for repository
 */
interface ReportRepository {

    fun getReports(): Flow<Resource<List<Report>>>
}