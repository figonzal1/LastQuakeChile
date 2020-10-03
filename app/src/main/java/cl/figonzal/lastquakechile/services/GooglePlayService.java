package cl.figonzal.lastquakechile.services;

import android.app.Activity;
import android.app.Dialog;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import cl.figonzal.lastquakechile.R;

public class GooglePlayService {

    public GooglePlayService() {
    }

    /**
     * Funcion que verifica si el dispositivo cuenta con GooglePlayServices actualizado
     */
    public void checkPlayServices(Activity activity) {

        FirebaseCrashlytics crashlytics = FirebaseCrashlytics.getInstance();

        int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
        GoogleApiAvailability mGoogleApiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = mGoogleApiAvailability.isGooglePlayServicesAvailable(activity);

        //Si existe algun problema con google play
        if (resultCode != ConnectionResult.SUCCESS) {

            //Si el error puede ser resuelto por el usuario
            if (mGoogleApiAvailability.isUserResolvableError(resultCode)) {

                Dialog dialog = mGoogleApiAvailability.getErrorDialog(activity, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST);
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();

            } else {

                //El error no puede ser resuelto por el usuario y la app se cierra
                Log.d(activity.getString(R.string.TAG_GOOGLE_PLAY), activity.getString(R.string.GOOGLE_PLAY_NOSOPORTADO));
                crashlytics.log(activity.getString(R.string.TAG_GOOGLE_PLAY) + activity.getString(R.string.GOOGLE_PLAY_NOSOPORTADO));

                activity.finish();
            }
        }

        //La app puede ser utilizada, google play esta actualizado
        else {

            Log.d(activity.getString(R.string.TAG_GOOGLE_PLAY), activity.getString(R.string.GOOGLE_PLAY_ACTUALIZADO));
            crashlytics.log(activity.getString(R.string.TAG_GOOGLE_PLAY) + activity.getString(R.string.GOOGLE_PLAY_ACTUALIZADO));
        }
    }
}
