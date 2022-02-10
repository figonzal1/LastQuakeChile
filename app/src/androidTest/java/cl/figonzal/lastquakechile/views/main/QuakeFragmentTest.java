package cl.figonzal.lastquakechile.views.main;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

import android.content.Context;
import android.content.SharedPreferences;
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
import cl.figonzal.lastquakechile.core.ui.MainActivity;


@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@LargeTest
public class QuakeFragmentTest {

    private static final int TIME_TO_TEST = 5000;
    @Rule
    public final ActivityScenarioRule<MainActivity> rule = new ActivityScenarioRule<>(MainActivity.class);
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

        SharedPreferences sharedPreferences = mContext.getSharedPreferences(mContext.getString(R.string.SHARED_PREF_MASTER_KEY), Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(mContext.getString(R.string.SHARED_PREF_STATUS_CARD_VIEW_INFO), true);
        editor.apply();
    }

    @Test
    public void test1_click_on_item() {

        ViewInteraction quakeItem = onView(
                allOf(withId(R.id.card_view),
                        childAtPosition(
                                allOf(withId(R.id.recycle_view_quakes),
                                        withContentDescription(mContext.getString(R.string.seccion_listado_de_sismos)),
                                        childAtPosition(
                                                withClassName(Matchers.is("androidx" +
                                                        ".constraintlayout.widget" +
                                                        ".ConstraintLayout")),
                                                3)),
                                0),
                        isDisplayed()));
        quakeItem.perform(click());
    }

    @Test
    public void test2_click_card_view_info() {

        ViewInteraction button = onView(
                allOf(withId(R.id.btn_info_accept),
                        withText(mContext.getString(R.string.last_quakes_info_card_view_button)),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.card_view_info),
                                        0),
                                2),
                        isDisplayed()));
        button.perform(click());

        try {
            Thread.sleep(TIME_TO_TEST);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
