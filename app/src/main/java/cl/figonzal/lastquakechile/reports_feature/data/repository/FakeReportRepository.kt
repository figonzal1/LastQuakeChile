package cl.figonzal.lastquakechile.reports_feature.data.repository

import cl.figonzal.lastquakechile.core.data.remote.ApiError
import cl.figonzal.lastquakechile.core.data.remote.NewStatusAPI
import cl.figonzal.lastquakechile.reports_feature.domain.model.CityQuakes
import cl.figonzal.lastquakechile.reports_feature.domain.model.Report
import cl.figonzal.lastquakechile.reports_feature.domain.repository.ReportRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn


class FakeReportRepository(
    private val dispatcher: CoroutineDispatcher
) : ReportRepository {

    private var shouldReturnNetworkError = false

    private val reportList = listOf(
        Report(
            reportMonth = "2021-01",
            nSensitive = 12,
            nQuakes = 450,
            promMagnitude = 5.78,
            promDepth = 23.4,
            maxMagnitude = 7.8,
            minDepth = 3.0,
            cityQuakes = listOf(
                CityQuakes("La Serena", 15),
                CityQuakes("Santiago", 20),
                CityQuakes("Valparaiso", 22),
                CityQuakes("Melipilla", 6)
            )
        ),
        Report(
            reportMonth = "2020-12",
            nSensitive = 12,
            nQuakes = 363,
            promMagnitude = 4.23,
            promDepth = 23.4,
            maxMagnitude = 7.8,
            minDepth = 3.0,
            cityQuakes = listOf(
                CityQuakes("La Serena", 15),
                CityQuakes("Santiago", 20),
                CityQuakes("Valparaiso", 22),
                CityQuakes("Concepcion", 12)
            )
        ),
    )


    override fun getReports(): Flow<NewStatusAPI<List<Report>>> = flow {

        when {
            shouldReturnNetworkError -> emit(NewStatusAPI.Error(ApiError.HttpError))
            else -> emit(NewStatusAPI.Success(reportList))
        }
    }.flowOn(dispatcher)
}