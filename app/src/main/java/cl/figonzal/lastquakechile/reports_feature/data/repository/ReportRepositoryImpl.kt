package cl.figonzal.lastquakechile.reports_feature.data.repository

import android.app.Application
import cl.figonzal.lastquakechile.R
import cl.figonzal.lastquakechile.core.Resource
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

/**
 * Single source of truth for reports
 */
class ReportRepositoryImpl(
    private val localDataSource: ReportLocalDataSource,
    private val remoteDataSource: ReportRemoteDataSource,
    private val ioDispatcher: CoroutineDispatcher,
    private val application: Application
) : ReportRepository {

    override fun getReports(): Flow<Resource<List<Report>>> = flow {

        emit(Resource.Loading())

        var cacheList = localDataSource.getReports().map { it.toDomainReport() }

        when {
            cacheList.isNotEmpty() -> {

                Timber.d(application.getString(R.string.EMIT_CACHE_LIST))

                emit(Resource.Success(cacheList))
            }
            else -> try {

                //Network call
                remoteDataSource.getReports().onEach {
                    // store remote results in cache
                    localDataSource.insert(it)

                }.map { it.toDomainReport() }

                Timber.d(application.getString(R.string.LIST_NETWORK_CALL))

                cacheList = localDataSource.getReports().map { it.toDomainReport() }
                emit(Resource.Success(cacheList))

            } catch (e: HttpException) {

                Timber.e(application.getString(R.string.EMIT_HTTP_ERROR))

                emit(
                    Resource.Error(
                        data = emptyList(),
                        message = application.getString(R.string.http_error)
                    )
                )
            } catch (e: IOException) {

                Timber.e(application.getString(R.string.EMIT_IO_EXCEPTION))

                emit(
                    Resource.Error(
                        data = emptyList(),
                        message = application.getString(R.string.io_error)
                    )
                )
            }
        }

    }.flowOn(ioDispatcher)
}