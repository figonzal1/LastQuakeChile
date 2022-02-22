@file:Suppress("unused")

package cl.figonzal.lastquakechile.core.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import cl.figonzal.lastquakechile.R
import timber.log.Timber


fun Context.doInstallation(packageName: String) {

    try {

        //LOG
        Timber.d(getString(R.string.INTENT_GOOGLEPLAY))

        //Intenta abrir google play
        startActivity(Intent(Intent.ACTION_VIEW).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            data = Uri.parse("market://details?id=$packageName")
        })

    } catch (anfe: ActivityNotFoundException) {

        //Si google play no esta abre webview
        Timber.w(anfe, getString(R.string.INTENT_BROWSER))

        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
            )
        )
    }
}
