package cl.figonzal.lastquakechile.quake_feature.domain.repository

import cl.figonzal.lastquakechile.core.data.remote.NewStatusAPI
import cl.figonzal.lastquakechile.quake_feature.domain.model.Quake
import kotlinx.coroutines.flow.Flow

interface QuakeRepository {

    fun getQuakes(limit: Int): Flow<NewStatusAPI<List<Quake>>>
}