package cl.figonzal.lastquakechile.quake_feature.data.local

import cl.figonzal.lastquakechile.fakes.FakeQuakeDAO
import cl.figonzal.lastquakechile.quake_feature.data.local.entity.QuakeEntity
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.inject


class QuakeLocalDataSourceTest : KoinTest {

    private val dataSource: QuakeLocalDataSource by inject()

    @Before
    fun setUp() {
        startKoin {

            modules(
                module {
                    single<QuakeDAO> { FakeQuakeDAO() }
                    single { QuakeLocalDataSource(get()) }
                }
            )
        }
    }

    @After
    fun down() {
        stopKoin()
    }

    @Test
    fun `test getQuake in DAO`() {

        with(dataSource.getQuakes()) {
            assertEquals(2, size)
            assertEquals(-30.447, this[0].latitude, 0.02)
            assertEquals("Huasco", this[1].city)
        }
    }

    @Test
    fun `test insert in DAO`() {

        val quakeEntity = QuakeEntity(
            id = 3,
            quakeCode = 67345,
            utcDate = "2022-02-19 17:12:05",
            city = "La Serena",
            reference = "2 km al NO de La Serena",
            magnitude = 5.9,
            scale = "Ml",
            isSensitive = true,
            latitude = -29.244,
            longitude = -71.578,
            depth = 55.00,
            isVerified = false
        )
        dataSource.insert(quakeEntity)

        assertEquals(3, dataSource.getQuakes().size)
    }

    @Test
    fun `test delete in DAO`() {
        dataSource.deleteAll()
        assert(dataSource.getQuakes().isEmpty())
    }
}