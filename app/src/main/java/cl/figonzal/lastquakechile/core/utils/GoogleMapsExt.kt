package cl.figonzal.lastquakechile.core.utils

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import cl.figonzal.lastquakechile.R
import cl.figonzal.lastquakechile.quake_feature.domain.model.Quake
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.ktx.addCircle
import com.google.maps.android.ktx.addMarker
import timber.log.Timber
import java.io.IOException
import java.util.*

fun GoogleMap.loadPins(quakeList: List<Quake>, context: Context) {

    for (quake in quakeList) {

        //LatLong of quake
        val epicentre = LatLng(quake.coordinates.latitude, quake.coordinates.longitude)

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
        setMapStyle(
            MapStyleOptions.loadRawResourceStyle(context, R.raw.map_night_mode)
        )
    }
}

/**
 * Calculate mean coordinates of quakeList
 */
fun calculateMeanCords(quakeList: List<Quake>): LatLng {
    var meanLat = 0.0
    var meanLong = 0.0

    quakeList.onEach {
        meanLat += it.coordinates.latitude
        meanLong += it.coordinates.longitude
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
            Timber.d(getString(R.string.GOOGLE_MAP_SNAPSHOT))
            val bitMapUri = it?.let { it1 -> this.getLocalBitmapUri(it1) }
            this.shareQuake(quake, bitMapUri)
        } catch (e: IOException) {
            Timber.e(e, getString(R.string.GOOGLE_MAP_ERROR_SNAPSHOT), e.message)
        }
    }

}

//TODO: Externalizar compartir sismo
private fun Context.shareQuake(quake: Quake, bitMapUri: Uri?) {
    Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(
            Intent.EXTRA_TEXT, String.format(
                Locale.US,
                """
                [Alerta sísmica] #sismo #chile
                
                Información sismológica
                Ciudad: %1${"$"}s
                Hora Local: %2${"$"}s
                Magnitud: %3$.1f %4${"$"}s
                Profundidad: %5$.1f Km
                Georeferencia: %6${"$"}s
                
                Descarga la app aquí -> %7${"$"}s
                """.trimIndent(),
                quake.city,
                quake.localDate,
                quake.magnitude,
                quake.scale,
                quake.depth,
                quake.reference,
                getString(R.string.DEEP_LINK)
            )
        )
        putExtra(Intent.EXTRA_STREAM, bitMapUri)
        type = "image/*"
        startActivity(Intent.createChooser(this, getString(R.string.intent_chooser)))
    }
}