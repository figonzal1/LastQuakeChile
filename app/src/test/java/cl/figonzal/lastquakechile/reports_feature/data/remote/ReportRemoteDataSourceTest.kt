package cl.figonzal.lastquakechile.reports_feature.data.remote

import cl.figonzal.lastquakechile.fakes.FakeReportAPI
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.inject


class ReportRemoteDataSourceTest : KoinTest {

    private val dataSource: ReportRemoteDataSource by inject()

    @Before
    fun setUp() {
        startKoin {

            modules(
                module {
                    single<ReportAPI> { FakeReportAPI() }
                    single { ReportRemoteDataSource(get()) }
                }
            )
        }
    }

    @After
    fun down() {
        stopKoin()
    }

    @Test
    fun `test reportAPI response`(): Unit = runBlocking {

        val list = dataSource.getReports()

        kotlin.test.assertEquals(634, list[0].report.nQuakes)
    }


}