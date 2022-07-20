package cl.figonzal.lastquakechile.reports_feature.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import cl.figonzal.lastquakechile.core.AppDatabase
import cl.figonzal.lastquakechile.reports_feature.data.local.entity.CityQuakesEntity
import cl.figonzal.lastquakechile.reports_feature.data.local.entity.ReportEntity
import cl.figonzal.lastquakechile.reports_feature.data.local.entity.relation.ReportWithCityQuakes
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
class ReportDAOTest : KoinTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private val database: AppDatabase by inject()
    private lateinit var reportDAO: ReportDAO

    @Before
    fun setUp() {
        reportDAO = database.reportDao()
    }

    @Test
    fun insertCityQuakes() = runTest {

        val cityQuakesEntity = CityQuakesEntity(
            city = "La Serena",
            nQuakes = 4,
        )

        val id = reportDAO.insertCityQuakes(
            listOf(cityQuakesEntity)
        )

        assertThat(id).isNotNull()
    }

    @Test
    fun insertReport() = runTest {

        val reportEntity = ReportEntity(
            id = 1,
            reportMonth = "Diciembre",
            nSensitive = 12,
            nQuakes = 450,
            promMagnitude = 4.23,
            promDepth = 159.34,
            maxMagnitude = 6.8,
            minDepth = 2.3
        )

        val id = reportDAO.insertReport(reportEntity)

        assertThat(id).isNotNull()
    }

    @Test
    fun insertFullReport() = runTest {
        val reportEntity = ReportEntity(
            id = 1,
            reportMonth = "Diciembre",
            nSensitive = 12,
            nQuakes = 450,
            promMagnitude = 4.23,
            promDepth = 159.34,
            maxMagnitude = 6.8,
            minDepth = 2.3
        )

        val cityQuakes = CityQuakesEntity(
            1,
            city = "La Serena",
            nQuakes = 10,
            1
        )

        val reportWithCityQuakes = ReportWithCityQuakes(
            reportEntity, listOf(cityQuakes)
        )

        reportDAO.insertAll(reportWithCityQuakes)

        val result = reportDAO.getAll().first()

        assertThat(result).isEqualTo(reportWithCityQuakes)
    }

    @Test
    fun deleteAllReport() = runTest {
        val reportEntity = ReportEntity(
            id = 1,
            reportMonth = "Diciembre",
            nSensitive = 12,
            nQuakes = 450,
            promMagnitude = 4.23,
            promDepth = 159.34,
            maxMagnitude = 6.8,
            minDepth = 2.3
        )

        val reportEntity2 = ReportEntity(
            id = 2,
            reportMonth = "Enero",
            nSensitive = 50,
            nQuakes = 680,
            promMagnitude = 4.23,
            promDepth = 159.34,
            maxMagnitude = 6.8,
            minDepth = 2.3
        )

        reportDAO.insertReport(reportEntity)
        reportDAO.insertReport(reportEntity2)

        reportDAO.deleteAllReports()

        val result = reportDAO.getAll()

        assertThat(result.size).isEqualTo(0)
    }
}