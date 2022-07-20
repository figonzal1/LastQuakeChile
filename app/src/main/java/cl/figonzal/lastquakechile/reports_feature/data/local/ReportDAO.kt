package cl.figonzal.lastquakechile.reports_feature.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import androidx.room.Transaction
import cl.figonzal.lastquakechile.reports_feature.data.local.entity.CityQuakesEntity
import cl.figonzal.lastquakechile.reports_feature.data.local.entity.ReportEntity
import cl.figonzal.lastquakechile.reports_feature.data.local.entity.relation.ReportWithCityQuakes

@Dao
interface ReportDAO {

    @Insert(onConflict = REPLACE)
    fun insertCityQuakes(cityQuakesEntity: List<CityQuakesEntity>)

    @Insert(onConflict = REPLACE)
    fun insertReport(reportEntity: ReportEntity): Long

    @Transaction
    @Query("SELECT * FROM reportentity")
    fun getAll(): List<ReportWithCityQuakes>

    @Query("Delete from reportentity")
    fun deleteAllReports()

    @Query("Delete from cityquakesentity")
    fun deleteAllCityQuakes()

    @Transaction
    fun insertAll(fullReport: ReportWithCityQuakes) {

        val reportId = insertReport(fullReport.report)

        fullReport.cityQuakes.forEach {
            it.reportId = reportId
        }

        insertCityQuakes(fullReport.cityQuakes)
    }

    fun deleteAll() {
        deleteAllReports()
        deleteAllCityQuakes()
    }
}