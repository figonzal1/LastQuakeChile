package cl.figonzal.lastquakechile.quake_feature.ui.map

import cl.figonzal.lastquakechile.quake_feature.domain.model.Quake
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

class QuakeClusterItem(val quake: Quake) : ClusterItem {
    override val position = LatLng(quake.coordinate.latitude, quake.coordinate.longitude)
    override val title = quake.city
    override val snippet: String? = null
    override val zIndex = 0f
}
