package cl.figonzal.lastquakechile.core.ui.dialog

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import android.os.Bundle as PlatformBundle
import androidx.fragment.app.DialogFragment
import cl.figonzal.lastquakechile.R
import cl.figonzal.lastquakechile.core.utils.SHARED_PREF_MAP_TYPE
import cl.figonzal.lastquakechile.core.utils.SharedPrefUtil
import com.google.android.gms.maps.GoogleMap

class MapTerrainDialogFragment : DialogFragment() {

    companion object {
        const val REQUEST_KEY = "map_terrain_request"
        const val RESULT_MAP_TYPE = "map_type"

        fun newInstance() = MapTerrainDialogFragment()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val sharedPrefUtil = SharedPrefUtil(requireContext())
        return AlertDialog.Builder(requireActivity())
            .setTitle(getString(R.string.map_type))
            .setItems(R.array.maps) { _, which ->
                val mapType = when (which) {
                    0 -> GoogleMap.MAP_TYPE_NORMAL
                    1 -> GoogleMap.MAP_TYPE_TERRAIN
                    2 -> GoogleMap.MAP_TYPE_HYBRID
                    else -> GoogleMap.MAP_TYPE_NONE
                }
                sharedPrefUtil.saveData(SHARED_PREF_MAP_TYPE, mapType)
                parentFragmentManager.setFragmentResult(REQUEST_KEY, PlatformBundle().apply { putInt(RESULT_MAP_TYPE, mapType) })
            }
            .create()
    }
}
