package cl.figonzal.lastquakechile.reports_feature.data.local

import cl.figonzal.lastquakechile.reports_feature.data.local.entity.ReportWithCityQuakesEntity


class ReportLocalDataSource(
    private val reportDAO: ReportDAO
) {

    fun getReports() = reportDAO.getReports()

    fun insert(reportEntity: ReportWithCityQuakesEntity) = reportDAO.insert(reportEntity)

    fun deleteAll() = reportDAO.deleteAll()
}