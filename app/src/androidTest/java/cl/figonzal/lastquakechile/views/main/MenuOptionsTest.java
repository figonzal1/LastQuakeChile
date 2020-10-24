package cl.figonzal.lastquakechile.views.main;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.annotation.NonNull;
import androidx.test.espresso.ViewInteraction;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import cl.figonzal.lastquakechile.R;
import cl.figonzal.lastquakechile.views.activities.MainActivity;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.core.AllOf.allOf;

@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@LargeTest
public class MenuOptionsTest {

    private static final int TIME_TO_TEST = 5000;
    @NonNull
    @Rule
    public ActivityScenarioRule<MainActivity> testRule = new ActivityScenarioRule<>(MainActivity.class);
    private Context mContext;

    @NonNull
    private static Matcher<View> childAtPosition(
            @NonNull final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(@NonNull Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(@NonNull View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }

    @Before
    public void setup() {
        mContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        //device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
    }

    @Test
    public void test1_check_menu_option() {

        try {
            Thread.sleep(7000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction overflowMenuButton = onView(
                Matchers.allOf(withContentDescription("Más opciones"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.tool_bar),
                                        2),
                                2),
                        isDisplayed()));
        overflowMenuButton.perform(click());

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test2_check_config_option() {

        try {
            Thread.sleep(TIME_TO_TEST);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction overflow = onView(
                allOf(withContentDescription(mContext.getString(R.string.more_options)),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.tool_bar),
                                        2),
                                2),
                        isDisplayed()));
        overflow.perform(click());

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction appCompatTextView = onView(
                Matchers.allOf(withId(R.id.title), withText(mContext.getString(R.string.settings)),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.content),
                                        0),
                                0),
                        isDisplayed()));
        appCompatTextView.perform(click());

        try {
            Thread.sleep(TIME_TO_TEST);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test3_check_changelog_option() {

        try {
            Thread.sleep(TIME_TO_TEST);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction overflow = onView(
                allOf(withContentDescription(R.string.more_options),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.tool_bar),
                                        2),
                                2),
                        isDisplayed()));
        overflow.perform(click());

        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction materialTextView = onView(
                Matchers.allOf(withId(R.id.title), withText(mContext.getString(R.string.change_logs)),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.content),
                                        0),
                                0),
                        isDisplayed()));
        materialTextView.perform(click());

        try {
            Thread.sleep(TIME_TO_TEST);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
