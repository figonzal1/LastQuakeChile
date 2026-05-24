package cl.figonzal.lastquakechile.quake_feature.ui

import cl.figonzal.lastquakechile.core.domain.DomainError
import cl.figonzal.lastquakechile.quake_feature.domain.model.Quake

data class QuakeState(
    val isLoading: Boolean = false,
    val domainError: DomainError? = null,
    val quakes: List<Quake> = emptyList(),
    val isLastPage: Boolean = false
)
