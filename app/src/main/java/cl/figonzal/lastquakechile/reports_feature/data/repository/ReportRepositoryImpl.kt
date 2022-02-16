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
    private val dispatcher: CoroutineDispatcher,
    private val application: Application
) : ReportRepository {

    override fun getReports(): Flow<Resource<List<Report>>> = flow {

        emit(Resource.Loading())

        var cacheList = localDataSource.getReports().map { it.toDomainReport() }

        when {
            cacheList.isNotEmpty() -> {

                Timber.d("Emit cached reportList")

                emit(Resource.Success(cacheList))
            }
            else -> try {

                //Network call
                remoteDataSource.getReports().onEach {
                    // store remote results in cache
                    localDataSource.insert(it)

                }.map { it.toDomainReport() }

                Timber.d("Emit network reportList")

                cacheList = localDataSource.getReports().map { it.toDomainReport() }
                emit(Resource.Success(cacheList))

            } catch (e: HttpException) {

                Timber.e("Emit http error")

                emit(
                    Resource.Error(
                        data = emptyList(),
                        message = application.getString(R.string.HTTP_ERROR)
                    )
                )
            } catch (e: IOException) {

                Timber.e("Emit ioexception")

                emit(
                    Resource.Error(
                        data = emptyList(),
                        message = application.getString(R.string.IO_ERROR)
                    )
                )
            }
        }

    }.flowOn(dispatcher)
}