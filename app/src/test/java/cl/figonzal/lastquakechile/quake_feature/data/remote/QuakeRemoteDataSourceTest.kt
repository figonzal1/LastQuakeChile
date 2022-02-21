package cl.figonzal.lastquakechile.quake_feature.data.remote

import cl.figonzal.lastquakechile.fakes.FakeQuakeAPI
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.inject

class QuakeRemoteDataSourceTest : KoinTest {

    private val dataSource: QuakeRemoteDataSource by inject()

    @Before
    fun setUp() {
        startKoin {

            modules(
                module {
                    single<QuakeAPI> { FakeQuakeAPI() }
                    single { QuakeRemoteDataSource(get()) }
                }
            )
        }
    }

    @After
    fun down() {
        stopKoin()
    }

    @Test
    fun `test quakeAPI response`(): Unit = runBlocking {

        val list = dataSource.getQuakes(1)

        kotlin.test.assertEquals(2.5, list[0].magnitude)
    }
}