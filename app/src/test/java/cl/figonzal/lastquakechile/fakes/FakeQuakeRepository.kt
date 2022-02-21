package cl.figonzal.lastquakechile.fakes

import cl.figonzal.lastquakechile.core.Resource
import cl.figonzal.lastquakechile.quake_feature.domain.model.Coordinates
import cl.figonzal.lastquakechile.quake_feature.domain.model.Quake
import cl.figonzal.lastquakechile.quake_feature.domain.repository.QuakeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.LocalDateTime

class FakeQuakeRepository : QuakeRepository {

    private val list: List<Quake> = listOf(
        Quake(
            quakeCode = 1234,
            localDate = LocalDateTime.now(),
            city = "La Serena",
            reference = "X km al SO de La Serena",
            magnitude = 4.5,
            depth = 45.3,
            scale = "Ml",
            coordinates = Coordinates(-23.23, -75.23),
            isSensitive = true,
            isVerified = true
        ),
        Quake(
            quakeCode = 4641,
            localDate = LocalDateTime.now(),
            city = "Santiago",
            reference = "X km al SO de Santiago",
            magnitude = 4.5,
            depth = 45.3,
            scale = "Ml",
            coordinates = Coordinates(-23.23, -75.23),
            isSensitive = true,
            isVerified = true
        )
    )

    override fun getQuakes(limit: Int): Flow<Resource<List<Quake>>> = flow {

        emit(
            Resource.Success(
                data = list
            )
        )

        emit(
            Resource.Error(
                data = emptyList(),
                message = "Error emission"
            )
        )
    }

}