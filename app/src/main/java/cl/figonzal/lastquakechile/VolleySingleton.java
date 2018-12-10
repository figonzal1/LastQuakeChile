package cl.figonzal.lastquakechile;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class VolleySingleton {

    private static VolleySingleton volleySingleton;
    private RequestQueue queue;
    private static Context mContext;

    private VolleySingleton(Context context){
        mContext = context;
        queue = getRequestQueue();
    }

    /*
        Funcion que genera el singleton del volley
     */
    static synchronized VolleySingleton getInstance(Context context){

        //Si la instancia no funciona
        if(volleySingleton == null){
            volleySingleton = new VolleySingleton(context);
        }
        return volleySingleton;
    }

    /*
        Funcion que devuelve la cola de request's
     */
    RequestQueue getRequestQueue(){

        if(queue == null){
            queue = Volley.newRequestQueue(mContext.getApplicationContext());
        }
        return queue;
    }

    /*
        Funcion para agregar a la cola
     */
    <T> void addToRequestQueue(Request<T> req){
        getRequestQueue().add(req);
    }

    /*
        Funcion para cancelar
     */
    void cancelRequestQueue(Object tag){
        if (queue !=null){
            queue.cancelAll(tag);
        }
    }
}
