package cl.figonzal.lastquakechile.views;


import androidx.test.espresso.ViewInteraction;
import androidx.test.rule.ActivityTestRule;

import org.junit.Rule;
import org.junit.Test;

import cl.figonzal.lastquakechile.R;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

public class ContactActivityTest {

	@Rule
	public ActivityTestRule<ContactActivity> testRule =
			new ActivityTestRule<>(ContactActivity.class);

	@Test
	public void check_email_displayed () {

		//Email desplegado
		ViewInteraction tvEmail = onView(allOf(withId(R.id.tv_email_contact),
				withText(R.string.contact_email)));

		//Checkear si fue desplegado
		tvEmail.check(matches(isDisplayed()));
	}

	@Test
	public void check_name_displayed () {

		ViewInteraction tvName = onView(allOf(withId(R.id.tv_name_contact),
				withText(R.string.contact_nombre)));

		tvName.check(matches(isDisplayed()));
	}

	@Test
	public void check_icon_name () {

		ViewInteraction ivName = onView(withId(R.id.iv_user_contact));
		ivName.check(matches(isDisplayed()));
	}

	@Test
	public void check_icon_email () {
		ViewInteraction ivEmail = onView(withId(R.id.iv_email_contact));

		//Si esta desplegedo
		ivEmail.check(matches(isDisplayed()));
	}
}