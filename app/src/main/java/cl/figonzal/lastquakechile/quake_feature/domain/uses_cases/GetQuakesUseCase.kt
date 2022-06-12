package cl.figonzal.lastquakechile.quake_feature.domain.uses_cases

import cl.figonzal.lastquakechile.quake_feature.domain.repository.QuakeRepository

class GetQuakesUseCase(
    private val repository: QuakeRepository,
    private val limit: Int
) {

    operator fun invoke() = repository.getQuakes(limit)
}