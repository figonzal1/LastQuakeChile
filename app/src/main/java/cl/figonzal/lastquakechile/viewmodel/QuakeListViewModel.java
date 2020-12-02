package cl.figonzal.lastquakechile.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;

import cl.figonzal.lastquakechile.model.QuakeModel;
import cl.figonzal.lastquakechile.repository.NetworkRepository;


/**
 * Clase ideada para cargar UI bajo el cambio de orientacion de pantalla
 * independiente de la creacion de una nueva activity
 */
public class QuakeListViewModel extends AndroidViewModel {

    private final NetworkRepository<QuakeModel> quakeRepository;
    private MutableLiveData<List<QuakeModel>> mQuakeMutableFilterList;
    private MutableLiveData<List<QuakeModel>> mQuakeMutableList;

    //Contructor para usar context dentro de la clase ViewModel
    public QuakeListViewModel(@NonNull Application application, NetworkRepository<QuakeModel> repository) {
        super(application);

        quakeRepository = repository;
    }

    /**
     * Funcion encargada de recibir los datos de repositorio y que la View pueda acceder a ellos
     *
     * @return retorna un LieData de listado de sismos
     */
    public LiveData<List<QuakeModel>> showQuakeList() {

        if (mQuakeMutableList == null) {
            mQuakeMutableList = quakeRepository.getData();
        }

        return mQuakeMutableList;
    }

    @NonNull
    public LiveData<Boolean> isLoading() {
        return quakeRepository.isLoading();
    }

    /**
     * La funcion fuerza el refresh de los datos del mutable
     */
    public void refreshMutableQuakeList() {
        quakeRepository.getData();
    }

    /**
     * Funcion recibe el status de la peticion desde el repositorio y permite que la View pueda
     * acceder el status
     *
     * @return Retorna el MutableLiveData del mensaje estado
     */
    @NonNull
    public SingleLiveEvent<String> showMsgErrorList() {
        return quakeRepository.getMsgErrorList();
    }

    /**
     * Funcion encargada de enviar el listado filtrado post busqueda hacia la View
     *
     * @return MutableLiveData de los simos filtrados
     */
    @NonNull
    public LiveData<List<QuakeModel>> showFilteredQuakeList() {

        if (mQuakeMutableFilterList == null) {

            mQuakeMutableFilterList = new MutableLiveData<>();
        }
        return mQuakeMutableFilterList;
    }

    /**
     * Funcion que realiza la busqueda sobre quakeModelList con el Parametro otorgado
     *
     * @param s Texto que ingresa el usuario en la busqueda
     */
    public void doSearch(@NonNull String s) {

        List<QuakeModel> mQuakeList = mQuakeMutableList.getValue();

        if (mQuakeList != null && mQuakeList.size() > 0 && !s.isEmpty()) {

            //Lista utilizada para el searchView
            List<QuakeModel> filteredList = new ArrayList<>();

            for (QuakeModel l : mQuakeList) {

                //Filtrar por ciudad
                if (l.getCiudad().toLowerCase().contains(s)) {
                    filteredList.add(l);
                }

                //Filtrar por magnitud de sismo
                if (l.getMagnitud().toString().contains(s)) {
                    filteredList.add(l);
                }
            }
            mQuakeMutableFilterList.postValue(filteredList);
        }
    }
}
