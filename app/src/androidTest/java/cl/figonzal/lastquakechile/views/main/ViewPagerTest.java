package cl.figonzal.lastquakechile.views.main;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.test.espresso.ViewInteraction;
import androidx.test.filters.LargeTest;
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;
import androidx.test.rule.ActivityTestRule;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.hamcrest.core.IsInstanceOf;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import cl.figonzal.lastquakechile.R;
import cl.figonzal.lastquakechile.views.MainActivity;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

@RunWith(AndroidJUnit4ClassRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@LargeTest
public class ViewPagerTest {

	@Rule
	public ActivityTestRule<MainActivity> testRule = new ActivityTestRule<>(MainActivity.class);

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

	@Test
	public void test1_click_tab_map () {
		//Checkear el texto mapa
		ViewInteraction tvMapa = onView(
				allOf(withText("MAPA"),
						childAtPosition(
								allOf(withContentDescription("Mapa"),
										childAtPosition(
												IsInstanceOf.<View>instanceOf(android.widget.LinearLayout.class),
												1)),
								0),
						isDisplayed()));
		tvMapa.check(matches(withText("MAPA")));

		//Hacer click en el tab de mapa
		ViewInteraction tabMapa = onView(
				allOf(withContentDescription("Mapa"),
						childAtPosition(
								childAtPosition(
										withId(R.id.tabs),
										0),
								1),
						isDisplayed()));
		tabMapa.perform(click());
	}

	@Test
	public void test2_click_tab_listado () {
		//Checkear texto Listado
		ViewInteraction tvListado = onView(
				allOf(withText("LISTADO"),
						childAtPosition(
								allOf(withContentDescription("Listado"),
										childAtPosition(
												IsInstanceOf.<View>instanceOf(android.widget.LinearLayout.class),
												0)),
								0),
						isDisplayed()));
		tvListado.check(matches(withText("LISTADO")));

		//Hacer click sobre el tab
		ViewInteraction tabListado = onView(
				allOf(withContentDescription("Listado"),
						childAtPosition(
								childAtPosition(
										withId(R.id.tabs),
										0),
								0),
						isDisplayed()));
		tabListado.perform(click());
	}
}
