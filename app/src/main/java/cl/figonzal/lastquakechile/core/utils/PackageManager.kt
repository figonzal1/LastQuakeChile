package cl.figonzal.lastquakechile.core.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import cl.figonzal.lastquakechile.R
import timber.log.Timber

/**
 * Funcion que realiza la instalacion de un paquete dado
 *
 * @param packageName Nombre del paquete
 * @param context     Contexto que permite utilizar recursos de strings
 */
fun doInstallation(packageName: String, context: Context) {

    try {

        //LOG
        Timber.i(context.getString(R.string.TAG_INTENT_GOOGLEPLAY))

        //Intenta abrir google play
        context.startActivity(Intent(Intent.ACTION_VIEW).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            data = Uri.parse("market://details?id=$packageName")
        })

    } catch (anfe: ActivityNotFoundException) {

        //Si google play no esta abre webview
        Timber.w(anfe, context.getString(R.string.TAG_INTENT_NAVEGADOR))

        context.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
            )
        )
    }
}
