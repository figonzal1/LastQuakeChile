package cl.figonzal.lastquakechile.reports_feature.data.local

import cl.figonzal.lastquakechile.reports_feature.data.local.entity.relation.ReportWithCityQuakes


class ReportLocalDataSource(
    private val reportDAO: ReportDAO
) {

    fun getReports() = reportDAO.getAll()

    fun insert(report: ReportWithCityQuakes) {
        reportDAO.insertAll(report)
    }

    fun deleteAll() {
        reportDAO.deleteAll()
    }
}