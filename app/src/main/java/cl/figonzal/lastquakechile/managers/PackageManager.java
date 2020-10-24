package cl.figonzal.lastquakechile.managers;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.NonNull;

import cl.figonzal.lastquakechile.R;
import timber.log.Timber;

public class PackageManager {

    public PackageManager() {
    }

    /**
     * Funcion que realiza la instalacion de un paquete dado
     *
     * @param packageName Nombre del paquete
     * @param context     Contexto que permite utilizar recursos de strings
     */
    public void doInstallation(String packageName, @NonNull Context context) {

        Intent mIntent;

        try {
            //Intenta abrir google play
            mIntent = new Intent(Intent.ACTION_VIEW);
            mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mIntent.setData(Uri.parse("market://details?id=" + packageName));

            //LOG
            Timber.i(context.getString(R.string.TAG_INTENT_GOOGLEPLAY));

            context.startActivity(mIntent);

        } catch (android.content.ActivityNotFoundException anfe) {

            //Si gogle play no esta abre webview
            Timber.w(anfe, context.getString(R.string.TAG_INTENT_NAVEGADOR));

            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google" + ".com/store/apps/details?id=" + packageName)));
        }
    }
}
