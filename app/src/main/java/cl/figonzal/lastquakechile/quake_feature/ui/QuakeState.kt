package cl.figonzal.lastquakechile.quake_feature.ui

import cl.figonzal.lastquakechile.core.utils.ApiError
import cl.figonzal.lastquakechile.quake_feature.domain.model.Quake

data class QuakeState(
    val isLoading: Boolean = false,
    val apiError: ApiError? = null,
    val quakes: List<Quake> = listOf()
)
