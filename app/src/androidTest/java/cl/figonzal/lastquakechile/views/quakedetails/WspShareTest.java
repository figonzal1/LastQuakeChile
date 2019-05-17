package cl.figonzal.lastquakechile.views.quakedetails;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.filters.LargeTest;
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;
import androidx.test.platform.app.InstrumentationRegistry;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import cl.figonzal.lastquakechile.R;
import cl.figonzal.lastquakechile.views.MainActivity;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isClickable;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.allOf;

@RunWith(AndroidJUnit4ClassRunner.class)
@LargeTest
public class WspShareTest {

	@Rule
	public IntentsTestRule<MainActivity> testRule =
			new IntentsTestRule<>(MainActivity.class);

	private Context mContext;

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
	public void start () {
		mContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
	}

	@Test
	public void test_click_wsp_button () {

		click_first_item();

		check_and_click_share();

		check_and_click_wsp();
	}

	private void click_first_item () {
		//Hacer click en el primer item de la lista
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
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		quakeItem.perform(click());

		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void check_and_click_share () {

		//Click sobre el boton de compartir
		ViewInteraction buttonShare = onView(withId(R.id.fab_share));

		//Checkear si esta desplegad o y es clickleable
		buttonShare.check(matches(allOf(isDisplayed(), isClickable())));

		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		//realizar click sobre el boton share
		buttonShare.perform(click());

		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void check_and_click_wsp () {

		//Buscar boton
		ViewInteraction buttonWsp = onView(withId(R.id.fab_wsp));

		//Checkear si esa desplegado y es clickcleable
		buttonWsp.check(matches(allOf(isDisplayed(), isClickable())));

		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		//Clickear boton whatsap
		/*onView(withId(R.id.fab_wsp)).perform(click());

		//Checkear que intent tenga paquete de wsp
		intended(toPackage(mContext.getString(R.string.PACKAGE_NAME_WSP)));

		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}*/
	}

}
