package cl.figonzal.lastquakechile.core.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import cl.figonzal.lastquakechile.core.ui.compose.AdScreen
import cl.figonzal.lastquakechile.core.ui.theme.LastQuakeChileTheme
import cl.figonzal.lastquakechile.core.utils.views.configOptionsMenu

class AdFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        configOptionsMenu {}

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                LastQuakeChileTheme {
                    AdScreen()
                }
            }
        }
    }

    companion object {
        fun newInstance() = AdFragment()
    }
}
