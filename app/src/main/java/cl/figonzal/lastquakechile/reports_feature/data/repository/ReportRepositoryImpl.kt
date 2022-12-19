package cl.figonzal.lastquakechile.reports_feature.data.repository

import android.app.Application
import cl.figonzal.lastquakechile.core.data.remote.StatusAPI
import cl.figonzal.lastquakechile.core.utils.*
import cl.figonzal.lastquakechile.reports_feature.data.local.ReportLocalDataSource
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
import java.time.LocalDateTime

private const val SHARED_PREF_REPORT_CACHE = "reports_cache_entry"

/**
 * Single source of truth for reports
 */
class ReportRepositoryImpl(
    private val localDataSource: ReportLocalDataSource,
    private val remoteDataSource: ReportRemoteDataSource,
    private val ioDispatcher: CoroutineDispatcher,
    private val application: Application,
    private val sharedPrefUtil: SharedPrefUtil
) : ReportRepository {

    override fun getReports(pageIndex: Int): Flow<StatusAPI<List<Report>>> = flow {

        var cacheList = localDataSource.getReports().toReportDomain()

        when {
            cacheList.isNotEmpty() && !isCacheExpired() -> {

                Timber.d("Emitting cached reportList")

                emit(StatusAPI.Success(cacheList))
            }
            else -> {

                //Network call
                remoteDataSource.getReports(pageIndex)
                    .suspendOnSuccess {

                        val reports = data.embedded!!.reports.toReportEntity()

                        localDataSource.deleteAll() //delete cached

                        reports.onEach { localDataSource.insert(it) }.toReportDomain()

                        //Save timestamp
                        sharedPrefUtil.saveData(
                            key = SHARED_PREF_REPORT_CACHE,
                            value = LocalDateTime.now().localDateTimeToString()
                        )

                        Timber.d("List updated with network call")

                        //emit cached
                        cacheList = localDataSource.getReports().toReportDomain()
                        emit(StatusAPI.Success(cacheList))
                    }
                    .suspendOnError {

                        Timber.e("Suspend error: ${this.message()}")

                        val apiError = application.processApiError(message(), null)
                        emit(StatusAPI.Error(cacheList, apiError))
                    }
                    .suspendOnFailure {

                        Timber.e("Suspend failure: ${this.message()}")

                        val apiError = application.processApiError(message(), null)
                        emit(StatusAPI.Error(cacheList, apiError))
                    }
            }
        }

    }.flowOn(ioDispatcher)

    private fun isCacheExpired(): Boolean {

        val sharedTimeCached = sharedPrefUtil.getData(
            key = SHARED_PREF_REPORT_CACHE,
            defaultValue = LocalDateTime.now().minusDays(2).localDateTimeToString()
            //Return now()-2 days if timeCached not exist, to force expiration
        ) as String

        val timeNow = LocalDateTime.now()
        val timeCached = sharedTimeCached.stringToLocalDateTime().plusDays(1)

        Timber.d("Cache list until $timeCached")

        with(timeNow.isAfter(timeCached)) {

            Timber.d("Report List isCacheExpired: $this")

            return this
        }
    }
}
