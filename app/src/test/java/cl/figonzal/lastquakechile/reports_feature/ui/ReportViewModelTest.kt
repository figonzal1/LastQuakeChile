package cl.figonzal.lastquakechile.reports_feature.ui

import cl.figonzal.lastquakechile.core.data.remote.ApiError
import cl.figonzal.lastquakechile.reports_feature.data.repository.FakeReportRepository
import cl.figonzal.lastquakechile.reports_feature.domain.model.Report
import cl.figonzal.lastquakechile.reports_feature.domain.use_case.GetReportsUseCase
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.KoinTestRule
import org.koin.test.inject

@ExperimentalCoroutinesApi
class ReportViewModelTest : KoinTest {

    private val dispatcher = StandardTestDispatcher()
    private val repository: FakeReportRepository by inject()

    @get:Rule
    val koinTestRule = KoinTestRule.create {
        modules(
            module {
                single { FakeReportRepository(dispatcher) }
            }
        )
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `get reports return normally`() = runTest {

        val useCase = GetReportsUseCase(repository)
        val viewModel = ReportViewModel(useCase)

        viewModel.getReports()

        val job = launch(dispatcher) {

            val resultState = viewModel.reportState.drop(2).first()
            assertThat(resultState.reports.size).isEqualTo(2)
        }

        job.cancel()
    }

    @Test
    fun `network error should activate errorState`() = runTest {

        repository.shouldReturnNetworkError = true
        val useCase = GetReportsUseCase(repository)
        val viewModel = ReportViewModel(useCase)

        viewModel.getReports()

        val job = launch(dispatcher) {

            val result = viewModel.errorState.first()
            assertThat(result).isSameInstanceAs(ApiError.HttpError)
        }
        job.cancel()
    }

    @Test
    fun `network error should return chachedList`() = runTest {

        repository.shouldReturnNetworkError = true
        val useCase = GetReportsUseCase(repository)
        val viewModel = ReportViewModel(useCase)

        viewModel.getReports()

        val job = launch(dispatcher) {
            val result: List<Report> = viewModel.reportState.first().reports
            assertThat(result.size).isEqualTo(2)
        }

        job.cancel()
    }
}