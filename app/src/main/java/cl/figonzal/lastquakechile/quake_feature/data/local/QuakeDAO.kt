package cl.figonzal.lastquakechile.quake_feature.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import cl.figonzal.lastquakechile.quake_feature.data.local.entity.QuakeEntity

@Dao
interface QuakeDAO {

    @Insert(onConflict = REPLACE)
    suspend fun insertQuake(quake: QuakeEntity)

    @Query("SELECT * FROM quakeentity")
    fun getQuakes(): List<QuakeEntity>

    @Query("DELETE FROM quakeentity")
    suspend fun deleteAll()
}