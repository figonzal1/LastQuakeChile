package cl.figonzal.lastquakechile.services;

import android.app.Activity;
import android.util.Log;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import cl.figonzal.lastquakechile.R;

public class FirebaseService {

    private Activity activity;
    private FirebaseCrashlytics crashlytics;
    private FirebaseMessaging firebaseMessaging;

    public FirebaseService(Activity activity, FirebaseMessaging firebaseMessaging) {
        this.activity = activity;
        this.firebaseMessaging = firebaseMessaging;
        crashlytics = FirebaseCrashlytics.getInstance();
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
                    Log.e(activity.getString(R.string.TAG_FIREBASE_TOKEN), token);

                    //CRASH ANALYTICS LOG
                    crashlytics.log(activity.getString(R.string.TAG_FIREBASE_TOKEN) + token);
                    crashlytics.setUserId(token);
                });
    }
}
