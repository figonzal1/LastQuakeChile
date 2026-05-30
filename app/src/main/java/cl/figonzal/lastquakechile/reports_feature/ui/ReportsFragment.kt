package cl.figonzal.lastquakechile.reports_feature.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import cl.figonzal.lastquakechile.R
import cl.figonzal.lastquakechile.core.ui.theme.LastQuakeChileTheme
import cl.figonzal.lastquakechile.core.utils.views.configOptionsMenu
import cl.figonzal.lastquakechile.reports_feature.ui.compose.ReportsScreen
import org.koin.androidx.viewmodel.ext.android.viewModel

class ReportsFragment : Fragment() {

    private val viewModel: ReportViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        configOptionsMenu(fragmentIndex = 3) { item ->
            when (item.itemId) {
                R.id.refresh_menu -> viewModel.getFirstPageReports()
            }
        }

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                LastQuakeChileTheme {
                    ReportsScreen(viewModel = viewModel)
                }
            }
        }
    }
}
