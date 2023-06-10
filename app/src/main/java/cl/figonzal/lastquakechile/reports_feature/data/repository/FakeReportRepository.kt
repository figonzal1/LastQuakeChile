package cl.figonzal.lastquakechile.reports_feature.data.repository

import cl.figonzal.lastquakechile.core.data.remote.ApiError
import cl.figonzal.lastquakechile.core.data.remote.StatusAPI
import cl.figonzal.lastquakechile.reports_feature.domain.model.CityQuakes
import cl.figonzal.lastquakechile.reports_feature.domain.model.Report
import cl.figonzal.lastquakechile.reports_feature.domain.repository.ReportRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.time.LocalDateTime


class FakeReportRepository(
    private val dispatcher: CoroutineDispatcher
) : ReportRepository {

    private var shouldReturnNetworkError = false

    private var now = LocalDateTime.now()

    private val reportList = listOf(
        Report(
            reportMonth = "${now.year}-${now.monthValue}",
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
            reportMonth = "${now.minusMonths(1).year}-${now.minusMonths(1).monthValue}",
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

    override fun getReports(pageIndex: Int) = when (pageIndex) {
        0 -> getFirstPage(pageIndex)
        else -> getNextPages(pageIndex)
    }

    override fun getFirstPage(pageIndex: Int): Flow<StatusAPI<List<Report>>> = flow {
        when {
            shouldReturnNetworkError -> emit(StatusAPI.Error(reportList, ApiError.HttpError))
            else -> emit(StatusAPI.Success(reportList.take(5)))
        }
    }.flowOn(dispatcher)

    override fun getNextPages(pageIndex: Int): Flow<StatusAPI<List<Report>>> = flow {
        when {
            shouldReturnNetworkError -> emit(StatusAPI.Error(reportList, ApiError.HttpError))
            else -> emit(StatusAPI.Success(reportList.take(5)))
        }
    }.flowOn(dispatcher)
}