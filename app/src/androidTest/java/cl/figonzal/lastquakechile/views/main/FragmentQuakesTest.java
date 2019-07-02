package cl.figonzal.lastquakechile.views.main;

import android.content.Context;
import android.content.SharedPreferences;
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
import cl.figonzal.lastquakechile.views.MainActivity;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;


@RunWith(AndroidJUnit4ClassRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@LargeTest
public class FragmentQuakesTest {

	@Rule
	public final ActivityTestRule<MainActivity> testRule =
			new ActivityTestRule<>(MainActivity.class);

	private static final int TIME_TO_TEST = 5000;
	private Context mContext;

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

		SharedPreferences sharedPreferences =
				testRule.getActivity().getSharedPreferences(mContext.getString(R.string.MAIN_SHARED_PREF_KEY), Context.MODE_PRIVATE);

		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putBoolean(mContext.getString(R.string.SHARED_PREF_STATUS_CARD_VIEW_INFO), true);
		editor.apply();
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

	@Test
	public void test1_click_on_item() {

		ViewInteraction quakeItem = onView(
				allOf(withId(R.id.card_view),
						childAtPosition(
								allOf(withId(R.id.recycle_view),
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

}
