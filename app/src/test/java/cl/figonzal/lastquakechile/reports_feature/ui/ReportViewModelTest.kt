package cl.figonzal.lastquakechile.reports_feature.ui

import cl.figonzal.lastquakechile.reports_feature.data.repository.FakeReportRepository
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
import org.junit.Test

@ExperimentalCoroutinesApi
class ReportViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var viewModel: ReportViewModel

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

        viewModel = ReportViewModel(GetReportsUseCase(FakeReportRepository(dispatcher)))
        viewModel.getReports()

        val job = launch(dispatcher) {

            val resultState = viewModel.reportState.drop(2).first()
            assertThat(resultState.reports.size).isEqualTo(2)
        }

        job.cancel()
    }

    @Test
    fun `get reports return network error & should activate errorStatus`() = runTest {

        viewModel = ReportViewModel(GetReportsUseCase(FakeReportRepository(dispatcher).apply {
            shouldReturnNetworkError = true
        }))

        viewModel.getReports()

        val job = launch(dispatcher) {

            val result = viewModel.errorStatus.first()
            assertThat(result).isEqualTo("Test network error")
        }
        job.cancel()
    }
}