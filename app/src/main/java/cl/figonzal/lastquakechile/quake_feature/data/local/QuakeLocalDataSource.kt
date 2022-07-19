package cl.figonzal.lastquakechile.quake_feature.data.local

import cl.figonzal.lastquakechile.quake_feature.data.local.entity.relation.QuakeAndCoordinate

class QuakeLocalDataSource(
    private val quakeDAO: QuakeDAO
) {

    suspend fun getQuakes() = quakeDAO.getAll()

    suspend fun insert(quake: QuakeAndCoordinate) {
        quakeDAO.insertAll(quake)
    }

    suspend fun deleteAll() {
        quakeDAO.deleteAll()
    }
}