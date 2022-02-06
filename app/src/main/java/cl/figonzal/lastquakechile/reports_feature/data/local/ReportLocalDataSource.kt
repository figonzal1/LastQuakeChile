package cl.figonzal.lastquakechile.reports_feature.data.local

import android.app.Application
import cl.figonzal.lastquakechile.ApplicationController
import cl.figonzal.lastquakechile.reports_feature.data.local.entity.ReportWithQuakeCity


class ReportLocalDataSource(
    application: Application
) {

    private val reportDAO = (application as ApplicationController).database.reportDao()

    fun getReports(): List<ReportWithQuakeCity> {
        return reportDAO.getReport()
    }

    fun insert(report: ReportWithQuakeCity) {
        reportDAO.insert(report)
    }
}