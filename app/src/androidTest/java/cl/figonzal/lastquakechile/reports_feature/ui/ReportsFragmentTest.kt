package cl.figonzal.lastquakechile.reports_feature.ui

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import cl.figonzal.lastquakechile.R
import cl.figonzal.lastquakechile.core.utils.views.REPORT_FORMAT
import cl.figonzal.lastquakechile.core.utils.views.getMonth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.KoinTest
import java.time.LocalDateTime


@MediumTest
@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class ReportsFragmentTest : KoinTest {

    // Empty rule: the fragment (and its host activity) is launched by
    // launchFragmentInContainer, so we only hook into the existing Compose tree.
    @get:Rule
    val composeTestRule = createEmptyComposeRule()

    private lateinit var context: Context
    private val now = LocalDateTime.now()

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun checkIfReports_areDisplayed() {

        launchFragmentInContainer<ReportsFragment>(themeResId = R.style.AppTheme)

        val firstTitle = String.format(
            REPORT_FORMAT,
            context.getMonth(now.monthValue),
            now.year
        )

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithText(firstTitle).fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithText(firstTitle).assertIsDisplayed()
    }

    @Test
    fun checkIfReport_showCorrectData() {

        launchFragmentInContainer<ReportsFragment>(themeResId = R.style.AppTheme)

        val firstTitle = String.format(
            REPORT_FORMAT,
            context.getMonth(now.monthValue),
            now.year
        )
        val secondTitle = String.format(
            REPORT_FORMAT,
            context.getMonth(now.minusMonths(1).monthValue),
            now.minusMonths(1).year
        )

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithText(firstTitle).fetchSemanticsNodes().isNotEmpty()
        }

        // First report card title
        composeTestRule.onNodeWithText(firstTitle).assertIsDisplayed()

        // Second report card title
        composeTestRule.onNodeWithText(secondTitle).assertIsDisplayed()
    }
}
