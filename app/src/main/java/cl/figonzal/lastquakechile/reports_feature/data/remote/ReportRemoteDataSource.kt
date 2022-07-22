package cl.figonzal.lastquakechile.reports_feature.data.remote


class ReportRemoteDataSource(
    private val reportAPI: ReportAPI
) {
    suspend fun getReports() = reportAPI.listReports()
}
