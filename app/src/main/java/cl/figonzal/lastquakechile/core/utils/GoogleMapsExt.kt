package cl.figonzal.lastquakechile.core.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager.MATCH_DEFAULT_ONLY
import android.content.pm.PackageManager.ResolveInfoFlags
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.view.View
import cl.figonzal.lastquakechile.R
import cl.figonzal.lastquakechile.core.utils.views.QUAKE_DETAILS_MAGNITUDE_FORMAT
import cl.figonzal.lastquakechile.core.utils.views.getLocalBitmapUri
import cl.figonzal.lastquakechile.core.utils.views.getMagnitudeColor
import cl.figonzal.lastquakechile.core.utils.views.timeToText
import cl.figonzal.lastquakechile.core.utils.views.toast
import cl.figonzal.lastquakechile.databinding.QuakeBottomSheetBinding
import cl.figonzal.lastquakechile.quake_feature.domain.model.Quake
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.ktx.addCircle
import com.google.maps.android.ktx.addMarker
import timber.log.Timber
import java.io.IOException


fun GoogleMap.loadPins(quakeList: List<Quake>, context: Context) {

    for (quake in quakeList) {

        //LatLong of quake
        val epicentre = LatLng(quake.coordinate.latitude, quake.coordinate.longitude)

        //Search magnitude color
        val quakeColor = getMagnitudeColor(quake.magnitude, true)

        apply {

            addMarker {
                position(epicentre)
                alpha(0.9f)
            }?.tag = quake

            addCircle {
                center(epicentre)
                radius(10000 * quake.magnitude)
                fillColor(context.getColor(quakeColor))
                strokeColor(context.getColor(R.color.grey_dark_alpha))
            }
        }
    }
}

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
fun calculateMeanCords(quakeList: List<Quake>): LatLng {
    var meanLat = 0.0
    var meanLong = 0.0

    quakeList.onEach {
        meanLat += it.coordinate.latitude
        meanLong += it.coordinate.longitude
    }

    meanLat /= quakeList.size.toDouble()
    meanLong /= quakeList.size.toDouble()

    return LatLng(meanLat, meanLong)
}

/**
 * Animate circle
 */
fun Circle.animate(body: Circle.() -> Unit): Circle {
    body()
    return this
}

/**
 * Google maps take snapshot
 */
fun Context.makeSnapshot(googleMap: GoogleMap, quake: Quake) {

    googleMap.snapshot {
        try {
            Timber.d("Snapshot google map")

            val bitMapUri = it?.let { it1 -> this.getLocalBitmapUri(it1) }
            shareQuake(quake, bitMapUri)
        } catch (e: IOException) {
            Timber.e(e, "Error screenshot map: %s", e.message)
        }
    }

}

private fun Context.shareQuake(quake: Quake, bitMapUri: Uri?) {

    val shareText = String.format(
        """
        [${getString(R.string.SHARE_TITLE)}]
        
        ${getString(R.string.SHARE_SUB_TITLE)}
        ${getString(R.string.SHARE_CITY)}: %1${"$"}s
        ${getString(R.string.SHARE_LOCAL_HOUR)}: %2${"$"}s
        ${getString(R.string.SHARE_MAGNITUDE)}: %3$.1f %4${"$"}s
        ${getString(R.string.SHARE_DEPTH)}: %5$.1f Km
        ${getString(R.string.SHARE_GEO_REF)}: %6${"$"}s
        
        ${getString(R.string.SHARE_DOWNLOAD_MSG)} %7${"$"}s
        
    """.trimIndent(),
        quake.city,
        quake.localDate,
        quake.magnitude,
        quake.scale,
        quake.depth,
        quake.reference,
        getString(R.string.DEEP_LINK)
    )

    Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, shareText)
        putExtra(Intent.EXTRA_STREAM, bitMapUri)
        type = "image/*"

        val chooser = Intent.createChooser(this, getString(R.string.intent_chooser))

        val resInfoList = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> packageManager.queryIntentActivities(
                chooser,
                ResolveInfoFlags.of(MATCH_DEFAULT_ONLY.toLong())
            )

            else -> packageManager.queryIntentActivities(chooser, MATCH_DEFAULT_ONLY)
        }

        for (resolveInfo in resInfoList) {
            val packageName = resolveInfo.activityInfo.packageName
            grantUriPermission(
                packageName,
                bitMapUri,
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }
        startActivity(chooser)
    }
}

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