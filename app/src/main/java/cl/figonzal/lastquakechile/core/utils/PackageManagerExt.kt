@file:Suppress("unused")

package cl.figonzal.lastquakechile.core.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import timber.log.Timber


fun Context.doInstallation(packageName: String) {

    try {

        //LOG
        Timber.d("Opening google play")

        //Intenta abrir google play
        startActivity(Intent(Intent.ACTION_VIEW).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            data = Uri.parse("market://details?id=$packageName")
        })

    } catch (anfe: ActivityNotFoundException) {

        //Si google play no esta abre webview
        Timber.w(anfe, "Opening browser")

        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
            )
        )
    }
}
