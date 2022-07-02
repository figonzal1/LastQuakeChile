package cl.figonzal.lastquakechile.reports_feature.data.repository

import cl.figonzal.lastquakechile.core.utils.StatusAPI
import cl.figonzal.lastquakechile.reports_feature.domain.model.QuakeCity
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
            "Enero",
            12,
            450,
            5.78,
            23.4,
            7.8,
            3.0,
            listOf(
                QuakeCity("La Serena", 15),
                QuakeCity("Santiago", 20),
                QuakeCity("Valparaiso", 22)
            )
        ),
        Report(
            "Marzo",
            12,
            363,
            4.23,
            23.4,
            7.8,
            3.0,
            listOf(
                QuakeCity("La Serena", 15),
                QuakeCity("Santiago", 20),
                QuakeCity("Valparaiso", 22)
            )
        ),
    )


    override fun getReports(): Flow<StatusAPI<List<Report>>> = flow {

        emit(StatusAPI.Loading())

        when {
            shouldReturnNetworkError -> emit(StatusAPI.Error("Test network error"))
            else -> {
                emit(StatusAPI.Success(reportList))
            }
        }
    }.flowOn(dispatcher)
}