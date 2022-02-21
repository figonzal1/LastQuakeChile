package cl.figonzal.lastquakechile.reports_feature.data.remote

import cl.figonzal.lastquakechile.reports_feature.data.local.entity.ReportWithQuakeCityEntity


class ReportRemoteDataSource(
    private val reportAPI: ReportAPI
) {

    suspend fun getReports(): List<ReportWithQuakeCityEntity> {

        val call = reportAPI.listReports()

        return call.body()?.reportes?.map { it ->

            val reportEntity = it.toReportEntity()
            val topCities = it.top_ciudades.map { it.toQuakeCityEntity() }

            ReportWithQuakeCityEntity(
                report = reportEntity,
                topCities = topCities
            )
        }!!
    }
}
