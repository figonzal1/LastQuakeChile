package cl.figonzal.lastquakechile.core.ui.dialog

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import cl.figonzal.lastquakechile.R
import cl.figonzal.lastquakechile.core.utils.SharedPrefUtil
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.*

class MapTerrainDialogFragment(private val googleMap: GoogleMap) : DialogFragment() {

    private lateinit var sharedPrefUtil: SharedPrefUtil

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        sharedPrefUtil = SharedPrefUtil(requireContext())

        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setTitle(getString(R.string.map_type))
                .setItems(R.array.maps) { _, which ->
                    // The 'which' argument contains the index position
                    // of the selected item
                    googleMap.mapType = when (which) {
                        0 -> MAP_TYPE_NORMAL
                        1 -> MAP_TYPE_TERRAIN
                        2 -> MAP_TYPE_HYBRID
                        else -> MAP_TYPE_NONE
                    }
                    sharedPrefUtil.saveData(
                        getString(R.string.shared_pref_map_type),
                        googleMap.mapType
                    )
                }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}