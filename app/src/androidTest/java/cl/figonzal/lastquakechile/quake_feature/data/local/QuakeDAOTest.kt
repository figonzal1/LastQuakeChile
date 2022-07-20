package cl.figonzal.lastquakechile.quake_feature.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import cl.figonzal.lastquakechile.core.AppDatabase
import cl.figonzal.lastquakechile.quake_feature.data.local.entity.CoordinateEntity
import cl.figonzal.lastquakechile.quake_feature.data.local.entity.QuakeEntity
import cl.figonzal.lastquakechile.quake_feature.data.local.entity.relation.QuakeAndCoordinate
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.KoinTest
import org.koin.test.inject

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@SmallTest
class QuakeDAOTest : KoinTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private val database: AppDatabase by inject()
    private lateinit var quakeDAO: QuakeDAO

    @Before
    fun setUp() {
        quakeDAO = database.quakeDao()
    }

    @Test
    fun insertQuake() = runTest {

        val quakeEntity = QuakeEntity(
            id = 1,
            quakeCode = 123,
            utcDate = "2012-04-12",
            city = "La Serena",
            reference = "4km al oeste de La serena",
            magnitude = 4.5,
            depth = 45.2,
            scale = "ml",
            isSensitive = false,
            isVerified = true
        )

        val id = quakeDAO.insertQuake(quakeEntity)

        assertThat(id).isNotNull()
    }

    @Test
    fun insertCoordinate() = runTest {
        val coordinateEntity = CoordinateEntity(
            latitude = 22.6,
            longitude = -22.5
        )

        val id = quakeDAO.insertCoordinate(coordinateEntity)

        assertThat(id).isNotNull()
    }

    @Test
    fun insertFullQuake() = runTest {

        val quakeEntity = QuakeEntity(
            id = 1,
            quakeCode = 123,
            utcDate = "2012-04-12",
            city = "La Serena",
            reference = "4km al oeste de La serena",
            magnitude = 4.5,
            depth = 45.2,
            scale = "ml",
            isSensitive = false,
            isVerified = true
        )

        val coordinateEntity = CoordinateEntity(1, latitude = 22.6, longitude = -22.5, 1)

        val quakeWithCoord = QuakeAndCoordinate(quakeEntity, coordinateEntity)

        quakeDAO.insertAll(quakeWithCoord)

        val result = quakeDAO.getAll().first()

        assertThat(result).isEqualTo(quakeWithCoord)
    }

    @Test
    fun deleteAll() = runTest {
        val quakeEntity = QuakeEntity(
            id = 1,
            quakeCode = 123,
            utcDate = "2012-04-12",
            city = "La Serena",
            reference = "4km al oeste de La serena",
            magnitude = 4.5,
            depth = 45.2,
            scale = "ml",
            isSensitive = false,
            isVerified = true
        )
        val quakeEntity2 = QuakeEntity(
            id = 2,
            quakeCode = 5435,
            utcDate = "2017-04-12",
            city = "Santiago",
            reference = "4km al oeste de La serena",
            magnitude = 4.5,
            depth = 45.2,
            scale = "ml",
            isSensitive = false,
            isVerified = true
        )

        quakeDAO.insertQuake(quakeEntity)
        quakeDAO.insertQuake(quakeEntity2)

        quakeDAO.deleteAll()
        val result = quakeDAO.getAll()

        assertThat(result.size).isEqualTo(0)
    }

}