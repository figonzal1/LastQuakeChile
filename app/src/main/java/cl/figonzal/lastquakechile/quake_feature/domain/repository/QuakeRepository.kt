package cl.figonzal.lastquakechile.quake_feature.domain.repository

import cl.figonzal.lastquakechile.core.domain.DomainResult
import cl.figonzal.lastquakechile.quake_feature.domain.model.Quake
import kotlinx.coroutines.flow.Flow

interface QuakeRepository {
    fun getQuakes(pageIndex: Int): Flow<DomainResult<List<Quake>>>
    fun getFirstPage(pageIndex: Int): Flow<DomainResult<List<Quake>>>
    fun getNextPages(pageIndex: Int): Flow<DomainResult<List<Quake>>>
}
