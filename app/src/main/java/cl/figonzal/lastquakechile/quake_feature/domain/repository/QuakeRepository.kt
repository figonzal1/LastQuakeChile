package cl.figonzal.lastquakechile.quake_feature.domain.repository

import cl.figonzal.lastquakechile.core.data.remote.StatusAPI
import cl.figonzal.lastquakechile.quake_feature.domain.model.Quake
import kotlinx.coroutines.flow.Flow

interface QuakeRepository {
    fun getQuakes(pageIndex: Int): Flow<StatusAPI<List<Quake>>>
    fun getFirstPage(pageIndex: Int): Flow<StatusAPI<List<Quake>>>
    fun getNextPages(pageIndex: Int): Flow<StatusAPI<List<Quake>>>
}