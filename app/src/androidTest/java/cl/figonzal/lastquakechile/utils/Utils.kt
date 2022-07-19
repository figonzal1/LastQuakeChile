package cl.figonzal.lastquakechile.utils

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers
import org.hamcrest.Description
import org.hamcrest.Matcher

fun checkRecyclerSubViews(
    recyclerViewId: Int,
    position: Int,
    itemMatcher: Matcher<View?>,
    subViewId: Int
) {
    Espresso.onView(ViewMatchers.withId(recyclerViewId)).perform(
        RecyclerViewActions.scrollToPosition<RecyclerView.ViewHolder>(
            position
        )
    ).check(ViewAssertions.matches(atPositionOnView(position, itemMatcher, subViewId)))
}


private fun atPositionOnView(
    position: Int, itemMatcher: Matcher<View?>, targetViewId: Int
): Matcher<View?> {
    return object : BoundedMatcher<View?, RecyclerView>(RecyclerView::class.java) {
        override fun describeTo(description: Description) {
            description.appendText("has view id $itemMatcher at position $position")
        }

        override fun matchesSafely(recyclerView: RecyclerView): Boolean {
            val viewHolder = recyclerView.findViewHolderForAdapterPosition(position)
            val targetView = viewHolder!!.itemView.findViewById<View>(targetViewId)
            return itemMatcher.matches(targetView)
        }
    }
}