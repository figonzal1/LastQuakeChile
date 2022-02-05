package cl.figonzal.lastquakechile.reports_feature.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import androidx.room.Transaction
import cl.figonzal.lastquakechile.reports_feature.data.local.entity.QuakeCityEntity
import cl.figonzal.lastquakechile.reports_feature.data.local.entity.ReportEntity
import cl.figonzal.lastquakechile.reports_feature.data.local.entity.ReportWithQuakeCity

@Dao
abstract class ReportDAO {

    @Insert(onConflict = REPLACE)
    abstract fun insertReport(report: ReportEntity): Long

    @Insert(onConflict = REPLACE)
    abstract fun insertAll(topCities: List<QuakeCityEntity>)

    @Transaction
    @Query("SELECT * FROM reportentity")
    abstract fun getReport(): List<ReportWithQuakeCity>

    fun insert(reportWithQuakeCities: ReportWithQuakeCity) {

        val reportID = insertReport(reportWithQuakeCities.report)

        reportWithQuakeCities.topCities.forEach {
            it.idReport = reportID
        }

        insertAll(reportWithQuakeCities.topCities)
    }
}