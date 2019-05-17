package cl.figonzal.lastquakechile.views.settings;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.test.espresso.ViewInteraction;
import androidx.test.filters.LargeTest;
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;
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
import cl.figonzal.lastquakechile.SettingsActivity;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.withDecorView;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsNot.not;

@RunWith(AndroidJUnit4ClassRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@LargeTest
public class NightModePrefTest {

	@Rule
	public final ActivityTestRule<SettingsActivity> testRule =
			new ActivityTestRule<>(SettingsActivity.class);

	private Activity mActivity;

	private static Matcher<View> childAtPosition (
			final Matcher<View> parentMatcher, final int position) {

		return new TypeSafeMatcher<View>() {
			@Override
			public void describeTo (Description description) {
				description.appendText("Child at position " + position + " in parent ");
				parentMatcher.describeTo(description);
			}

			@Override
			public boolean matchesSafely (View view) {
				ViewParent parent = view.getParent();
				return parent instanceof ViewGroup && parentMatcher.matches(parent)
						&& view.equals(((ViewGroup) parent).getChildAt(position));
			}
		};
	}

	@Before
	public void setup () {
		mActivity = testRule.getActivity();
	}

	@Test
	public void test1_check_night_mode_automatic_off () {

		ViewInteraction switchAuto = onView(
				Matchers.allOf(childAtPosition(
						Matchers.allOf(withId(R.id.recycler_view),
								childAtPosition(
										withId(android.R.id.list_container),
										0)),
						4),
						isDisplayed()));

		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		//Realizar click
		switchAuto.perform(click());

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		//Checkear display de Toast de shared pref TRUE
		onView(withText(R.string.NIGHT_MODE_AUTO_KEY_TOAST_OFF)).inRoot(withDecorView(not(is(mActivity.getWindow().getDecorView()))))
				.check(matches(isDisplayed()));

		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void test2_check_night_mode_manual_on () {

		ViewInteraction switchManual = onView(
				Matchers.allOf(childAtPosition(
						Matchers.allOf(withId(R.id.recycler_view),
								childAtPosition(
										withId(android.R.id.list_container),
										0)),
						3),
						isDisplayed()));

		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		//Realizar click
		switchManual.perform(click());

		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		//Checkear display de Toast de shared pref TRUE
		onView(withText(R.string.NIGHT_MODE_MANUAL_KEY_TOAST_ON)).inRoot(withDecorView(not(is(mActivity.getWindow().getDecorView()))))
				.check(matches(isDisplayed()));

		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void test3_check_night_mode_manual_off () {

		ViewInteraction switchManual = onView(
				Matchers.allOf(childAtPosition(
						Matchers.allOf(withId(R.id.recycler_view),
								childAtPosition(
										withId(android.R.id.list_container),
										0)),
						3),
						isDisplayed()));

		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		//Realizar click
		switchManual.perform(click());

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		//Checkear display de Toast de shared pref TRUE
		onView(withText(R.string.NIGHT_MODE_MANUAL_KEY_TOAST_OFF)).inRoot(withDecorView(not(is(mActivity.getWindow().getDecorView()))))
				.check(matches(isDisplayed()));

		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void test4_check_night_mode_automatic_on () {

		ViewInteraction switchAuto = onView(
				Matchers.allOf(childAtPosition(
						Matchers.allOf(withId(R.id.recycler_view),
								childAtPosition(
										withId(android.R.id.list_container),
										0)),
						4),
						isDisplayed()));

		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		//Realizar click
		switchAuto.perform(click());

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		//Checkear display de Toast de shared pref TRUE
		onView(withText(R.string.NIGHT_MODE_AUTO_KEY_TOAST_ON)).inRoot(withDecorView(not(is(mActivity.getWindow().getDecorView()))))
				.check(matches(isDisplayed()));

		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}


}
