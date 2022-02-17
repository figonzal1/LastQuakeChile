package cl.figonzal.lastquakechile.reports_feature.data.local

import cl.figonzal.lastquakechile.reports_feature.data.local.entity.ReportWithQuakeCity


class ReportLocalDataSource(
    private val reportDAO: ReportDAO
) {

    fun getReports(): List<ReportWithQuakeCity> {
        return reportDAO.getReport()
    }

    fun insert(report: ReportWithQuakeCity) {
        reportDAO.insert(report)
    }
}