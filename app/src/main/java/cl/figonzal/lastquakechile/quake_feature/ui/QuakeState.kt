package cl.figonzal.lastquakechile.quake_feature.ui

import cl.figonzal.lastquakechile.quake_feature.domain.model.Quake

data class QuakeState(
    val quakes: List<Quake> = emptyList(),
    val isLoading: Boolean = false
)
