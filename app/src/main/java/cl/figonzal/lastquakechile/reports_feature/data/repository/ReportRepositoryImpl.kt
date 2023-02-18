package cl.figonzal.lastquakechile.reports_feature.data.repository

import android.app.Application
import cl.figonzal.lastquakechile.core.data.remote.ApiError
import cl.figonzal.lastquakechile.core.data.remote.StatusAPI
import cl.figonzal.lastquakechile.core.utils.processSandwichError
import cl.figonzal.lastquakechile.core.utils.toReportListDomain
import cl.figonzal.lastquakechile.core.utils.toReportListEntity
import cl.figonzal.lastquakechile.reports_feature.data.local.ReportLocalDataSource
import cl.figonzal.lastquakechile.reports_feature.data.local.entity.relation.ReportWithCityQuakes
import cl.figonzal.lastquakechile.reports_feature.data.remote.ReportRemoteDataSource
import cl.figonzal.lastquakechile.reports_feature.domain.model.Report
import cl.figonzal.lastquakechile.reports_feature.domain.repository.ReportRepository
import com.skydoves.sandwich.message
import com.skydoves.sandwich.suspendOnError
import com.skydoves.sandwich.suspendOnFailure
import com.skydoves.sandwich.suspendOnSuccess
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import timber.log.Timber

/**
 * Single source of truth for reports
 */
class ReportRepositoryImpl(
    private val localDataSource: ReportLocalDataSource,
    private val remoteDataSource: ReportRemoteDataSource,
    private val dispatcher: CoroutineDispatcher,
    private val application: Application
) : ReportRepository {

    override fun getReports(pageIndex: Int) = when (pageIndex) {
        0 -> getFirstPage(pageIndex)
        else -> getNextPages(pageIndex)
    }

    override fun getFirstPage(pageIndex: Int): Flow<StatusAPI<List<Report>>> = flow {

        var cacheList = localDataSource.getReports().toReportListDomain()

        remoteDataSource.getReports(pageIndex)
            .suspendOnSuccess {

                when {
                    data.embedded != null -> {
                        val quakes = data.embedded!!.reports.toReportListEntity()

                        localDataSource.deleteAll()
                        saveToLocalReports(quakes)

                        cacheList = localDataSource.getReports().toReportListDomain()

                        emit(StatusAPI.Success(cacheList))

                        Timber.d("List updated with network call")
                    }
                    else -> {
                        //First page empty, send cacheList or empty list
                        val apiError = when {
                            cacheList.isEmpty() -> ApiError.EmptyList
                            else -> ApiError.NoMoreData
                        }
                        emit(StatusAPI.Error(cacheList, apiError))
                    }
                }
            }
            .suspendOnError {

                Timber.e("Suspend error: ${this.message()}")

                val apiError = application.processSandwichError("", statusCode)
                emit(StatusAPI.Error(data = cacheList, apiError = apiError))
            }
            .suspendOnFailure {

                Timber.e("Suspend failure: ${this.message()}")

                val apiError = application.processSandwichError(message(), null)
                emit(StatusAPI.Error(data = cacheList, apiError = apiError))
            }
    }.flowOn(dispatcher)

    override fun getNextPages(pageIndex: Int): Flow<StatusAPI<List<Report>>> = flow {

        val emptyList = emptyList<Report>()

        //Get remote data
        remoteDataSource.getReports(pageIndex)
            .suspendOnSuccess {

                when {
                    data.embedded != null -> {
                        val reports = data.embedded!!.reports
                            .toReportListEntity()
                            .toReportListDomain()

                        emit(StatusAPI.Success(reports))

                        Timber.d("List updated with network call")
                    }
                    else -> {
                        val apiError = ApiError.NoMoreData
                        emit(StatusAPI.Error(emptyList, apiError))
                    }
                }
            }
            .suspendOnError {
                Timber.e("Suspend error: ${this.message()}")

                val apiError = application.processSandwichError("", null)
                emit(StatusAPI.Error(emptyList, apiError))
            }
            .suspendOnFailure {

                Timber.e("Suspend failure: ${this.message()}")

                val apiError = application.processSandwichError(message(), null)
                emit(StatusAPI.Error(emptyList, apiError))
            }
    }.flowOn(dispatcher)

    private fun saveToLocalReports(report: List<ReportWithCityQuakes>) {
        report.forEach {
            localDataSource.insert(it)
        }
    }
}
