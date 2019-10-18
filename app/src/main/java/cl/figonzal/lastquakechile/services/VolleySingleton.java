package cl.figonzal.lastquakechile.services;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class VolleySingleton {

    private static VolleySingleton sVolleySingleton;
    private static Context mContext;
    private RequestQueue mQueue;

    private VolleySingleton(Context context) {
        mContext = context.getApplicationContext();
        mQueue = getRequestQueue();
    }

    /**
     * Funcion que genera el singleton del Volley
     *
     * @param context Contexto necesario para uso de singleton
     * @return objecto volley singleton
     */
    public static synchronized VolleySingleton getInstance(Context context) {

        //Si la instancia no existe
        if (sVolleySingleton == null) {
            sVolleySingleton = new VolleySingleton(context);
        }
        return sVolleySingleton;
    }

    /**
     * Funcion que devuelve la cola de request's
     *
     * @return retorna la cola de solicitudes apiladas en volley
     */
    private RequestQueue getRequestQueue() {

        if (mQueue == null) {
            mQueue = Volley.newRequestQueue(mContext);
        }
        return mQueue;
    }

    /**
     * Funcion para agregar a la cola
     *
     * @param req //
     * @param <T> //
     */
    public <T> void addToRequestQueue(Request<T> req, Object tag) {
        req.setTag(tag);
        getRequestQueue().add(req);
    }

    public void cancelPendingRequests(Object tag) {
        if (mQueue != null) {
            mQueue.cancelAll(tag);
        }
    }
}
