package cl.figonzal.lastquakechile.quake_feature.domain.uses_cases

import cl.figonzal.lastquakechile.fakes.FakeQuakeRepository
import cl.figonzal.lastquakechile.quake_feature.domain.repository.QuakeRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.inject
import kotlin.test.assertEquals

class GetQuakesUseCaseTest : KoinTest {

    private val quakeRepository: QuakeRepository by inject()

    @Before
    fun setUp() {
        startKoin {
            modules(
                module {
                    single<QuakeRepository> { FakeQuakeRepository() }
                }
            )
        }
    }

    @After
    fun down() {
        stopKoin()
    }

    @Test
    fun `test uses case success`() = runBlocking {
        val usesCase = GetQuakesUseCase(quakeRepository, 15)

        val lista = usesCase().first()

        assertEquals(2, lista.data!!.size)
    }

    @Test
    fun `test uses case error`() = runBlocking {
        val usesCase = GetQuakesUseCase(quakeRepository, 15)

        val lista = usesCase().last()

        assertEquals(0, lista.data!!.size)
        assert(lista.message!!.isNotEmpty())
        assertEquals("Error emission", lista.message)
    }
}


