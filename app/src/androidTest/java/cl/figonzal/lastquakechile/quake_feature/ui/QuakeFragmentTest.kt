package cl.figonzal.lastquakechile.quake_feature.ui

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import cl.figonzal.lastquakechile.R
import cl.figonzal.lastquakechile.core.utils.TestFragmentFactory
import cl.figonzal.lastquakechile.utils.checkRecyclerSubViews
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.KoinTest
import org.koin.test.inject
import java.text.DecimalFormatSymbols


@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@MediumTest
class QuakeFragmentTest : KoinTest {

    private val testFragmentFactory: TestFragmentFactory by inject()

    @Test
    fun checkIfRecyclerView_isDisplayed() {

        Thread.sleep(2000)

        launchFragmentInContainer<QuakeFragment>(
            factory = testFragmentFactory,
            themeResId = R.style.AppTheme
        )

        onView(withId(R.id.recycle_view_quakes)).check(matches(isDisplayed()))
    }

    @Test
    fun checkQuakeData_matchCorrectly() {

        val separator: Char = DecimalFormatSymbols.getInstance().decimalSeparator

        Thread.sleep(2000)

        launchFragmentInContainer<QuakeFragment>(
            factory = testFragmentFactory,
            themeResId = R.style.AppTheme
        )

        //POSITION 0
        checkRecyclerSubViews(R.id.recycle_view_quakes, 0, withText("La Serena"), R.id.tv_city)
        checkRecyclerSubViews(
            R.id.recycle_view_quakes,
            0,
            withText("45km al OS de La Serena"),
            R.id.tv_reference
        )
        checkRecyclerSubViews(
            R.id.recycle_view_quakes,
            0,
            withText("3${separator}6"),
            R.id.tv_magnitude
        )


        //POSITION 1
        checkRecyclerSubViews(R.id.recycle_view_quakes, 1, withText("Concepción"), R.id.tv_city)
        checkRecyclerSubViews(
            R.id.recycle_view_quakes,
            1,
            withText("14km al OS de Concpeción"),
            R.id.tv_reference
        )
        checkRecyclerSubViews(
            R.id.recycle_view_quakes,
            1,
            withText("6${separator}6"),
            R.id.tv_magnitude
        )

        Thread.sleep(2000)
    }

    @Test
    fun clickOnQuakeItem_openDetailActivity() {

        Intents.init()

        launchFragmentInContainer<QuakeFragment>(
            factory = testFragmentFactory,
            themeResId = R.style.AppTheme
        )

        Thread.sleep(2000)

        onView(withId(R.id.recycle_view_quakes)).perform(
            RecyclerViewActions.actionOnItemAtPosition<QuakeAdapter.QuakeViewHolder>(
                0,
                click()
            )
        )

        //Check if activity has open
        Intents.intended(hasComponent(QuakeDetailsActivity::class.java.name))

        Intents.release()

        Thread.sleep(2000)
    }
}