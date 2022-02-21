package cl.figonzal.lastquakechile.fakes

import cl.figonzal.lastquakechile.core.Resource
import cl.figonzal.lastquakechile.reports_feature.domain.model.QuakeCity
import cl.figonzal.lastquakechile.reports_feature.domain.model.Report
import cl.figonzal.lastquakechile.reports_feature.domain.repository.ReportRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FakeReportRepository : ReportRepository {

    private val reportList: List<Report> = listOf(
        Report(
            reportMonth = "2022-1",
            nSensitive = 123,
            nQuakes = 634,
            promMagnitud = 4.56,
            promDepth = 14.23,
            maxMagnitude = 6.7,
            minDepth = 1.0,
            topCities = listOf(
                QuakeCity("Huasco", 14),
                QuakeCity("La Serena", 55)
            )
        )
    )

    override fun getReports(): Flow<Resource<List<Report>>> = flow {

        emit(
            Resource.Success(
                data = reportList
            )
        )

        emit(
            Resource.Error(
                message = "Error emission",
                data = emptyList()
            )
        )

    }
}