package cl.figonzal.lastquakechile.reports_feature.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import androidx.room.Transaction
import cl.figonzal.lastquakechile.reports_feature.data.local.entity.CityQuakesEntity
import cl.figonzal.lastquakechile.reports_feature.data.local.entity.ReportEntity
import cl.figonzal.lastquakechile.reports_feature.data.local.entity.ReportWithCityQuakesEntity

@Dao
interface ReportDAO {

    @Insert(onConflict = REPLACE)
    fun insertReport(report: ReportEntity): Long

    @Insert(onConflict = REPLACE)
    fun insertAll(topCities: List<CityQuakesEntity>)

    @Query("Delete from reportentity")
    fun deleteAllReportEntity()

    @Query("Delete from cityquakesentity")
    fun deleteAllQuakeCityEntity()

    @Transaction
    @Query("SELECT * FROM reportentity")
    fun getReports(): List<ReportWithCityQuakesEntity>

    fun insert(reportWithQuakeCitiesEntity: ReportWithCityQuakesEntity) {

        val reportID = insertReport(reportWithQuakeCitiesEntity.report)

        reportWithQuakeCitiesEntity.topCities.forEach {
            it.idReport = reportID
        }

        insertAll(reportWithQuakeCitiesEntity.topCities)
    }

    fun deleteAll() {
        deleteAllQuakeCityEntity()
        deleteAllReportEntity()
    }
}