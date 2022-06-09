package cl.figonzal.lastquakechile.quake_feature.data.repository

import cl.figonzal.lastquakechile.core.Resource
import cl.figonzal.lastquakechile.quake_feature.domain.model.Coordinates
import cl.figonzal.lastquakechile.quake_feature.domain.model.Quake
import cl.figonzal.lastquakechile.quake_feature.domain.repository.QuakeRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.time.LocalDateTime


class FakeQuakeRepository(
    private val dispatcher: CoroutineDispatcher
) : QuakeRepository {

    var shouldReturnNetworkError = false

    private val quakeList = listOf(
        Quake(
            123,
            LocalDateTime.now(),
            "La Serena",
            "14km al OS de La Serena",
            5.6,
            34.8,
            "ml",
            Coordinates(-24.23, 75.3),
            isSensitive = false,
            isVerified = true
        ),
        Quake(
            435,
            LocalDateTime.now(),
            "Concepción",
            "14km al OS de Concpeción",
            7.6,
            34.8,
            "ml",
            Coordinates(-24.23, 75.3),
            isSensitive = true,
            isVerified = true
        ),
        Quake(
            123,
            LocalDateTime.now(),
            "Santiago",
            "14km al OS de Santiago",
            6.2,
            55.2,
            "ml",
            Coordinates(-30.34, 60.3),
            isSensitive = false,
            isVerified = true
        )
    )

    override fun getQuakes(limit: Int): Flow<Resource<List<Quake>>> = flow {

        //Should be omitted in test
        emit(Resource.Loading())

        when {
            shouldReturnNetworkError -> {
                emit(Resource.Error("Network Error"))
            }
            else -> {
                emit(Resource.Success(quakeList.take(limit)))
            }
        }
    }.flowOn(dispatcher)

}