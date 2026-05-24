package cl.figonzal.lastquakechile.reports_feature.data.repository

import cl.figonzal.lastquakechile.core.data.remote.toDomainError
import cl.figonzal.lastquakechile.core.domain.DomainError
import cl.figonzal.lastquakechile.core.domain.DomainResult
import cl.figonzal.lastquakechile.reports_feature.data.local.ReportLocalDataSource
import cl.figonzal.lastquakechile.reports_feature.data.local.entity.relation.ReportWithCityQuakes
import cl.figonzal.lastquakechile.reports_feature.data.mapper.toReportListDomain
import cl.figonzal.lastquakechile.reports_feature.data.mapper.toReportListEntity
import cl.figonzal.lastquakechile.reports_feature.data.remote.ReportRemoteDataSource
import cl.figonzal.lastquakechile.reports_feature.domain.model.Report
import cl.figonzal.lastquakechile.reports_feature.domain.repository.ReportRepository
import com.skydoves.sandwich.message
import com.skydoves.sandwich.retrofit.statusCode
import com.skydoves.sandwich.suspendOnError
import com.skydoves.sandwich.suspendOnFailure
import com.skydoves.sandwich.suspendOnSuccess
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import timber.log.Timber

class ReportRepositoryImpl(
    private val localDataSource: ReportLocalDataSource,
    private val remoteDataSource: ReportRemoteDataSource,
    private val dispatcher: CoroutineDispatcher
) : ReportRepository {

    override fun getReports(pageIndex: Int) = when (pageIndex) {
        0 -> getFirstPage(pageIndex)
        else -> getNextPages(pageIndex)
    }

    override fun getFirstPage(pageIndex: Int): Flow<DomainResult<List<Report>>> = flow {

        var cacheList = localDataSource.getReports()

        remoteDataSource.getReports(pageIndex)
            .suspendOnSuccess {
                when {
                    data.isNotEmpty() -> {
                        val reports = data.toReportListEntity()

                        localDataSource.deleteAll()
                        saveToLocalReports(reports)

                        cacheList = localDataSource.getReports()

                        emit(DomainResult.Success(cacheList))
                        Timber.d("List updated with network call")
                    }

                    else -> {
                        val error =
                            if (cacheList.isEmpty()) DomainError.EmptyList else DomainError.NoMoreData
                        emit(DomainResult.Error(cacheList, error))
                    }
                }
            }
            .suspendOnError {
                Timber.e("Suspend error: ${this.message()}")
                emit(DomainResult.Error(cacheList, statusCode.toDomainError()))
            }
            .suspendOnFailure {
                Timber.e("Suspend failure: ${this.message()}")
                emit(DomainResult.Error(cacheList, message().toDomainError()))
            }
    }.flowOn(dispatcher)

    override fun getNextPages(pageIndex: Int): Flow<DomainResult<List<Report>>> = flow {

        val emptyList = emptyList<Report>()

        remoteDataSource.getReports(pageIndex)
            .suspendOnSuccess {
                when {
                    data.isNotEmpty() -> {
                        val reports = data
                            .toReportListEntity()
                            .toReportListDomain()

                        emit(DomainResult.Success(reports))
                        Timber.d("List updated with network call")
                    }

                    else -> emit(DomainResult.Error(emptyList, DomainError.NoMoreData))
                }
            }
            .suspendOnError {
                Timber.e("Suspend error: ${this.message()}")
                emit(DomainResult.Error(emptyList, statusCode.toDomainError()))
            }
            .suspendOnFailure {
                Timber.e("Suspend failure: ${this.message()}")
                emit(DomainResult.Error(emptyList, message().toDomainError()))
            }
    }.flowOn(dispatcher)

    private fun saveToLocalReports(report: List<ReportWithCityQuakes>) {
        report.forEach { localDataSource.insert(it) }
    }
}
