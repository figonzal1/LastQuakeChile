package cl.figonzal.lastquakechile.core.ui

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import cl.figonzal.lastquakechile.R
import cl.figonzal.lastquakechile.quake_feature.ui.QuakeFragment
import cl.figonzal.lastquakechile.reports_feature.ui.ReportsFragment
import cl.figonzal.lastquakechile.views.fragments.MapsFragmentNew

class MainFragmentStateAdapter(fa: FragmentActivity, context: Context) : FragmentStateAdapter(fa) {

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> QuakeFragment.newInstance()
            1 -> ReportsFragment.newInstance()
            else -> MapsFragmentNew.newInstance()
        }
    }

    override fun getItemCount(): Int {
        return tabs.size
    }

    companion object {
        val tabs = arrayOfNulls<String>(3)
    }

    init {
        tabs[0] = context.getString(R.string.tab_list)
        tabs[1] = context.getString(R.string.tab_reports)
        tabs[2] = context.getString(R.string.tab_map)
    }
}