package cl.figonzal.lastquakechile.quake_feature.ui

import cl.figonzal.lastquakechile.core.utils.ApiError
import cl.figonzal.lastquakechile.quake_feature.data.repository.FakeQuakeRepository
import cl.figonzal.lastquakechile.quake_feature.domain.model.Quake
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
    fun `limit 3 should return quakeStatus with list size equal to 3`() = runTest {

        val userCase = GetQuakesUseCase(repository, 3)
        val viewModel = QuakeViewModel(userCase)

        val job = launch(dispatcher) {

            //Drop init state flow & loading resource state
            val resultState = viewModel.quakeState.drop(2).first()

            assertThat(resultState.quakes.size).isEqualTo(3)
        }
        job.cancel()

    }

    @Test
    fun `limit 0 should return quakeStatus with empty List`() = runTest {

        val userCase = GetQuakesUseCase(repository, 0)
        val viewModel = QuakeViewModel(userCase)

        viewModel.getQuakes()

        val job = launch(dispatcher) {

            val result = viewModel.quakeState.drop(2).first()
            assertThat(result.quakes.size).isEqualTo(0)
        }
        job.cancel()
    }

    @Test
    fun `network error should activate errorState`() = runTest {

        repository.shouldReturnNetworkError = true
        val useCase = GetQuakesUseCase(repository, 3)
        val viewModel = QuakeViewModel(useCase)

        viewModel.getQuakes()

        val job = launch(dispatcher) {
            val result: ApiError = viewModel.errorState.first()
            assertThat(result).isSameInstanceAs(ApiError.HttpError)
        }

        job.cancel()
    }

    @Test
    fun `network error should return chachedList`() = runTest {

        repository.shouldReturnNetworkError = true
        val useCase = GetQuakesUseCase(repository, 3)
        val viewModel = QuakeViewModel(useCase)

        viewModel.getQuakes()

        val job = launch(dispatcher) {
            val result: List<Quake> = viewModel.quakeState.first().quakes
            assertThat(result.size).isEqualTo(3)
        }

        job.cancel()
    }
}