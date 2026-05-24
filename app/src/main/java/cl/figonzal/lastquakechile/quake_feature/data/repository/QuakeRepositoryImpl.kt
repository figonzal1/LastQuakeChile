package cl.figonzal.lastquakechile.quake_feature.data.repository

import cl.figonzal.lastquakechile.core.data.remote.toDomainError
import cl.figonzal.lastquakechile.core.domain.DomainError
import cl.figonzal.lastquakechile.core.domain.DomainResult
import cl.figonzal.lastquakechile.quake_feature.data.local.QuakeLocalDataSource
import cl.figonzal.lastquakechile.quake_feature.data.local.entity.relation.QuakeAndCoordinate
import cl.figonzal.lastquakechile.quake_feature.data.mapper.toQuakeListDomain
import cl.figonzal.lastquakechile.quake_feature.data.mapper.toQuakeListEntity
import cl.figonzal.lastquakechile.quake_feature.data.mapper.translateReference
import cl.figonzal.lastquakechile.quake_feature.data.remote.QuakeRemoteDataSource
import cl.figonzal.lastquakechile.quake_feature.domain.model.Quake
import cl.figonzal.lastquakechile.quake_feature.domain.repository.QuakeRepository
import com.skydoves.sandwich.message
import com.skydoves.sandwich.retrofit.statusCode
import com.skydoves.sandwich.suspendOnError
import com.skydoves.sandwich.suspendOnFailure
import com.skydoves.sandwich.suspendOnSuccess
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import timber.log.Timber

class QuakeRepositoryImpl(
    private val localDataSource: QuakeLocalDataSource,
    private val remoteDataSource: QuakeRemoteDataSource,
    private val dispatcher: CoroutineDispatcher
) : QuakeRepository {

    override fun getQuakes(pageIndex: Int) = when (pageIndex) {
        0 -> getFirstPage(pageIndex)
        else -> getNextPages(pageIndex)
    }

    override fun getFirstPage(pageIndex: Int): Flow<DomainResult<List<Quake>>> = flow {

        var cacheList = localDataSource.getQuakes()

        remoteDataSource.getQuakes(pageIndex)
            .suspendOnSuccess {
                when {
                    data.isNotEmpty() -> {
                        val quakes = data
                            .toQuakeListEntity()
                            .translateReference()

                        localDataSource.deleteAll()
                        saveToLocalQuakes(quakes)

                        cacheList = localDataSource.getQuakes()

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
    }.catch { throwable ->
        if (throwable is CancellationException) throw throwable
        Timber.e(throwable, "Unexpected error in getFirstPage flow")
        emit(DomainResult.Error(emptyList(), DomainError.Unknown))
    }.flowOn(dispatcher)

    override fun getNextPages(pageIndex: Int): Flow<DomainResult<List<Quake>>> = flow {

        val emptyList = emptyList<Quake>()

        remoteDataSource.getQuakes(pageIndex)
            .suspendOnSuccess {
                when {
                    data.isNotEmpty() -> {
                        val quakes = data
                            .toQuakeListEntity()
                            .translateReference()
                            .toQuakeListDomain()

                        emit(DomainResult.Success(quakes))
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
    }.catch { throwable ->
        if (throwable is CancellationException) throw throwable
        Timber.e(throwable, "Unexpected error in getNextPages flow")
        emit(DomainResult.Error(emptyList(), DomainError.Unknown))
    }.flowOn(dispatcher)

    private suspend fun saveToLocalQuakes(remoteData: List<QuakeAndCoordinate>) {
        for (quake in remoteData) {
            localDataSource.insert(quake)
        }
    }
}
