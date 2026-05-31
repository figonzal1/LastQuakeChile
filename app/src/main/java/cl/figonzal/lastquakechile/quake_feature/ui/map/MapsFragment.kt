package cl.figonzal.lastquakechile.quake_feature.ui.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.lifecycle.compose.LocalLifecycleOwner
import cl.figonzal.lastquakechile.R
import cl.figonzal.lastquakechile.core.ui.dialog.MapTerrainDialogFragment
import cl.figonzal.lastquakechile.core.ui.theme.LastQuakeChileTheme
import cl.figonzal.lastquakechile.core.utils.configMapType
import cl.figonzal.lastquakechile.core.utils.views.configOptionsMenu
import cl.figonzal.lastquakechile.quake_feature.ui.QuakeViewModel
import org.koin.androidx.viewmodel.ext.android.activityViewModel

/**
 * Thin host for [MapsScreen]. The fragment only survives because the main shell still uses
 * `ViewPager2` + `MainFragmentStateAdapter`; all map rendering now lives in Compose.
 *
 * The "layers" toolbar action keeps using the original [MapTerrainDialogFragment]; its result
 * is forwarded to [MapsScreen] through the [mapType] state.
 */
class MapsFragment : Fragment() {

    private val viewModel: QuakeViewModel by activityViewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        configOptionsMenu(fragmentIndex = 2) { item ->
            when (item.itemId) {
                R.id.layers_menu -> MapTerrainDialogFragment.newInstance()
                    .show(parentFragmentManager, "map_terrain")
            }
        }

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                LastQuakeChileTheme {
                    val context = LocalContext.current
                    val lifecycleOwner = LocalLifecycleOwner.current
                    var mapType by rememberSaveable { mutableIntStateOf(context.configMapType()) }

                    DisposableEffect(lifecycleOwner) {
                        parentFragmentManager.setFragmentResultListener(
                            MapTerrainDialogFragment.REQUEST_KEY,
                            lifecycleOwner
                        ) { _, bundle ->
                            mapType = bundle.getInt(MapTerrainDialogFragment.RESULT_MAP_TYPE)
                        }
                        onDispose {
                            parentFragmentManager.clearFragmentResultListener(
                                MapTerrainDialogFragment.REQUEST_KEY
                            )
                        }
                    }

                    MapsScreen(mapType = mapType, viewModel = viewModel)
                }
            }
        }
    }
}
