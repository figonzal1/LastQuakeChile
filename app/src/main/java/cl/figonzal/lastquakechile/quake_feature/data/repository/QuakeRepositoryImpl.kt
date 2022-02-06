package cl.figonzal.lastquakechile.quake_feature.data.repository

import cl.figonzal.lastquakechile.core.Resource
import cl.figonzal.lastquakechile.quake_feature.data.remote.QuakeRemoteDataSource
import cl.figonzal.lastquakechile.quake_feature.domain.model.Quake
import cl.figonzal.lastquakechile.quake_feature.domain.repository.QuakeRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class QuakeRepositoryImpl(
    private val remoteDataSource: QuakeRemoteDataSource,
    private val dispatcher: CoroutineDispatcher
) : QuakeRepository {


    override fun getQuakes(): Flow<Resource<List<Quake>>> = flow {

        emit(Resource.Loading())

        val networkCall = remoteDataSource.getQuakes()

        emit(Resource.Success(networkCall))

    }.flowOn(dispatcher)
}