package cl.figonzal.lastquakechile.managers;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import cl.figonzal.lastquakechile.R;

public class PackageManager {

    public PackageManager() {
    }

    /**
     * Funcion que realiza la instalacion de un paquete dado
     *
     * @param packageName Nombre del paquete
     * @param context     Contexto que permite utilizar recursos de strings
     */
    public void doInstallation(String packageName, Context context) {

        FirebaseCrashlytics crashlytics = FirebaseCrashlytics.getInstance();

        Intent mIntent;

        try {
            //Intenta abrir google play
            mIntent = new Intent(Intent.ACTION_VIEW);
            mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mIntent.setData(Uri.parse("market://details?id=" + packageName));

            //LOG
            Log.d(context.getString(R.string.TAG_INTENT), context.getString(R.string.TAG_INTENT_GOOGLEPLAY));
            crashlytics.log(context.getString(R.string.TAG_INTENT) + context.getString(R.string.TAG_INTENT_GOOGLEPLAY));

            context.startActivity(mIntent);

        } catch (android.content.ActivityNotFoundException anfe) {

            //Si gogle play no esta abre webview
            Log.d(context.getString(R.string.TAG_INTENT), context.getString(R.string.TAG_INTENT_NAVEGADOR));
            crashlytics.log(context.getString(R.string.TAG_INTENT) + context.getString(R.string.TAG_INTENT_NAVEGADOR));

            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google" + ".com/store/apps/details?id=" + packageName)));
        }
    }
}
