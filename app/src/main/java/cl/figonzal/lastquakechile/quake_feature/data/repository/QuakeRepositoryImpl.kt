package cl.figonzal.lastquakechile.quake_feature.data.repository

import android.app.Application
import cl.figonzal.lastquakechile.R
import cl.figonzal.lastquakechile.core.data.remote.ApiError
import cl.figonzal.lastquakechile.core.data.remote.StatusAPI
import cl.figonzal.lastquakechile.core.utils.toQuakeDomain
import cl.figonzal.lastquakechile.quake_feature.data.local.QuakeLocalDataSource
import cl.figonzal.lastquakechile.quake_feature.data.local.entity.relation.QuakeAndCoordinate
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

        var cacheList = localDataSource.getQuakes().toQuakeDomain()

        emit(StatusAPI.Success(cacheList))

        try {

            val remoteData = remoteDataSource.getQuakes(limit)

            localDataSource.deleteAll()

            //Save to localSource
            savedLocalQuakes(remoteData)

            cacheList = localDataSource.getQuakes().toQuakeDomain()

            Timber.d(application.getString(R.string.LIST_NETWORK_CALL))

            emit(StatusAPI.Success(cacheList))

        } catch (e: HttpException) {

            Timber.e(application.getString(R.string.EMIT_HTTP_ERROR))
            emit(StatusAPI.Error(ApiError.HttpError, cacheList))
        } catch (e: IOException) {

            Timber.e(application.getString(R.string.EMIT_IO_EXCEPTION))
            emit(StatusAPI.Error(ApiError.IoError, cacheList))
        }

    }.flowOn(dispatcher)

    private suspend fun savedLocalQuakes(remoteData: List<QuakeAndCoordinate>?) {
        remoteData?.forEach {
            //store remote result in cache
            localDataSource.insert(it)
        }
    }
}