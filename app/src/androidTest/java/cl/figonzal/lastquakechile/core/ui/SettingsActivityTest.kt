package cl.figonzal.lastquakechile.core.ui


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Root
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItem
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import cl.figonzal.lastquakechile.BuildConfig
import cl.figonzal.lastquakechile.R
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.*
import org.hamcrest.TypeSafeMatcher
import org.hamcrest.core.IsInstanceOf.instanceOf
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters


@LargeTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(AndroidJUnit4::class)
class SettingsActivityTest {

    @get:Rule
    val rule = ActivityScenarioRule(SettingsActivity::class.java)

    private lateinit var context: Context
    private lateinit var activity: Activity

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()

        val scenario = ActivityScenario.launch(SettingsActivity::class.java)
        scenario.onActivity { activity -> this.activity = activity }
    }

    @Test
    fun test1_checkQuakeDataPreferences_correctText() {

        Thread.sleep(2000)

        //TOOLBAR TITLE
        onView(
            allOf(
                withText(context.getString(R.string.settings)),
                withParent(
                    allOf(
                        withId(R.id.materialToolbar),
                        withParent(instanceOf(LinearLayout::class.java))
                    )
                ),
                isDisplayed()
            )
        ).check(matches(withText(context.getString(R.string.settings))))

        //Quake Data
        onView(
            allOf(
                withId(android.R.id.title),
                withText(context.getString(R.string.quake_list_number_pref_title))
            )
        ).check(matches(isDisplayed()))
            .check(matches(withText(context.getString(R.string.quake_list_number_pref_title))))

        //Quake Data summary
        onView(
            allOf(
                withId(android.R.id.summary),
                withText(context.getString(R.string.quake_list_number_pref_summary)),
                isDisplayed()
            )
        ).check(matches(withText(context.getString(R.string.quake_list_number_pref_summary))))

        //Quake limit summary
        onView(
            allOf(
                withId(android.R.id.summary), withText(
                    context.resources.getQuantityString(
                        R.plurals.list_quake_number_summary,
                        15,
                        15
                    )
                ),
                isDisplayed()
            )
        ).check(
            matches(
                withText(
                    context.resources.getQuantityString(R.plurals.list_quake_number_summary, 15, 15)
                )
            )
        )

        Thread.sleep(2000)
    }

    @Test
    fun test2_checkNotificationPreferences_correctText() {

        Thread.sleep(2000)

        //NOTIFICATIONS CATEGORY TITLE
        onView(
            allOf(
                withId(android.R.id.title), withText(context.getString(R.string.alert_pref_title)),
                isDisplayed()
            )
        ).check(matches(withText(context.getString(R.string.alert_pref_title))))

        //NOTIFICATIONS CATEGORY SUMMARY
        onView(
            allOf(
                withId(android.R.id.summary),
                withText(context.getString(R.string.alert_pref_summary)),
                isDisplayed()
            )
        ).check(matches(withText(context.getString(R.string.alert_pref_summary))))

        Thread.sleep(2000)

        //NOTIFICATIONS PREFERENCE TITLE
        onView(
            allOf(
                withId(android.R.id.title),
                withText(context.getString(R.string.alert_pref_title_switch)),
                isDisplayed()
            )
        ).check(matches(withText(context.getString(R.string.alert_pref_title_switch))))

        Thread.sleep(2000)

        //NOTIFICATION PREFECENCE SUMMARY
        onView(
            allOf(
                withId(android.R.id.summary),
                withText(context.getString(R.string.alert_pref_summary_on)),
                isDisplayed()
            )
        ).check(matches(withText(context.getString(R.string.alert_pref_summary_on))))


        Thread.sleep(2000)
    }

    @Test
    fun test3_checkAboutPreferences_correctText() {

        Thread.sleep(2000)

        //PREFERENCE CATEGORY TITLE
        onView(
            allOf(
                withId(android.R.id.title),
                withText(context.getString(R.string.about)),
                isDisplayed()
            )
        ).check(matches(withText(context.getString(R.string.about))))

        //ABOUT PREFERENCE SUMMARY
        onView(
            allOf(
                withId(android.R.id.summary),
                withText(context.getString(R.string.about_msg)),
                isDisplayed()
            )
        ).check(matches(withText(context.getString(R.string.about_msg))))

        Thread.sleep(2000)

        //VERSION PREFERENCE TITLE
        onView(
            allOf(
                withId(android.R.id.title),
                withText(context.getString(R.string.version)),
                isDisplayed()
            )
        ).check(matches(withText(context.getString(R.string.version))))

        //VERSION PREFERENCE SUMMARY
        onView(
            allOf(
                withId(android.R.id.summary),
                withText(BuildConfig.VERSION_NAME),
                isDisplayed()
            )
        ).check(matches(withText(BuildConfig.VERSION_NAME)))

        Thread.sleep(2000)

        //CONTACT PREFERENCE TITLE
        onView(
            allOf(
                withId(android.R.id.title),
                withText(context.getString(R.string.contact_developer)),
                isDisplayed()
            )
        ).check(matches(withText(context.getString(R.string.contact_developer))))

        //CONTACT PREFERENCE SUMMARY
        onView(
            allOf(
                withId(android.R.id.summary),
                withText(context.getString(R.string.contact_summary)),
                isDisplayed()
            )
        ).check(matches(withText(context.getString(R.string.contact_summary))))


        Thread.sleep(2000)
    }

    //NOT WORK IN API 31 & 32
    @Test
    fun test4_clickOnAlertPreference_deactivateAlert() {

        Thread.sleep(2000)

        onView(withId(androidx.preference.R.id.recycler_view))
            .perform(
                actionOnItem<RecyclerView.ViewHolder>(
                    hasDescendant(withText(R.string.alert_pref_title_switch)), click()
                )
            )

        //Checkear display de Toast de shared pref TRUE
        onView(withText(context.getString(R.string.firebase_pref_key_alert_off)))
            .inRoot(ToastMatcher())
            .check(matches(isDisplayed()))
    }

    //NOT WORK IN API 31 & 32
    @Test
    fun test5_clickOnAlertPreference_activatedAlert() {

        Thread.sleep(2000)

        onView(withId(androidx.preference.R.id.recycler_view))
            .perform(
                actionOnItem<RecyclerView.ViewHolder>(
                    hasDescendant(withText(R.string.alert_pref_title_switch)), click()
                )
            )

        //Checkear display de Toast de shared pref TRUE
        onView(withText(context.getString(R.string.firebase_pref_key_alert_on)))
            .inRoot(ToastMatcher())
            .check(matches(isDisplayed()))
    }

    @Test
    fun test6_clickOnContactDeveloper_openIntent() {

        Thread.sleep(2000)

        Intents.init()

        //CLick on contact developer
        onView(
            allOf(
                withId(androidx.preference.R.id.recycler_view),
                childAtPosition(withId(android.R.id.list_container), 0)
            )
        ).perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                7,
                click()
            )
        )

        val expectedIntent = allOf(
            hasAction(Intent.ACTION_CHOOSER),
            hasExtra(
                equalTo(Intent.EXTRA_INTENT),
                allOf(
                    hasExtra(
                        `is`(Intent.EXTRA_EMAIL),
                        `is`(arrayOf(context.getString(R.string.mail_to_felipe)))
                    ),
                    hasExtra(
                        `is`(Intent.EXTRA_SUBJECT),
                        `is`(context.getString(R.string.email_subject))
                    )
                )
            ),
            hasExtra(
                `is`(Intent.EXTRA_TITLE),
                `is`(context.getString(R.string.email_chooser_title))
            )
        )

        intended(expectedIntent)

        UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()).pressBack()

        Intents.release()

        Thread.sleep(2000)
    }

    private fun childAtPosition(
        parentMatcher: Matcher<View>, position: Int
    ): Matcher<View> {

        return object : TypeSafeMatcher<View>() {
            override fun describeTo(description: Description) {
                description.appendText("Child at position $position in parent ")
                parentMatcher.describeTo(description)
            }

            public override fun matchesSafely(view: View): Boolean {
                val parent = view.parent
                return parent is ViewGroup && parentMatcher.matches(parent)
                        && view == parent.getChildAt(position)
            }
        }
    }

    class ToastMatcher : TypeSafeMatcher<Root>() {
        override fun describeTo(description: Description) {
            description.appendText("is toast")
        }

        public override fun matchesSafely(root: Root): Boolean {
            val type = root.windowLayoutParams.get().type
            if (type == WindowManager.LayoutParams.TYPE_TOAST) {
                val windowToken = root.decorView.windowToken
                val appToken = root.decorView.applicationWindowToken
                if (windowToken === appToken) {
                    // windowToken == appToken means this window isn't contained by any other windows.
                    // if it was a window for an activity, it would have TYPE_BASE_APPLICATION.
                    return true
                }
            }
            return false
        }
    }
}
