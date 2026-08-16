package cl.figonzal.lastquakechile.core.utils

import android.content.Context
import android.content.res.Configuration
import android.view.View
import cl.figonzal.lastquakechile.R
import cl.figonzal.lastquakechile.core.utils.views.QUAKE_DETAILS_MAGNITUDE_FORMAT
import cl.figonzal.lastquakechile.core.utils.views.getMagnitudeColor
import cl.figonzal.lastquakechile.core.utils.views.timeToText
import cl.figonzal.lastquakechile.core.utils.views.toast
import cl.figonzal.lastquakechile.databinding.QuakeBottomSheetBinding
import cl.figonzal.lastquakechile.quake_feature.domain.model.Quake
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import java.util.Locale

/**
 * Night mode for google map
 */
fun GoogleMap.setNightMode(context: Context) {

    //NIGHT MODE MAP
    val nightModeFlags =
        context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK

    if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
        setMapStyle(MapStyleOptions.loadRawResourceStyle(context, R.raw.map_night_mode))
    }
}

/**
 * Calculate mean coordinates of quakeList
 */
fun calculateMeanCords(quakeList: List<Quake>): LatLng = LatLng(
    quakeList.map { it.coordinate.latitude }.average(),
    quakeList.map { it.coordinate.longitude }.average()
)

const val SHARED_PREF_MAP_TYPE = "map_type"
fun Context.configMapType(): Int {
    val sharedPrefUtil = SharedPrefUtil(this@configMapType)
    return sharedPrefUtil.getData(SHARED_PREF_MAP_TYPE, GoogleMap.MAP_TYPE_NORMAL)
}

fun Context.setBottomSheetQuakeData(
    quake: Quake,
    binding: QuakeBottomSheetBinding
) {

    with(binding.sheetContent) {
        tvCity.text = quake.city
        tvReference.text = quake.reference

        tvMagnitude.text = String.format(
            Locale.getDefault(),
            QUAKE_DETAILS_MAGNITUDE_FORMAT,
            quake.magnitude
        )
        ivMagColor.setColorFilter(
            resources.getColor(
                getMagnitudeColor(quake.magnitude, false), theme
            )
        )

        tvDate.timeToText(quake, true)

        //Verified status
        ivVerified.visibility = when {
            quake.isVerified -> View.VISIBLE
            else -> View.GONE
        }

        ivVerified.setOnClickListener {
            toast(R.string.quake_verified_toast)
        }

        root.setOnClickListener {
            openQuakeDetails(quake)
        }
    }

    with(binding) {

        //Handle details button
        btnOpenDetails.setOnClickListener { openQuakeDetails(quake) }

        //Handle share button
        btnShareQuake.setOnClickListener { openQuakeDetails(quake, true) }
    }
}