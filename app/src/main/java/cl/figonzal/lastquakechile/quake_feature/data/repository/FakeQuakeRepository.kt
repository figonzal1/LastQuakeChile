@file:Suppress("OldIssuesGlobalInspectionTool")

package cl.figonzal.lastquakechile.quake_feature.data.repository

import cl.figonzal.lastquakechile.core.data.remote.ApiError
import cl.figonzal.lastquakechile.core.data.remote.StatusAPI
import cl.figonzal.lastquakechile.core.utils.localDateTimeToString
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

    private var shouldReturnNetworkError = false

    private val quakeList = listOf(
        Quake(
            quakeCode = 123,
            localDate = LocalDateTime.now().minusMinutes(12).localDateTimeToString(),
            city = "La Serena",
            reference = "45km al OS de La Serena",
            magnitude = 3.6,
            depth = 34.8,
            scale = "ml",
            coordinate = Coordinate(-30.06, -71.31),
            isSensitive = false,
            isVerified = true
        ),
        Quake(
            quakeCode = 435,
            localDate = LocalDateTime.now().minusMinutes(40).localDateTimeToString(),
            city = "Concepción",
            reference = "14km al OS de Concpeción",
            magnitude = 6.6,
            depth = 34.8,
            scale = "mw",
            coordinate = Coordinate(-36.4, -73.47),
            isSensitive = true,
            isVerified = true
        ),
        Quake(
            quakeCode = 8213,
            localDate = LocalDateTime.now().minusHours(2).localDateTimeToString(),
            city = "Santiago",
            reference = "14km al OS de Santiago",
            magnitude = 2.8,
            depth = 55.2,
            scale = "ml",
            coordinate = Coordinate(-33.55, -70.64),
            isSensitive = false,
            isVerified = true
        ),
        Quake(
            quakeCode = 6342,
            localDate = LocalDateTime.now().minusHours(3).localDateTimeToString(),
            city = "Arica",
            reference = "67km al SE de Arica",
            magnitude = 3.2,
            depth = 55.2,
            scale = "ml",
            coordinate = Coordinate(-30.14, 60.9),
            isSensitive = false,
            isVerified = true
        ),
        Quake(
            quakeCode = 461,
            localDate = LocalDateTime.now().minusHours(4).localDateTimeToString(),
            city = "Iquique",
            reference = "21km al SE de Arica",
            magnitude = 3.8,
            depth = 32.2,
            scale = "ml",
            coordinate = Coordinate(-18.62, -71.27),
            isSensitive = true,
            isVerified = true
        ),
        Quake(
            quakeCode = 200125,
            localDate = LocalDateTime.now().minusHours(5).localDateTimeToString(),
            city = "Punitaqui",
            reference = "20 km al NO de Punitaqui",
            magnitude = 4.6,
            depth = 48.0,
            scale = "Mw",
            coordinate = Coordinate(-30.74, -71.43),
            isSensitive = false,
            isVerified = true
        ),
        Quake(
            quakeCode = 200096,
            localDate = LocalDateTime.now().minusHours(5).localDateTimeToString(),
            city = "Los Vilos",
            reference = "31 km al NO de Los Vilos",
            magnitude = 2.9,
            depth = 39.0,
            scale = "ML",
            coordinate = Coordinate(-31.76, -71.79),
            isSensitive = false,
            isVerified = true
        ),
        Quake(
            quakeCode = 200111,
            localDate = LocalDateTime.now().minusHours(8).localDateTimeToString(),
            city = "Socaire",
            reference = "68 km al S de Socaire",
            magnitude = 3.0,
            depth = 250.0,
            scale = "ML",
            coordinate = Coordinate(-24.2, -67.91),
            isSensitive = false,
            isVerified = true
        ),
        Quake(
            quakeCode = 200102,
            localDate = LocalDateTime.now().minusHours(10).localDateTimeToString(),
            city = "Quillagua",
            reference = "41 km al NO de Quillagua",
            magnitude = 2.8,
            depth = 42.0,
            scale = "ML",
            coordinate = Coordinate(-21.4, -69.81),
            isSensitive = false,
            isVerified = true
        ),
        Quake(
            quakeCode = 200088,
            localDate = LocalDateTime.now().minusHours(12).localDateTimeToString(),
            city = "Camiña",
            reference = "45 km al S de Camiña",
            magnitude = 2.6,
            depth = 42.0,
            scale = "ML",
            coordinate = Coordinate(-19.72, -69.42),
            isSensitive = false,
            isVerified = true
        )
    )

    override fun getQuakes(pageIndex: Int) = when (pageIndex) {
        0 -> getFirstPage(pageIndex)
        else -> getNextPages(pageIndex)
    }

    //NOSONAR
    override fun getFirstPage(pageIndex: Int): Flow<StatusAPI<List<Quake>>> = flow {
        when {
            shouldReturnNetworkError -> emit(StatusAPI.Error(quakeList, ApiError.HttpError))
            else -> emit(StatusAPI.Success(quakeList.take(20)))
        }
    }.flowOn(dispatcher)

    //NOSONAR
    override fun getNextPages(pageIndex: Int): Flow<StatusAPI<List<Quake>>> = flow {
        when {
            shouldReturnNetworkError -> emit(StatusAPI.Error(quakeList, ApiError.HttpError))
            else -> emit(StatusAPI.Success(quakeList.take(20)))
        }
    }.flowOn(dispatcher)

}