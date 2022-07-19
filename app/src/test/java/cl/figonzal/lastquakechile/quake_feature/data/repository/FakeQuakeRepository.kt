package cl.figonzal.lastquakechile.quake_feature.data.repository

import cl.figonzal.lastquakechile.core.data.remote.ApiError
import cl.figonzal.lastquakechile.core.data.remote.NewStatusAPI
import cl.figonzal.lastquakechile.quake_feature.domain.model.Coordinate
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
            quakeCode = 123,
            localDate = LocalDateTime.now(),
            city = "La Serena",
            reference = "14km al OS de La Serena",
            magnitude = 5.6,
            depth = 34.8,
            scale = "ml",
            coordinate = Coordinate(-24.23, 75.3),
            isSensitive = false,
            isVerified = true
        ),
        Quake(
            quakeCode = 435,
            localDate = LocalDateTime.now(),
            city = "Concepción",
            reference = "14km al OS de Concpeción",
            magnitude = 7.6,
            depth = 34.8,
            scale = "ml",
            coordinate = Coordinate(-24.23, 75.3),
            isSensitive = true,
            isVerified = true
        ),
        Quake(
            quakeCode = 123,
            localDate = LocalDateTime.now(),
            city = "Santiago",
            reference = "14km al OS de Santiago",
            magnitude = 6.2,
            depth = 55.2,
            scale = "ml",
            coordinate = Coordinate(-30.34, 60.3),
            isSensitive = false,
            isVerified = true
        )
    )

    override fun getQuakes(limit: Int): Flow<NewStatusAPI<List<Quake>>> = flow {

        when {
            shouldReturnNetworkError -> emit(
                NewStatusAPI.Error(
                    ApiError.HttpError,
                    quakeList //Error with cached List
                )
            )
            else -> emit(NewStatusAPI.Success(quakeList.take(limit)))
        }
    }.flowOn(dispatcher)

}