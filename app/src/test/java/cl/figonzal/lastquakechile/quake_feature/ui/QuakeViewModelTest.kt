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
    fun `return list with size equal to 1`() = runTest {

        with(QuakeViewModel(GetQuakesUseCase(FakeQuakeRepository(dispatcher), 3))) {

            getQuakes()

            val job = launch(dispatcher) {

                val result = quakeState.drop(2).first()
                assertThat(result.quakes.size).isEqualTo(3)
            }
            job.cancel()
        }
    }

    @Test
    fun `return emptyList`() = runTest {
        with(QuakeViewModel(GetQuakesUseCase(FakeQuakeRepository(dispatcher), 0))) {

            getQuakes()

            val job = launch(dispatcher) {

                val result = quakeState.drop(2).first()
                assertThat(result.quakes.size).isEqualTo(0)
            }
            job.cancel()
        }
    }
}