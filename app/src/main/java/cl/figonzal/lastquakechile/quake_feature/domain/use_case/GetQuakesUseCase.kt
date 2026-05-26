package cl.figonzal.lastquakechile.quake_feature.domain.use_case

import cl.figonzal.lastquakechile.quake_feature.domain.repository.QuakeRepository

class GetQuakesUseCase(
    private val repository: QuakeRepository
) {
    operator fun invoke(pageIndex: Int) = repository.getQuakes(pageIndex)
}