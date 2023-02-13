package cl.figonzal.lastquakechile.reports_feature.ui.viewmodel

import app.cash.turbine.test
import cl.figonzal.lastquakechile.core.data.remote.ApiError
import cl.figonzal.lastquakechile.reports_feature.data.repository.FakeReportRepository
import cl.figonzal.lastquakechile.reports_feature.domain.use_case.GetReportsUseCase
import cl.figonzal.lastquakechile.reports_feature.ui.ReportViewModel
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.drop
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
class NextPageTest : KoinTest {

    private val dispatcher = StandardTestDispatcher()
    private val repository: FakeReportRepository by inject()
    private lateinit var viewModel: ReportViewModel

    @get:Rule
    val koinTestRule = KoinTestRule.create {
        modules(module { single { FakeReportRepository(dispatcher) } })
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)

        val useCase = GetReportsUseCase(repository)
        viewModel = ReportViewModel(useCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `check default state`() = runTest {

        viewModel.nextPagesState.test {
            val emission = awaitItem()

            assertThat(emission.reports.size).isEqualTo(0)
            assertThat(emission.apiError).isNull()
            assertThat(emission.isLoading).isFalse()
        }
    }

    @Test
    fun `check loading state`() = runTest {

        viewModel.getNextPageReports()

        //Drop default state
        viewModel.nextPagesState.drop(1).test {
            val emission = awaitItem()

            assertThat(emission.isLoading).isTrue()
            assertThat(emission.apiError).isNull()
            assertThat(emission.reports.size).isEqualTo(0)
        }
    }

    @Test
    fun `check nextPagesState with Status Api Success then return quakes`() = runTest {

        viewModel.getNextPageReports()

        //Drop 2 states (Default & loading state)
        viewModel.nextPagesState.drop(2).test {
            val emission = awaitItem()
            assertThat(emission.reports.size).isEqualTo(2)
            assertThat(emission.isLoading).isFalse()
            assertThat(emission.apiError).isNull()
        }
    }

    @Test
    fun `check nextPagesState with Status Api Error then return error`() = runTest {

        repository.shouldReturnNetworkError = true
        viewModel.getNextPageReports()

        viewModel.nextPagesState.drop(2).test {

            val emission = awaitItem()

            assertThat(emission.reports.size).isEqualTo(2)
            assertThat(emission.isLoading).isFalse()
            assertThat(emission.apiError).isSameInstanceAs(ApiError.HttpError)
        }
    }
}