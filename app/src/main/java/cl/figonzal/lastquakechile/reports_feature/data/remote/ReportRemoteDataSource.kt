package cl.figonzal.lastquakechile.reports_feature.data.remote

import cl.figonzal.lastquakechile.core.utils.toReportEntity
import cl.figonzal.lastquakechile.reports_feature.data.local.entity.ReportWithCityQuakesEntity


class ReportRemoteDataSource(
    private val reportAPI: ReportAPI
) {

    suspend fun getReports(): List<ReportWithCityQuakesEntity>? {

        val call = reportAPI.listReports()

        return call.body()?.reports?.toReportEntity()
    }
}
