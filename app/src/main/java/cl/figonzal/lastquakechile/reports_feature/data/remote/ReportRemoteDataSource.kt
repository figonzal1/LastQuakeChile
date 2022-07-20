package cl.figonzal.lastquakechile.reports_feature.data.remote

import cl.figonzal.lastquakechile.core.utils.toReportEntity
import cl.figonzal.lastquakechile.reports_feature.data.local.entity.relation.ReportWithCityQuakes


class ReportRemoteDataSource(
    private val reportAPI: ReportAPI
) {

    suspend fun getReports(): List<ReportWithCityQuakes>? {

        val call = reportAPI.listReports()

        return call.body()?.embedded?.reports?.toReportEntity()
    }
}
