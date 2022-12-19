package cl.figonzal.lastquakechile.core.utils

import androidx.fragment.app.FragmentFactory
import cl.figonzal.lastquakechile.quake_feature.ui.QuakeAdapter
import cl.figonzal.lastquakechile.quake_feature.ui.QuakeFragment
import cl.figonzal.lastquakechile.reports_feature.ui.ReportAdapter
import cl.figonzal.lastquakechile.reports_feature.ui.ReportsFragment

class TestFragmentFactory(
    private val quakeAdapter: QuakeAdapter,
    private val reportAdapter: ReportAdapter
) : FragmentFactory() {

    override fun instantiate(classLoader: ClassLoader, className: String) =
        when (className) {
            QuakeFragment::class.java.name -> QuakeFragment(quakeAdapter)
            ReportsFragment::class.java.name -> ReportsFragment(reportAdapter)
            else -> super.instantiate(classLoader, className)
        }

}