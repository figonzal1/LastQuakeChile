package cl.figonzal.lastquakechile.reports_feature.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import androidx.room.Transaction
import cl.figonzal.lastquakechile.reports_feature.data.local.entity.QuakeCityEntity
import cl.figonzal.lastquakechile.reports_feature.data.local.entity.ReportEntity
import cl.figonzal.lastquakechile.reports_feature.data.local.entity.ReportWithQuakeCityEntity

@Dao
interface ReportDAO {

    @Insert(onConflict = REPLACE)
    fun insertReport(report: ReportEntity): Long

    @Insert(onConflict = REPLACE)
    fun insertAll(topCities: List<QuakeCityEntity>)

    @Query("Delete from reportentity")
    fun deleteAllReportEntity()

    @Query("Delete from quakecityentity")
    fun deleteAllQuakeCityEntity()

    @Transaction
    @Query("SELECT * FROM reportentity")
    fun getReports(): List<ReportWithQuakeCityEntity>

    fun insert(reportWithQuakeCitiesEntity: ReportWithQuakeCityEntity) {

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