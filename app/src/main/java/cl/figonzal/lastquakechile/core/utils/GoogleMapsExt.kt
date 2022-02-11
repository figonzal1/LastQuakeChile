package cl.figonzal.lastquakechile.core.utils

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import cl.figonzal.lastquakechile.R
import cl.figonzal.lastquakechile.quake_feature.domain.model.Quake
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.MapStyleOptions
import timber.log.Timber
import java.io.IOException
import java.util.*

fun GoogleMap.setNightMode(context: Context) {

    //NIGHT MODE MAPA
    val nightModeFlags =
        context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK

    if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
        setMapStyle(
            MapStyleOptions.loadRawResourceStyle(
                context,
                R.raw.map_night_mode
            )
        )
    }
}

fun Circle.animate(body: Circle.() -> Unit): Circle {
    body()
    return this
}

fun Context.makeSnapshot(googleMap: GoogleMap, quake: Quake) {

    //Tomar screenshot del mapa para posterior funcion de compartir

    googleMap.snapshot {
        try {
            Timber.i("Snapshot google play")
            val bitMapUri = it?.let { it1 -> this.getLocalBitmapUri(it1) }
            this.shareQuake(quake, bitMapUri)
        } catch (e: IOException) {
            Timber.e(e, "Error screenshot map: %s", e.message)
        }
    }

}

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
        startActivity(Intent.createChooser(this, getString(R.string.INTENT_CHOOSER)))
    }
}