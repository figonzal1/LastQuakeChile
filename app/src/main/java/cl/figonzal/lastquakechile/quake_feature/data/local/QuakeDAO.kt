package cl.figonzal.lastquakechile.quake_feature.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import androidx.room.Transaction
import cl.figonzal.lastquakechile.quake_feature.data.local.entity.CoordinateEntity
import cl.figonzal.lastquakechile.quake_feature.data.local.entity.QuakeEntity
import cl.figonzal.lastquakechile.quake_feature.data.local.entity.relation.QuakeAndCoordinate

@Dao
interface QuakeDAO {

    @Insert(onConflict = REPLACE)
    fun insertCoordinate(coordinateEntity: CoordinateEntity): Long

    @Insert(onConflict = REPLACE)
    fun insertQuake(quakeEntity: QuakeEntity): Long

    @Transaction
    @Query("SELECT * FROM quakeentity")
    fun getAll(): List<QuakeAndCoordinate>

    @Query("DELETE FROM quakeentity")
    fun deleteAllQuakes()

    @Query("DELETE FROM coordinateentity")
    fun deleteAllCoordinates()

    @Transaction
    fun insertAll(fullQuake: QuakeAndCoordinate) {

        val quakeId = insertQuake(fullQuake.quakeEntity)
        fullQuake.coordinateEntity.quakeId = quakeId
        insertCoordinate(fullQuake.coordinateEntity)
    }

    fun deleteAll() {
        deleteAllCoordinates()
        deleteAllQuakes()
    }
}