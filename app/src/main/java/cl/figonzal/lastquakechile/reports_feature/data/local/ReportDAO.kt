package cl.figonzal.lastquakechile.reports_feature.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import androidx.room.Transaction
import cl.figonzal.lastquakechile.reports_feature.data.local.entity.CityQuakesEntity
import cl.figonzal.lastquakechile.reports_feature.data.local.entity.ReportEntity
import cl.figonzal.lastquakechile.reports_feature.data.local.entity.relation.ReportWithCityQuakes

@Dao
interface ReportDAO {

    @Insert(onConflict = REPLACE)
    suspend fun insertCityQuakes(cityQuakesEntity: List<CityQuakesEntity>)

    @Insert(onConflict = REPLACE)
    suspend fun insertReport(reportEntity: ReportEntity): Long

    @Transaction
    @Query("SELECT * FROM reportentity")
    suspend fun getAll(): List<ReportWithCityQuakes>

    @Query("Delete from reportentity")
    suspend fun deleteAllReports()

    @Query("Delete from cityquakesentity")
    suspend fun deleteAllCityQuakes()

    @Transaction
    suspend fun insertAll(fullReport: ReportWithCityQuakes) {
        val reportId = insertReport(fullReport.report)

        fullReport.cityQuakes.forEach {
            it.reportId = reportId
        }

        insertCityQuakes(fullReport.cityQuakes)
    }

    @Transaction
    suspend fun deleteAll() {
        deleteAllReports()
        deleteAllCityQuakes()
    }
}
