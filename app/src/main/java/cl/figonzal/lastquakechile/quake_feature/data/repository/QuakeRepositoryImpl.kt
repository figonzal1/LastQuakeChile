package cl.figonzal.lastquakechile.quake_feature.data.repository

import android.app.Application
import cl.figonzal.lastquakechile.R
import cl.figonzal.lastquakechile.core.utils.StatusAPI
import cl.figonzal.lastquakechile.quake_feature.data.local.QuakeLocalDataSource
import cl.figonzal.lastquakechile.quake_feature.data.remote.QuakeRemoteDataSource
import cl.figonzal.lastquakechile.quake_feature.domain.model.Quake
import cl.figonzal.lastquakechile.quake_feature.domain.repository.QuakeRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException

class QuakeRepositoryImpl(
    private val localDataSource: QuakeLocalDataSource,
    private val remoteDataSource: QuakeRemoteDataSource,
    private val dispatcher: CoroutineDispatcher,
    private val application: Application
) : QuakeRepository {


    override fun getQuakes(limit: Int): Flow<StatusAPI<List<Quake>>> = flow {

        emit(StatusAPI.Loading())

        var cacheList = localDataSource.getQuakes().map { it.toDomainQuake() }
        emit(StatusAPI.Success(cacheList))

        try {
            localDataSource.deleteAll()
            remoteDataSource.getQuakes(limit)?.onEach {
                //store remote result in cache
                localDataSource.insert(it)

            }?.map { it.toDomainQuake() }

            cacheList = localDataSource.getQuakes().map { it.toDomainQuake() }

            Timber.d(application.getString(R.string.LIST_NETWORK_CALL))

            emit(StatusAPI.Success(cacheList))

        } catch (e: HttpException) {

            Timber.e(application.getString(R.string.EMIT_HTTP_ERROR))

            emit(
                StatusAPI.Error(
                    message = application.getString(R.string.http_error)
                )
            )
        } catch (e: IOException) {

            Timber.e(application.getString(R.string.EMIT_IO_EXCEPTION))

            emit(
                StatusAPI.Error(
                    message = application.getString(R.string.io_error)
                )
            )
        }

    }.flowOn(dispatcher)
}