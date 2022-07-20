package cl.figonzal.lastquakechile.reports_feature.data.repository

import android.app.Application
import cl.figonzal.lastquakechile.R
import cl.figonzal.lastquakechile.core.data.remote.ApiError
import cl.figonzal.lastquakechile.core.data.remote.NewStatusAPI
import cl.figonzal.lastquakechile.core.utils.SharedPrefUtil
import cl.figonzal.lastquakechile.core.utils.localDateTimeToString
import cl.figonzal.lastquakechile.core.utils.stringToLocalDateTime
import cl.figonzal.lastquakechile.core.utils.toReportDomain
import cl.figonzal.lastquakechile.reports_feature.data.local.ReportLocalDataSource
import cl.figonzal.lastquakechile.reports_feature.data.remote.ReportRemoteDataSource
import cl.figonzal.lastquakechile.reports_feature.domain.model.Report
import cl.figonzal.lastquakechile.reports_feature.domain.repository.ReportRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException
import java.time.LocalDateTime

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

    override fun getReports(): Flow<NewStatusAPI<List<Report>>> = flow {

        var cacheList = localDataSource.getReports().toReportDomain()

        when {
            cacheList.isNotEmpty() && !isCacheExpired() -> {

                Timber.d(application.getString(R.string.EMIT_CACHE_LIST))

                emit(NewStatusAPI.Success(cacheList))
            }
            else -> try {

                //Network call
                val reports = remoteDataSource.getReports()

                if (!reports.isNullOrEmpty()) {

                    localDataSource.deleteAll()

                    reports
                        .onEach { localDataSource.insert(it) }
                        .toReportDomain()
                        .also {
                            //Save timestamp
                            sharedPrefUtil.saveData(
                                application.getString(R.string.shared_report_cache),
                                LocalDateTime.now().localDateTimeToString()
                            )
                        }

                    Timber.d(application.getString(R.string.LIST_NETWORK_CALL))

                    //emit cached
                    cacheList = localDataSource.getReports().toReportDomain()
                    emit(NewStatusAPI.Success(cacheList))
                }

            } catch (e: HttpException) {

                Timber.e(application.getString(R.string.EMIT_HTTP_ERROR))

                emit(NewStatusAPI.Error(ApiError.HttpError))
            } catch (e: IOException) {

                Timber.e(application.getString(R.string.EMIT_IO_EXCEPTION))

                emit(NewStatusAPI.Error(ApiError.IoError))
            }
        }

    }.flowOn(ioDispatcher)

    private fun isCacheExpired(): Boolean {

        val sharedTimeCached = sharedPrefUtil.getData(
            application.getString(R.string.shared_report_cache),
            LocalDateTime.now().minusDays(2).localDateTimeToString()
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
