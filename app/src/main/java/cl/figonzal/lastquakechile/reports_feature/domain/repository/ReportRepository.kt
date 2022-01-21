package cl.figonzal.lastquakechile.reports_feature.domain.repository

import cl.figonzal.lastquakechile.reports_feature.domain.model.Report
import kotlinx.coroutines.flow.Flow

/**
 * Interface for repository
 */
interface ReportRepository {

    fun getReports(): Flow<List<Report>>
}