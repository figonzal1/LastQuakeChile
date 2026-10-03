package cl.figonzal.lastquakechile.core.ui

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import cl.figonzal.lastquakechile.R
import cl.figonzal.lastquakechile.quake_feature.ui.QuakeFragment
import cl.figonzal.lastquakechile.quake_feature.ui.map.MapsFragment
import cl.figonzal.lastquakechile.reports_feature.ui.ReportsFragment
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

/**
 * Koin component needed for use injects
 */
class MainFragmentStateAdapter(
    fa: FragmentActivity,
    context: Context
) : FragmentStateAdapter(fa), KoinComponent {

    val tabs = listOf(
        "", //Ad section
        context.getString(R.string.tab_list),
        context.getString(R.string.tab_map),
        context.getString(R.string.tab_reports)
    )

    // Must return a new instance on every call: ViewPager2 destroys off-screen fragments, and
    // re-adding a destroyed instance leaves its ActivityResultLaunchers unregistered.
    override fun createFragment(position: Int): Fragment = when (position) {
        0 -> AdFragment.newInstance()
        2 -> get<MapsFragment>()
        3 -> get<ReportsFragment>()
        else -> get<QuakeFragment>()
    }

    override fun getItemCount() = tabs.size
}
