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
import cl.figonzal.lastquakechile.core.utils.views.REPORT_FORMAT
import cl.figonzal.lastquakechile.core.utils.views.getMonth
import cl.figonzal.lastquakechile.utils.checkRecyclerSubViews
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.KoinTest
import org.koin.test.inject
import java.time.LocalDateTime


@MediumTest
@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class ReportsFragmentTest : KoinTest {

    private lateinit var context: Context
    private var now = LocalDateTime.now()

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun checkIfRecyclerView_isDisplayed() {

        launchFragmentInContainer<ReportsFragment>(
            themeResId = R.style.AppTheme
        )

        Thread.sleep(2000)

        onView(withId(R.id.recycle_view_reports))
            .check(matches(isDisplayed()))
    }

    @Test
    fun checkIfReport_showCorrectData() {

        launchFragmentInContainer<ReportsFragment>(
            themeResId = R.style.AppTheme
        )

        Thread.sleep(2000)

        //Check first position
        checkRecyclerSubViews(
            R.id.recycle_view_reports, 0, withText(
                String.format(
                    REPORT_FORMAT,
                    context.getMonth(now.monthValue),
                    now.year
                )
            ), R.id.tv_title_report
        )

        //CHeck second position
        checkRecyclerSubViews(
            R.id.recycle_view_reports, 1, withText(
                String.format(
                    REPORT_FORMAT,
                    context.getMonth(now.minusMonths(1).monthValue),
                    now.minusMonths(1).year
                )
            ), R.id.tv_title_report
        )

        Thread.sleep(2000)
    }

}