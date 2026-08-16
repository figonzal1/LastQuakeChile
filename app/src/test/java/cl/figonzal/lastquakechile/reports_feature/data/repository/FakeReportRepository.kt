package cl.figonzal.lastquakechile.reports_feature.data.repository

import cl.figonzal.lastquakechile.core.domain.DomainError
import cl.figonzal.lastquakechile.core.domain.DomainResult
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

    var shouldReturnNetworkError = false

    private val reportList = listOf(
        Report(
            reportMonth = "Enero",
            nSensitive = 12,
            nQuakes = 450,
            promMagnitude = 5.78,
            promDepth = 23.4,
            maxMagnitude = 7.8,
            minDepth = 3.0,
            cityQuakes = listOf(
                CityQuakes("La Serena", 15),
                CityQuakes("Santiago", 20),
                CityQuakes("Valparaiso", 22)
            )
        ),
        Report(
            reportMonth = "Marzo",
            nSensitive = 12,
            nQuakes = 363,
            promMagnitude = 4.23,
            promDepth = 23.4,
            maxMagnitude = 7.8,
            minDepth = 3.0,
            cityQuakes = listOf(
                CityQuakes("La Serena", 15),
                CityQuakes("Santiago", 20),
                CityQuakes("Valparaiso", 22)
            )
        ),
    )

    override fun getReports(pageIndex: Int) = when (pageIndex) {
        0 -> getFirstPage(pageIndex)
        else -> getNextPages(pageIndex)
    }

    private fun getFirstPage(pageIndex: Int): Flow<DomainResult<List<Report>>> = flow {
        when {
            shouldReturnNetworkError -> emit(DomainResult.Error(reportList, DomainError.HttpError))
            else -> emit(DomainResult.Success(reportList))
        }
    }.flowOn(dispatcher)

    private fun getNextPages(pageIndex: Int): Flow<DomainResult<List<Report>>> = flow {
        when {
            shouldReturnNetworkError -> emit(DomainResult.Error(reportList, DomainError.HttpError))
            else -> emit(DomainResult.Success(reportList))
        }
    }.flowOn(dispatcher)
}
