package cl.figonzal.lastquakechile.quake_feature.ui

import cl.figonzal.lastquakechile.core.data.remote.ApiError
import cl.figonzal.lastquakechile.quake_feature.data.repository.FakeQuakeRepository
import cl.figonzal.lastquakechile.quake_feature.domain.uses_cases.GetQuakesUseCase
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
class QuakeViewModelTest : KoinTest {

    private val dispatcher = StandardTestDispatcher()
    private val repository: FakeQuakeRepository by inject()

    @get:Rule
    val koinTestRule = KoinTestRule.create {
        modules(
            module {
                single { FakeQuakeRepository(dispatcher) }
            }
        )
    }

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `check firstPageState with Status Api Success then return quakes`() = runTest {

        val userCase = GetQuakesUseCase(repository)
        val viewModel = QuakeViewModel(userCase)

        val job = launch(dispatcher) {

            //Drop init state flow & loading resource state
            val resultState: QuakeState = viewModel.firstPageState.drop(2).first()

            assertThat(resultState.quakes.size).isEqualTo(3)
        }
        job.cancel()

    }

    @Test
    fun `check firstPageState with Status Api Error then return error`() = runTest {

        repository.shouldReturnNetworkError = true
        val userCase = GetQuakesUseCase(repository)
        val viewModel = QuakeViewModel(userCase)

        val job = launch(dispatcher) {

            val resultState: QuakeState = viewModel.firstPageState.drop(2).first()

            assertThat(resultState.quakes.size).isEqualTo(3)
            assertThat(resultState.apiError).isSameInstanceAs(ApiError.HttpError)
        }
        job.cancel()
    }

    @Test
    fun `check geNextPages with Status Api Success then return quakes`() = runTest {

        val useCase = GetQuakesUseCase(repository)
        val viewModel = QuakeViewModel(useCase)

        val job = launch(dispatcher) {

            val resultState = viewModel.nextPagesState.drop(2).first()

            assertThat(resultState.quakes.size).isEqualTo(3)
        }
        job.cancel()
    }

    @Test
    fun `check geNextPages with Status Api Error then return error`() = runTest {

        repository.shouldReturnNetworkError = true
        val userCase = GetQuakesUseCase(repository)
        val viewModel = QuakeViewModel(userCase)

        val job = launch(dispatcher) {

            val resultState: QuakeState = viewModel.nextPagesState.drop(2).first()

            assertThat(resultState.quakes.size).isEqualTo(3)
            assertThat(resultState.apiError).isSameInstanceAs(ApiError.HttpError)
        }
        job.cancel()
    }
}