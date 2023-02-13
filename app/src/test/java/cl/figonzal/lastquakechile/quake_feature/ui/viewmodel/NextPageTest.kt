package cl.figonzal.lastquakechile.quake_feature.ui.viewmodel

import app.cash.turbine.test
import cl.figonzal.lastquakechile.core.data.remote.ApiError
import cl.figonzal.lastquakechile.quake_feature.data.repository.FakeQuakeRepository
import cl.figonzal.lastquakechile.quake_feature.domain.uses_cases.GetQuakesUseCase
import cl.figonzal.lastquakechile.quake_feature.ui.QuakeViewModel
import com.google.common.truth.Truth.*
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

@OptIn(ExperimentalCoroutinesApi::class)
class NextPageTest : KoinTest {

    private val dispatcher = StandardTestDispatcher()
    private val repository: FakeQuakeRepository by inject()
    private lateinit var viewModel: QuakeViewModel

    @get:Rule
    val koinTestRule = KoinTestRule.create {
        modules(module { single { FakeQuakeRepository(dispatcher) } })
    }

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)

        val userCase = GetQuakesUseCase(repository)
        viewModel = QuakeViewModel(userCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `check default state`() = runTest {

        viewModel.nextPagesState.test {
            val emission = awaitItem()

            assertThat(emission.quakes.size).isEqualTo(0)
            assertThat(emission.apiError).isNull()
            assertThat(emission.isLoading).isFalse()
        }
    }

    @Test
    fun `check loading state`() = runTest {

        viewModel.getNextPageQuakes()

        //Drop default state
        viewModel.nextPagesState.drop(1).test {
            val emission = awaitItem()

            assertThat(emission.isLoading).isTrue()
            assertThat(emission.apiError).isNull()
            assertThat(emission.quakes.size).isEqualTo(0)
        }
    }

    @Test
    fun `check nextPagesState with Status Api Success then return quakes`() = runTest {

        viewModel.getNextPageQuakes()

        //Drop 2 states (Default & loading state)
        viewModel.nextPagesState.drop(2).test {
            val emission = awaitItem()
            assertThat(emission.quakes.size).isEqualTo(3)
            assertThat(emission.isLoading).isFalse()
            assertThat(emission.apiError).isNull()
        }
    }

    @Test
    fun `check nextPagesState with Status Api Error then return error`() = runTest {

        repository.shouldReturnNetworkError = true
        viewModel.getNextPageQuakes()

        viewModel.nextPagesState.drop(2).test {

            val emission = awaitItem()

            assertThat(emission.quakes.size).isEqualTo(3)
            assertThat(emission.isLoading).isFalse()
            assertThat(emission.apiError).isSameInstanceAs(ApiError.HttpError)
        }
    }
}