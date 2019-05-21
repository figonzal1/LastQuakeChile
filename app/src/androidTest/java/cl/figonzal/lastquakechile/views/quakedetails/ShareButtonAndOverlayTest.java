package cl.figonzal.lastquakechile.views.quakedetails;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import androidx.test.espresso.ViewInteraction;
import androidx.test.filters.LargeTest;
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;
import cl.figonzal.lastquakechile.R;
import cl.figonzal.lastquakechile.views.MainActivity;
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

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static org.hamcrest.Matchers.allOf;

@RunWith(AndroidJUnit4ClassRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@LargeTest
public class ShareButtonAndOverlayTest {

	@Rule
	public ActivityTestRule<MainActivity> testRule = new ActivityTestRule<>(MainActivity.class);

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
	}

	@Test
	public void test1_click_first_item_then_click_navigate_up() {
		click_first_item();

		//Boton parent
		ViewInteraction appCompatImageButton = onView(
				allOf(withContentDescription(mContext.getString(R.string.HOME_PARENT)),
						childAtPosition(
								allOf(withId(R.id.tool_bar_detail),
										withContentDescription(mContext.getString(R.string.titulo_toolbar)),
										childAtPosition(
												withId(R.id.app_bar_detail),
												0)),
								1),
						isDisplayed()));
		appCompatImageButton.perform(click());

		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void test2_click_share_button_and_close_with_same() {
		click_first_item();

		//abrir
		check_and_click_share();

		//cerrar
		check_and_click_share();
	}

	@Test
	public void test3_click_share_button_and_close_with_overlay_touch() {

		click_first_item();

		check_and_click_share();

		//clickear overlay
		ViewInteraction view = onView(
				allOf(withId(R.id.quake_details_container),
						childAtPosition(
								childAtPosition(
										withId(android.R.id.content),
										0),
								7),
						isDisplayed()));
		view.perform(click());

		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void click_first_item() {
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

	private void check_and_click_share() {

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
}
