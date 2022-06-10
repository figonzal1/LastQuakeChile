package cl.figonzal.lastquakechile.quake_feature.ui

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
import org.junit.Test

@ExperimentalCoroutinesApi
class QuakeViewModelTest {

    private val dispatcher = StandardTestDispatcher()

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

        with(QuakeViewModel(GetQuakesUseCase(FakeQuakeRepository(dispatcher), 3))) {

            getQuakes()

            val job = launch(dispatcher) {

                //Drop init state flow & loading resource state
                val resultState: QuakeState = quakeState.drop(2).first()
                assertThat(resultState.quakes.size).isEqualTo(3)
            }
            job.cancel()
        }
    }

    @Test
    fun `limit 0 should return quakeStatus with empty List`() = runTest {
        with(QuakeViewModel(GetQuakesUseCase(FakeQuakeRepository(dispatcher), 0))) {

            getQuakes()

            val job = launch(dispatcher) {

                val result = quakeState.drop(2).first()
                assertThat(result.quakes.size).isEqualTo(0)
            }
            job.cancel()
        }
    }

    @Test
    fun `network error should activate errorStatus`() = runTest {
        val fakeRepo = FakeQuakeRepository(dispatcher)
        fakeRepo.shouldReturnNetworkError = true

        val viewModel = QuakeViewModel(GetQuakesUseCase(fakeRepo, 3))

        viewModel.getQuakes()

        val job = launch(dispatcher) {
            val result = viewModel.errorStatus.first()
            assertThat(result).isEqualTo("Test network error")
        }

        job.cancel()
    }
}