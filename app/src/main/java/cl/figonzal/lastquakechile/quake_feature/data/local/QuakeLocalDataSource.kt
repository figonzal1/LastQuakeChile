package cl.figonzal.lastquakechile.quake_feature.data.local

import cl.figonzal.lastquakechile.quake_feature.data.local.entity.QuakeEntity

class QuakeLocalDataSource(
    private val quakeDAO: QuakeDAO
) {

    fun getQuakes(): List<QuakeEntity> {
        return quakeDAO.getQuakes()
    }

    suspend fun insert(quake: QuakeEntity) {
        quakeDAO.insertQuake(quake)
    }

    suspend fun deleteAll() {
        quakeDAO.deleteAll()
    }
}