package cl.figonzal.lastquakechile;

import android.content.Context;

public interface ResponseNetworkHandler {

    void getData();

    void showSnackBar(Context context, String tipo);
}
