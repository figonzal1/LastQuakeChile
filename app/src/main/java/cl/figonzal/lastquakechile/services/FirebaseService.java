package cl.figonzal.lastquakechile.services;

import android.app.Activity;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import timber.log.Timber;

public class FirebaseService {

    private final Activity activity;
    private final FirebaseMessaging firebaseMessaging;

    public FirebaseService(Activity activity, FirebaseMessaging firebaseMessaging) {
        this.activity = activity;
        this.firebaseMessaging = firebaseMessaging;
    }

    /**
     * Funcion encargada de realizar la iniciacion de los servicios de FIREBASE
     */
    public void getFirebaseToken() {

        //FIREBASE SECTION
        firebaseMessaging.setAutoInitEnabled(true);
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(activity,
                instanceIdResult -> {

                    String token = instanceIdResult.getToken();
                    Timber.i("Token: %s", token);
                });
    }
}
