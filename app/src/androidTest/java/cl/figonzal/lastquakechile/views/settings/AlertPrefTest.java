package cl.figonzal.lastquakechile.views.settings;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.preference.PreferenceManager;
import androidx.test.espresso.ViewInteraction;
import androidx.test.filters.LargeTest;
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
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
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.AllOf.allOf;


@RunWith(AndroidJUnit4ClassRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@LargeTest
public class AlertPrefTest {

	@Rule
	public ActivityTestRule<SettingsActivity> testRule =
			new ActivityTestRule<>(SettingsActivity.class);

	private Context mContext;
	private Activity mActivity;
	private SharedPreferences.Editor editor;

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
		mContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
		mActivity = testRule.getActivity();
		editor = PreferenceManager.getDefaultSharedPreferences(testRule.getActivity()).edit();

	}

	@Test
	public void test1_check_switch_notificaciones_on () {

		editor.putBoolean(mContext.getString(R.string.FIREBASE_PREF_KEY), false);
		editor.apply();

		//Checkear si el switch esta desplegado, habilitado y es clickeable, la preferencia de
		// notificaciones
		ViewInteraction switchNotificaciones = onView(
				allOf(childAtPosition(
						allOf(withId(R.id.recycler_view),
								childAtPosition(
										withId(android.R.id.list_container),
										0)),
						1),
						isDisplayed()));

		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		//Realizar click
		switchNotificaciones.perform(click());

		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		onView(withText(R.string.FIREBASE_PREF_KEY_TOAST_ALERTAS_ON)).
				inRoot(withDecorView(not(is(mActivity.getWindow().getDecorView())))).
				check(matches(isDisplayed()));

		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void test2_check_switch_notificaciones_off () {

		//Checkear si el switch esta desplegado, habilitado y es clickeable, la preferencia de
		// notificaciones
		ViewInteraction switchNotificaciones = onView(
				allOf(childAtPosition(
						allOf(withId(R.id.recycler_view),
								childAtPosition(
										withId(android.R.id.list_container),
										0)),
						1),
						isDisplayed()));

		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		//Realizar click
		switchNotificaciones.perform(click());

		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		//Checkear display de Toast de shared pref TRUE
		onView(withText(R.string.FIREBASE_PREF_KEY_TOAST_ALERTAS_OFF)).inRoot(withDecorView(not(is(mActivity.getWindow().getDecorView()))))
				.check(matches(isDisplayed()));

		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}