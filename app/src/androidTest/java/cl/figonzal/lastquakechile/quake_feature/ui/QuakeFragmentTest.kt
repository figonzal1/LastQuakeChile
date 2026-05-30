package cl.figonzal.lastquakechile.quake_feature.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import cl.figonzal.lastquakechile.R
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.KoinTest
import java.text.DecimalFormatSymbols

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@MediumTest
class QuakeFragmentTest : KoinTest {

    @get:Rule
    val composeTestRule = createEmptyComposeRule()

    @Test
    fun checkIfQuakes_areDisplayed() {
        launchFragmentInContainer<QuakeFragment>(themeResId = R.style.AppTheme)

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithText("La Serena").fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithText("La Serena").assertIsDisplayed()
    }

    @Test
    fun checkQuakeData_matchCorrectly() {
        val separator = DecimalFormatSymbols.getInstance().decimalSeparator

        launchFragmentInContainer<QuakeFragment>(themeResId = R.style.AppTheme)

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithText("La Serena").fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithText("La Serena").assertIsDisplayed()
        composeTestRule.onNodeWithText("45km al OS de La Serena").assertIsDisplayed()
        composeTestRule.onNodeWithText("3${separator}6").assertIsDisplayed()

        composeTestRule.onNodeWithText("Concepción").assertIsDisplayed()
        composeTestRule.onNodeWithText("14km al OS de Concpeción").assertIsDisplayed()
        composeTestRule.onNodeWithText("6${separator}6").assertIsDisplayed()
    }

    @Test
    fun clickOnQuakeItem_openDetailActivity() {
        Intents.init()

        launchFragmentInContainer<QuakeFragment>(themeResId = R.style.AppTheme)

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithText("La Serena").fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithText("La Serena").performClick()

        Intents.intended(hasComponent(QuakeDetailsActivity::class.java.name))

        Intents.release()
    }
}
