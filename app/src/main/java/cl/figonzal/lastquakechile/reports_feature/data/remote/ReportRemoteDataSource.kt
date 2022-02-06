package cl.figonzal.lastquakechile.reports_feature.data.remote

import android.app.Application
import cl.figonzal.lastquakechile.ApplicationController
import cl.figonzal.lastquakechile.reports_feature.data.local.entity.ReportWithQuakeCity


class ReportRemoteDataSource(
    private val application: Application
) {

    private val service: ReportAPI by lazy {
        (application as ApplicationController).apiService.create(ReportAPI::class.java)
    }

    suspend fun getReports(): List<ReportWithQuakeCity> {

        val call = service.listReports()

        return call.body()?.reportes?.map { it ->

            val reportEntity = it.toReportEntity()
            val topCities = it.top_ciudades.map { it.toQuakeCityEntity() }

            ReportWithQuakeCity(
                report = reportEntity,
                topCities = topCities
            )
        }!!
    }
}
