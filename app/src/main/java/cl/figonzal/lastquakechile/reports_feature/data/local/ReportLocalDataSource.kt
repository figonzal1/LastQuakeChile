package cl.figonzal.lastquakechile.reports_feature.data.local

import cl.figonzal.lastquakechile.reports_feature.data.local.entity.relation.ReportWithCityQuakes
import cl.figonzal.lastquakechile.reports_feature.data.mapper.toReportListDomain
import cl.figonzal.lastquakechile.reports_feature.domain.model.Report

class ReportLocalDataSource(private val reportDAO: ReportDAO) {

    suspend fun getReports(): List<Report> = reportDAO.getAll().toReportListDomain()

    suspend fun insert(report: ReportWithCityQuakes) {
        reportDAO.insertAll(report)
    }

    suspend fun deleteAll() {
        reportDAO.deleteAll()
    }
}
