package cl.figonzal.lastquakechile.quake_feature.domain.uses_cases

import cl.figonzal.lastquakechile.core.Resource
import cl.figonzal.lastquakechile.quake_feature.domain.model.Quake
import cl.figonzal.lastquakechile.quake_feature.domain.repository.QuakeRepository
import kotlinx.coroutines.flow.Flow

class GetQuakesUseCase(
    private val repository: QuakeRepository
) {

    operator fun invoke(): Flow<Resource<List<Quake>>> {
        return repository.getQuakes()
    }
}