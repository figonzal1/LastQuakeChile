package cl.figonzal.lastquakechile.quake_feature.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import cl.figonzal.lastquakechile.core.AppDatabase
import cl.figonzal.lastquakechile.quake_feature.data.local.entity.QuakeEntity
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@SmallTest
class QuakeDAOTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: AppDatabase
    private lateinit var quakeDAO: QuakeDAO

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()

        quakeDAO = database.quakeDao()
    }

    @After
    fun clean() {
        database.close()
    }

    @Test
    fun insertQuake() = runTest {

        val quakeEntity = QuakeEntity(
            1,
            123,
            "2012-04-12",
            "La Serena",
            "4km al oeste de La serena",
            4.5,
            45.2,
            "ml",
            -74.23,
            22.34,
            isSensitive = false,
            isVerified = true
        )

        quakeDAO.insertQuake(quakeEntity)

        val result = quakeDAO.getQuakes()

        assertThat(result).contains(quakeEntity)
    }

    @Test
    fun deleteAll() = runTest {
        val quakeEntity = QuakeEntity(
            1,
            123,
            "2012-04-12",
            "La Serena",
            "4km al oeste de La serena",
            4.5,
            45.2,
            "ml",
            -74.23,
            22.34,
            isSensitive = false,
            isVerified = true
        )
        val quakeEntity2 = QuakeEntity(
            2,
            5435,
            "2017-04-12",
            "Santiago",
            "4km al oeste de La serena",
            4.5,
            45.2,
            "ml",
            -74.23,
            22.34,
            isSensitive = false,
            isVerified = true
        )

        quakeDAO.insertQuake(quakeEntity)
        quakeDAO.insertQuake(quakeEntity2)

        quakeDAO.deleteAll()
        val result = quakeDAO.getQuakes()

        assertThat(result.size).isEqualTo(0)
    }

}