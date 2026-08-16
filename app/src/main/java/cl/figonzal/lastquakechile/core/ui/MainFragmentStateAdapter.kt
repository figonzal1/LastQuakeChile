package cl.figonzal.lastquakechile.core.ui

import android.content.Context
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import cl.figonzal.lastquakechile.R
import cl.figonzal.lastquakechile.quake_feature.ui.QuakeFragment
import cl.figonzal.lastquakechile.quake_feature.ui.map.MapsFragment
import cl.figonzal.lastquakechile.reports_feature.ui.ReportsFragment
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Koin component needed for use injects
 */
class MainFragmentStateAdapter(
    fa: FragmentActivity,
    context: Context
) : FragmentStateAdapter(fa), KoinComponent {

    private val quakeFragment: QuakeFragment by inject()
    private val mapsFragment: MapsFragment by inject()
    private val reportFragment: ReportsFragment by inject()
    private val adFragment: AdFragment = AdFragment.newInstance()

    val tabs = listOf(
        "", //Ad section
        context.getString(R.string.tab_list),
        context.getString(R.string.tab_map),
        context.getString(R.string.tab_reports)
    )

    override fun createFragment(position: Int) = when (position) {
        0 -> adFragment
        1 -> quakeFragment
        2 -> mapsFragment
        3 -> reportFragment
        else -> quakeFragment
    }

    override fun getItemCount() = tabs.size
}