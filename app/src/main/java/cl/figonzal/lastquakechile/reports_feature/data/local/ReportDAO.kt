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
    suspend fun insertReport(report: ReportEntity): Long

    @Insert(onConflict = REPLACE)
    suspend fun insertAll(topCities: List<QuakeCityEntity>)

    @Query("Delete from reportentity")
    suspend fun deleteAllReportEntity()

    @Query("Delete from quakecityentity")
    suspend fun deleteAllQuakeCityEntity()

    @Transaction
    @Query("SELECT * FROM reportentity")
    fun getReports(): List<ReportWithQuakeCityEntity>

    suspend fun insert(reportWithQuakeCitiesEntity: ReportWithQuakeCityEntity) {

        val reportID = insertReport(reportWithQuakeCitiesEntity.report)

        reportWithQuakeCitiesEntity.topCities.forEach {
            it.idReport = reportID
        }

        insertAll(reportWithQuakeCitiesEntity.topCities)
    }

    suspend fun deleteAll() {
        deleteAllQuakeCityEntity()
        deleteAllReportEntity()
    }
}