package cl.figonzal.lastquakechile.quake_feature.ui

import android.content.Context
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
import androidx.test.platform.app.InstrumentationRegistry
import cl.figonzal.lastquakechile.R
import cl.figonzal.lastquakechile.core.utils.TestFragmentFactory
import cl.figonzal.lastquakechile.utils.checkRecyclerSubViews
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.KoinTest
import org.koin.test.inject


@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@MediumTest
class QuakeFragmentTest : KoinTest {

    private lateinit var context: Context
    private val testFragmentFactory: TestFragmentFactory by inject()

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().context
    }

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
            withText("14km al OS de La Serena"),
            R.id.tv_reference
        )
        checkRecyclerSubViews(R.id.recycle_view_quakes, 0, withText("5.6"), R.id.tv_magnitude)


        //POSITION 1
        checkRecyclerSubViews(R.id.recycle_view_quakes, 1, withText("Concepción"), R.id.tv_city)
        checkRecyclerSubViews(
            R.id.recycle_view_quakes,
            1,
            withText("14km al OS de Concpeción"),
            R.id.tv_reference
        )
        checkRecyclerSubViews(R.id.recycle_view_quakes, 1, withText("7.6"), R.id.tv_magnitude)

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

        Thread.sleep(2000)
    }
}