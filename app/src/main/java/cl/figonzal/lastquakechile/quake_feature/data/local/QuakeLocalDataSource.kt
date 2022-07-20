package cl.figonzal.lastquakechile.quake_feature.data.local

import cl.figonzal.lastquakechile.quake_feature.data.local.entity.relation.QuakeAndCoordinate

class QuakeLocalDataSource(
    private val quakeDAO: QuakeDAO
) {

    fun getQuakes() = quakeDAO.getAll()

    fun insert(quake: QuakeAndCoordinate) {
        quakeDAO.insertAll(quake)
    }

    fun deleteAll() {
        quakeDAO.deleteAll()
    }
}