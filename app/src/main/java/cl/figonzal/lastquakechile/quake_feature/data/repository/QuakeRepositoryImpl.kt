package cl.figonzal.lastquakechile.quake_feature.data.repository

import android.app.Application
import cl.figonzal.lastquakechile.core.data.remote.ApiError
import cl.figonzal.lastquakechile.core.data.remote.StatusAPI
import cl.figonzal.lastquakechile.core.utils.processSandwichError
import cl.figonzal.lastquakechile.core.utils.toQuakeListDomain
import cl.figonzal.lastquakechile.core.utils.toQuakeListEntity
import cl.figonzal.lastquakechile.core.utils.translateReference
import cl.figonzal.lastquakechile.quake_feature.data.local.QuakeLocalDataSource
import cl.figonzal.lastquakechile.quake_feature.data.local.entity.relation.QuakeAndCoordinate
import cl.figonzal.lastquakechile.quake_feature.data.remote.QuakeRemoteDataSource
import cl.figonzal.lastquakechile.quake_feature.domain.model.Quake
import cl.figonzal.lastquakechile.quake_feature.domain.repository.QuakeRepository
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

class QuakeRepositoryImpl(
    private val localDataSource: QuakeLocalDataSource,
    private val remoteDataSource: QuakeRemoteDataSource,
    private val dispatcher: CoroutineDispatcher,
    private val application: Application
) : QuakeRepository {

    override fun getQuakes(pageIndex: Int) = when (pageIndex) {
        0 -> getFirstPage(pageIndex)
        else -> getNextPages(pageIndex)
    }

    override fun getFirstPage(pageIndex: Int): Flow<StatusAPI<List<Quake>>> = flow {

        var cacheList = localDataSource.getQuakes().toQuakeListDomain()

        //GET REMOTE DATA
        remoteDataSource.getQuakes(pageIndex)
            .suspendOnSuccess {

                when {
                    data.embedded != null -> {
                        val quakes = data.embedded!!.quakes
                            .toQuakeListEntity()
                            .translateReference()

                        localDataSource.deleteAll()
                        saveToLocalQuakes(quakes)

                        cacheList = localDataSource.getQuakes().toQuakeListDomain()

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

    override fun getNextPages(pageIndex: Int): Flow<StatusAPI<List<Quake>>> = flow {

        val emptyList = emptyList<Quake>()

        //GET REMOTE DATA
        remoteDataSource.getQuakes(pageIndex)
            .suspendOnSuccess {

                when {
                    data.embedded != null -> {
                        val quakes = data.embedded!!.quakes
                            .toQuakeListEntity()
                            .translateReference()
                            .toQuakeListDomain()

                        emit(StatusAPI.Success(quakes))

                        Timber.d("List updated with network call")
                    }
                    else -> {
                        //Resources not found in next page index
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

    private fun saveToLocalQuakes(remoteData: List<QuakeAndCoordinate>) {

        remoteData.forEach {
            //store remote result in cache
            localDataSource.insert(it)
        }
    }
}