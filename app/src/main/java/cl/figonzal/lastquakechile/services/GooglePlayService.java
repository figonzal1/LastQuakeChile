package cl.figonzal.lastquakechile.services;

import android.app.Activity;
import android.app.Dialog;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import cl.figonzal.lastquakechile.R;
import timber.log.Timber;

public class GooglePlayService implements LifecycleObserver {

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private final GoogleApiAvailability googlePlay;
    private final Activity activity;

    public GooglePlayService(Activity activity, @NonNull Lifecycle lifecycle) {
        this.activity = activity;
        googlePlay = GoogleApiAvailability.getInstance();
        lifecycle.addObserver(this);
    }

    /**
     * Funcion que verifica si el dispositivo cuenta con GooglePlayServices actualizado cada vez que una actividad esta en modo "OnStart" (visible)
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void checkPlayServices() {

        int resultCode = googlePlay.isGooglePlayServicesAvailable(activity);

        //Si existe algun problema con google play
        if (resultCode != ConnectionResult.SUCCESS) {

            //Si el error puede ser resuelto por el usuario
            if (googlePlay.isUserResolvableError(resultCode)) {

                Timber.e(activity.getString(R.string.GOOGLE_PLAY_SOLICITUD));

                //Solicitar al usuario actualizar google play
                Dialog dialog = googlePlay.getErrorDialog(activity, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST);
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();

            } else {

                //El error no puede ser resuelto por el usuario y la app se cierra
                Timber.e(activity.getString(R.string.GOOGLE_PLAY_NOSOPORTADO));

                activity.finish();
            }
        }

        //La app puede ser utilizada, google play esta actualizado
        else {
            Timber.i(activity.getString(R.string.GOOGLE_PLAY_ACTUALIZADO));
        }
    }
}
