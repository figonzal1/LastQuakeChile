package cl.figonzal.lastquakechile.reports_feature.data.repository

import cl.figonzal.lastquakechile.reports_feature.data.remote.ReportsRemoteDataSource
import cl.figonzal.lastquakechile.reports_feature.domain.model.Report
import cl.figonzal.lastquakechile.reports_feature.domain.repository.ReportRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ReportRepositoryImpl(
    private val remoteDataSource: ReportsRemoteDataSource
) : ReportRepository {

    override fun getReports(): Flow<List<Report>> = flow {
        emit(remoteDataSource.getReports())
    }

}