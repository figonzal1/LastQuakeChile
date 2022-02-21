package cl.figonzal.lastquakechile.reports_feature.domain.use_case

import cl.figonzal.lastquakechile.fakes.FakeReportRepository
import cl.figonzal.lastquakechile.reports_feature.domain.repository.ReportRepository
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

class GetReportsUseCaseTest : KoinTest {

    private val reportRepository: ReportRepository by inject()

    @Before
    fun setUp() {
        startKoin {
            modules(
                module {
                    single<ReportRepository> { FakeReportRepository() }
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
        val usesCase = GetReportsUseCase(reportRepository)

        val lista = usesCase().first()

        assertEquals(1, lista.data!!.size)
    }

    @Test
    fun `test uses case error`() = runBlocking {
        val usesCase = GetReportsUseCase(reportRepository)

        val lista = usesCase().last()

        assertEquals(0, lista.data!!.size)
        assert(lista.message!!.isNotEmpty())
    }
}
