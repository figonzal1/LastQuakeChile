package cl.figonzal.lastquakechile.quake_feature.data.repository

import android.app.Application
import cl.figonzal.lastquakechile.R
import cl.figonzal.lastquakechile.core.Resource
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


    override fun getQuakes(): Flow<Resource<List<Quake>>> = flow {

        emit(Resource.Loading())

        var cacheList = localDataSource.getQuakes().map { it.toDomainQuake() }
        emit(Resource.Success(cacheList))

        try {
            localDataSource.deleteAll()
            remoteDataSource.getQuakes().onEach {
                //store remote result in cache
                localDataSource.insert(it)

            }.map { it.toDomainQuake() }

            cacheList = localDataSource.getQuakes().map { it.toDomainQuake() }

            Timber.d("List updated with network call")

            emit(Resource.Success(cacheList))

        } catch (e: HttpException) {

            Timber.e("Emit http error")

            emit(
                Resource.Error(
                    message = application.getString(R.string.HTTP_ERROR)
                )
            )
        } catch (e: IOException) {

            Timber.e("Emit ioexception")

            emit(
                Resource.Error(
                    message = application.getString(R.string.IO_ERROR)
                )
            )
        }

    }.flowOn(dispatcher)
}