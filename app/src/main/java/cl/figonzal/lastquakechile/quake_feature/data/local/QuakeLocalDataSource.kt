package cl.figonzal.lastquakechile.quake_feature.data.local

import android.app.Application
import cl.figonzal.lastquakechile.core.ApplicationController
import cl.figonzal.lastquakechile.quake_feature.data.local.entity.QuakeEntity

class QuakeLocalDataSource(
    application: Application
) {

    private val quakeDAO = (application as ApplicationController).database.quakeDao()

    fun getQuakes(): List<QuakeEntity> {
        return quakeDAO.getQuakes()
    }

    fun insert(quake: QuakeEntity) {
        quakeDAO.insertQuake(quake)
    }

    fun deleteAll() {
        quakeDAO.deleteAll()
    }
}