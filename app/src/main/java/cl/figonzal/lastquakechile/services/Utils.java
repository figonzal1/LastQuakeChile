package cl.figonzal.lastquakechile.services;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.webkit.WebView;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import cl.figonzal.lastquakechile.R;

public class Utils {


    /**
     * Funcion encargada se guardar en directorio de celular una imagen bitmap
     *
     * @param bitmap  Bitmap de la imagen
     * @param context Contexto necesario para usar recursos
     * @return Path de la imagen
     */
    @Deprecated
    public static Uri getLocalBitmapUri(Bitmap bitmap, Context context) throws IOException {

        File mFile = new File(context.getCacheDir(), "share_image_" + System.currentTimeMillis() + ".jpeg");
        FileOutputStream out = new FileOutputStream(mFile);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        out.close();

        return FileProvider.getUriForFile(context, "cl.figonzal.lastquakechile.fileprovider", mFile);
    }

    /**
     * Funcion encargada de corregir el bug de en modo noche procovado por adviews
     *
     * @param activity Actividad desde donde proviene el adview
     */
    private static void fixAdViewNightMode(Activity activity) {

        Log.d(activity.getString(R.string.tag_adview_night_mode), activity.getString(R.string.tag_adview_night_mode_response));

        try {
            new WebView(activity.getApplicationContext());
        } catch (Exception e) {
            Log.e(activity.getString(R.string.tag_adview_night_mode), activity.getString(R.string.tag_adview_night_mode_response_error), e);
        }
    }
}
