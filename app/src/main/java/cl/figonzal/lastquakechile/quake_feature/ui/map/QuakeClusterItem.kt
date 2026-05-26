package cl.figonzal.lastquakechile.quake_feature.ui.map

import cl.figonzal.lastquakechile.quake_feature.domain.model.Quake
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

class QuakeClusterItem(val quake: Quake) : ClusterItem {
    private val position = LatLng(quake.coordinate.latitude, quake.coordinate.longitude)
    override fun getPosition() = position
    override fun getTitle() = quake.city
    override fun getSnippet() = null
    override fun getZIndex() = 0f
}
