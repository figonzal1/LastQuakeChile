package cl.figonzal.lastquakechile.quake_feature.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.figonzal.lastquakechile.R
import cl.figonzal.lastquakechile.core.ui.theme.LastQuakeChileTheme
import cl.figonzal.lastquakechile.core.utils.views.configOptionsMenu
import cl.figonzal.lastquakechile.quake_feature.ui.compose.QuakeScreen
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class QuakeFragment : Fragment() {

    private val viewModel: QuakeViewModel by activityViewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        configOptionsMenu(fragmentIndex = 1) { item ->
            when (item.itemId) {
                R.id.refresh_menu -> viewModel.getFirstPageQuakes()
            }
        }

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                LastQuakeChileTheme {
                    QuakeScreen(viewModel = viewModel)
                }
            }
        }
    }
}
