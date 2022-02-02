package cl.figonzal.lastquakechile.reports_feature.data.repository

import cl.figonzal.lastquakechile.reports_feature.data.remote.ReportRemoteDataSource
import cl.figonzal.lastquakechile.reports_feature.domain.model.Report
import cl.figonzal.lastquakechile.reports_feature.domain.repository.ReportRepository
import cl.figonzal.lastquakechile.reports_feature.utils.Resource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class ReportRepositoryImpl(
    private val remoteDataSource: ReportRemoteDataSource,
    private val dispatcher: CoroutineDispatcher
) : ReportRepository {

    override fun getReports(): Flow<Resource<List<Report>>> = flow {
        emit(remoteDataSource.getReports())
    }.flowOn(dispatcher)

}