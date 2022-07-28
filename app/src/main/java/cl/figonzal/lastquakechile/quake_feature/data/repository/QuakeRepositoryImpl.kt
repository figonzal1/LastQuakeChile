package cl.figonzal.lastquakechile.quake_feature.data.repository

import android.app.Application
import cl.figonzal.lastquakechile.R
import cl.figonzal.lastquakechile.core.data.remote.ApiError
import cl.figonzal.lastquakechile.core.data.remote.StatusAPI
import cl.figonzal.lastquakechile.core.utils.toQuakeDomain
import cl.figonzal.lastquakechile.core.utils.toQuakeListEntity
import cl.figonzal.lastquakechile.quake_feature.data.local.QuakeLocalDataSource
import cl.figonzal.lastquakechile.quake_feature.data.local.entity.relation.QuakeAndCoordinate
import cl.figonzal.lastquakechile.quake_feature.data.remote.QuakeRemoteDataSource
import cl.figonzal.lastquakechile.quake_feature.domain.model.Quake
import cl.figonzal.lastquakechile.quake_feature.domain.repository.QuakeRepository
import com.skydoves.sandwich.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import timber.log.Timber

class QuakeRepositoryImpl(
    private val localDataSource: QuakeLocalDataSource,
    private val remoteDataSource: QuakeRemoteDataSource,
    private val dispatcher: CoroutineDispatcher,
    private val application: Application
) : QuakeRepository {


    override fun getQuakes(limit: Int): Flow<StatusAPI<List<Quake>>> = flow {

        var cacheList = localDataSource.getQuakes().toQuakeDomain()

        emit(StatusAPI.Success(cacheList)) //Cached list

        //GET REMOTE DATA
        remoteDataSource.getQuakes(limit)
            .suspendOnSuccess {

                val quakes = data.embedded.quakes.toQuakeListEntity()

                localDataSource.deleteAll() //Remove cache

                savedLocalQuakes(quakes)

                cacheList = localDataSource.getQuakes().toQuakeDomain()

                Timber.d(application.getString(R.string.LIST_NETWORK_CALL))

                emit(StatusAPI.Success(cacheList))
            }
            .suspendOnError {

                Timber.e("Suspend error: ${this.message()}")

                val apiError = when (statusCode) {
                    StatusCode.NotFound -> ApiError.HttpError
                    StatusCode.RequestTimeout -> ApiError.ServerError
                    StatusCode.InternalServerError -> ApiError.ServerError
                    StatusCode.ServiceUnavailable -> ApiError.ServerError
                    else -> ApiError.UnknownError
                }

                emit(StatusAPI.Error(apiError, cacheList))
            }
            .suspendOnFailure {

                Timber.e("Suspend failure: ${this.message()}")

                val apiError = when {
                    message().contains("10000ms") || message().contains(
                        "failed to connect",
                        true
                    ) -> ApiError.TimeoutError
                    else -> ApiError.UnknownError
                }

                emit(StatusAPI.Error(apiError, cacheList))
            }
    }.flowOn(dispatcher)

    private fun savedLocalQuakes(remoteData: List<QuakeAndCoordinate>) {

        remoteData.forEach {
            //store remote result in cache
            localDataSource.insert(it)
        }
    }
}