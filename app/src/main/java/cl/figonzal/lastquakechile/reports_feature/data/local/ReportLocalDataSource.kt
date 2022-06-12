package cl.figonzal.lastquakechile.reports_feature.data.local

import cl.figonzal.lastquakechile.reports_feature.data.local.entity.ReportWithQuakeCityEntity


class ReportLocalDataSource(
    private val reportDAO: ReportDAO
) {

    fun getReports() = reportDAO.getReports()

    fun insert(reportEntity: ReportWithQuakeCityEntity) {
        reportDAO.insert(reportEntity)
    }

    fun deleteAll() {
        reportDAO.deleteAll()
    }
}