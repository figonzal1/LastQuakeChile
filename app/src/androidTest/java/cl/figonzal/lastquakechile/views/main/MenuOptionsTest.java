package cl.figonzal.lastquakechile.views.main;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.test.espresso.ViewInteraction;
import androidx.test.filters.LargeTest;
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

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
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.core.AllOf.allOf;

@RunWith(AndroidJUnit4ClassRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@LargeTest
public class MenuOptionsTest {

	@Rule
	public ActivityTestRule<MainActivity> testRule = new ActivityTestRule<>(MainActivity.class);

	private Context mContext;
	private static final int TIME_TO_TEST = 5000;

	private static Matcher<View> childAtPosition(
			final Matcher<View> parentMatcher, final int position) {

		return new TypeSafeMatcher<View>() {
			@Override
			public void describeTo(Description description) {
				description.appendText("Child at position " + position + " in parent ");
				parentMatcher.describeTo(description);
			}

			@Override
			public boolean matchesSafely(View view) {
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
			Thread.sleep(TIME_TO_TEST);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	@Test
	public void test2_check_config_option() {

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
	public void test3_check_invitation_option() {
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
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		ViewInteraction appCompatTextView = onView(
				Matchers.allOf(withId(R.id.title), withText(mContext.getString(R.string.invite)),
						childAtPosition(
								childAtPosition(
										withId(R.id.content),
										0),
								0),
						isDisplayed()));
		appCompatTextView.check(matches(isDisplayed()));
		/*appCompatTextView.perform(click());

		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		device.pressBack();

		try {
			Thread.sleep(TIME_TO_TEST);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}*/
	}
}
