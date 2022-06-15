package cl.figonzal.lastquakechile.core.utils

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import cl.figonzal.lastquakechile.quake_feature.ui.QuakeAdapter
import cl.figonzal.lastquakechile.quake_feature.ui.QuakeFragment

class TestFragmentFactory(
    private val quakeAdapter: QuakeAdapter
) : FragmentFactory() {

    override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
        return when (className) {
            QuakeFragment::class.java.name -> QuakeFragment(quakeAdapter)
            else -> super.instantiate(classLoader, className)
        }
    }

}