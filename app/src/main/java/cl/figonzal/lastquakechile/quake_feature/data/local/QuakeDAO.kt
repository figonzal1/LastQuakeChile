package cl.figonzal.lastquakechile.quake_feature.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import androidx.room.Transaction
import cl.figonzal.lastquakechile.quake_feature.data.local.entity.CoordinateEntity
import cl.figonzal.lastquakechile.quake_feature.data.local.entity.QuakeEntity
import cl.figonzal.lastquakechile.quake_feature.data.local.entity.relation.QuakeAndCoordinate

@Dao
interface QuakeDAO {

    @Insert(onConflict = REPLACE)
    suspend fun insertCoordinate(coordinateEntity: CoordinateEntity): Long

    @Insert(onConflict = REPLACE)
    suspend fun insertQuake(quakeEntity: QuakeEntity): Long

    @Transaction
    @Query("SELECT * FROM quakeentity")
    suspend fun getAll(): List<QuakeAndCoordinate>

    @Query("DELETE FROM quakeentity")
    suspend fun deleteAllQuakes()

    @Query("DELETE FROM coordinateentity")
    suspend fun deleteAllCoordinates()

    @Transaction
    suspend fun insertAll(fullQuake: QuakeAndCoordinate) {
        val quakeId = insertQuake(fullQuake.quakeEntity)
        val coordinate = fullQuake.coordinateEntity ?: return
        coordinate.quakeId = quakeId
        insertCoordinate(coordinate)
    }

    @Transaction
    suspend fun deleteAll() {
        deleteAllCoordinates()
        deleteAllQuakes()
    }
}
