package cl.figonzal.lastquakechile.services;

import com.google.firebase.messaging.FirebaseMessaging;

import timber.log.Timber;

public class FirebaseService {

    private final FirebaseMessaging firebaseMessaging;

    public FirebaseService(FirebaseMessaging firebaseMessaging) {
        this.firebaseMessaging = firebaseMessaging;
    }

    /**
     * Funcion encargada de realizar la iniciacion de los servicios de FIREBASE
     */
    public void getFirebaseToken() {

        //FIREBASE SECTION

        firebaseMessaging.getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Timber.w("Fetching FCM registration token failed");
                        return;
                    }

                    // Get new FCM registration token
                    String token = task.getResult();
                    Timber.i("Token %s", token);
                });
    }
}
