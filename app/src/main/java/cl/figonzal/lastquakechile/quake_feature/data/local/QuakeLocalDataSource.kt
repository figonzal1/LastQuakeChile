package cl.figonzal.lastquakechile.quake_feature.data.local

import cl.figonzal.lastquakechile.quake_feature.data.local.entity.relation.QuakeAndCoordinate
import cl.figonzal.lastquakechile.quake_feature.data.mapper.toQuakeListDomain
import cl.figonzal.lastquakechile.quake_feature.domain.model.Quake

class QuakeLocalDataSource(private val quakeDAO: QuakeDAO) {

    fun getQuakes(): List<Quake> = quakeDAO.getAll().toQuakeListDomain()

    fun insert(quake: QuakeAndCoordinate) {
        quakeDAO.insertAll(quake)
    }

    fun deleteAll() {
        quakeDAO.deleteAll()
    }
}
