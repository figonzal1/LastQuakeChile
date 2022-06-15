package cl.figonzal.lastquakechile.reports_feature.ui

import android.content.Context
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import cl.figonzal.lastquakechile.R
import cl.figonzal.lastquakechile.core.utils.TestFragmentFactory
import cl.figonzal.lastquakechile.core.utils.getMonth
import cl.figonzal.lastquakechile.utils.checkRecyclerSubViews
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.KoinTest
import org.koin.test.inject


@MediumTest
@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class ReportsFragmentTest : KoinTest {

    private lateinit var context: Context
    private val testFragmentFactory: TestFragmentFactory by inject()

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun checkIfRecyclerView_isDisplayed() {

        Thread.sleep(2000)

        launchFragmentInContainer<ReportsFragment>(
            factory = testFragmentFactory,
            themeResId = R.style.AppTheme
        )

        onView(withId(R.id.recycle_view_reports))
            .check(matches(isDisplayed()))
    }

    @Test
    fun checkIfReport_showCorrectData() {

        Thread.sleep(2000)

        launchFragmentInContainer<ReportsFragment>(
            factory = testFragmentFactory,
            themeResId = R.style.AppTheme
        )

        //Check first position
        checkRecyclerSubViews(
            R.id.recycle_view_reports, 0, withText(
                String.format(
                    context.getString(R.string.REPORT_FORMAT),
                    context.getMonth(1),
                    "2021"
                )
            ), R.id.tv_title_report
        )

        //CHeck second position
        checkRecyclerSubViews(
            R.id.recycle_view_reports, 1, withText(
                String.format(
                    context.getString(R.string.REPORT_FORMAT),
                    context.getMonth(12),
                    "2020"
                )
            ), R.id.tv_title_report
        )

        Thread.sleep(2000)
    }

}