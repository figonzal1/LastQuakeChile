package cl.figonzal.lastquakechile.reports_feature.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import cl.figonzal.lastquakechile.core.AppDatabase
import cl.figonzal.lastquakechile.reports_feature.data.local.entity.CityQuakesEntity
import cl.figonzal.lastquakechile.reports_feature.data.local.entity.ReportEntity
import cl.figonzal.lastquakechile.reports_feature.data.local.entity.ReportWithCityQuakesEntity
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
    fun insertReport() = runTest {

        val reportEntity = ReportEntity(
            1,
            "Diciembre",
            12,
            450,
            4.23,
            159.34,
            6.8,
            2.3
        )

        val id = reportDAO.insertReport(reportEntity)

        assertThat(id).isNotNull()
    }

    @Test
    fun deleteAllReport() = runTest {
        val reportEntity = ReportEntity(
            1,
            "Diciembre",
            12,
            450,
            4.23,
            159.34,
            6.8,
            2.3
        )

        val reportEntity2 = ReportEntity(
            2,
            "Enero",
            50,
            680,
            4.23,
            159.34,
            6.8,
            2.3
        )

        reportDAO.insertReport(reportEntity)
        reportDAO.insertReport(reportEntity2)

        reportDAO.deleteAllReportEntity()

        val result = reportDAO.getReports()

        assertThat(result.size).isEqualTo(0)
    }

    @Test
    fun insertReportWithQuakeCity() = runTest {

        val topCities = listOf(
            CityQuakesEntity(1, "La Serena", 4, 1),
            CityQuakesEntity(2, "Santiago", 4, 1)
        )

        val reportWithCityQuakesEntity = ReportWithCityQuakesEntity(
            ReportEntity(
                1,
                "Diciembre",
                12,
                450,
                4.23,
                159.34,
                6.8,
                2.3
            ), topCities
        )

        val id = reportDAO.insertReport(reportWithCityQuakesEntity.report)
        reportWithCityQuakesEntity.topCities.forEach { it.idReport = id }
        reportDAO.insertAll(reportWithCityQuakesEntity.topCities)

        val result = reportDAO.getReports()

        assertThat(result).contains(reportWithCityQuakesEntity)
    }
}