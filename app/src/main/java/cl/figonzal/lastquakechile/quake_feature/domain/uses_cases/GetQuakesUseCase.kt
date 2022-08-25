package cl.figonzal.lastquakechile.quake_feature.domain.uses_cases

import cl.figonzal.lastquakechile.quake_feature.domain.repository.QuakeRepository

class GetQuakesUseCase(
    private val repository: QuakeRepository
) {
    operator fun invoke(pageIndex: Int) = repository.getQuakes(pageIndex)
}