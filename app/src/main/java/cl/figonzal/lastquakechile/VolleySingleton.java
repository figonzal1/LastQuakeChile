package cl.figonzal.lastquakechile;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

class VolleySingleton {

    private static VolleySingleton volleySingleton;
    private RequestQueue queue;
    private static Context mContext;

    private VolleySingleton(Context context) {
        mContext = context.getApplicationContext();
        queue = getRequestQueue();
    }

    /**
     * Funcion que genera el singleton del Volley
     *
     * @param context Contexto necesario para uso de singleton
     * @return objecto volley singleton
     */
    static synchronized VolleySingleton getInstance(Context context) {

        //Si la instancia no existe
        if (volleySingleton == null) {
            volleySingleton = new VolleySingleton(context);
        }
        return volleySingleton;
    }

    /**
     * Funcion que devuelve la cola de request's
     *
     * @return retorna la cola de solicitudes apiladas en volley
     */
    private RequestQueue getRequestQueue() {

        if (queue == null) {
            queue = Volley.newRequestQueue(mContext);
        }
        return queue;
    }

    /**
     * Funcion para agregar a la cola
     *
     * @param req //
     * @param <T> //
     */
    <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }
}
