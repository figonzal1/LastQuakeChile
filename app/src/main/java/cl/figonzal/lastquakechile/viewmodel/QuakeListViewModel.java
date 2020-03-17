package cl.figonzal.lastquakechile.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;

import cl.figonzal.lastquakechile.model.QuakeModel;
import cl.figonzal.lastquakechile.repository.QuakeRepository;


/**
 * Clase ideada para cargar UI bajo el cambio de orientacion de pantalla
 * independiente de la creacion de una nueva activity
 */
public class QuakeListViewModel extends AndroidViewModel {

    private final MutableLiveData<List<QuakeModel>> mQuakeMutableFilterList = new MutableLiveData<>();
    private QuakeRepository mQuakeRepository;
    private MutableLiveData<List<QuakeModel>> mQuakeMutableList;

    //Contructor para usar context dentro de la clase ViewModel
    public QuakeListViewModel(@NonNull Application application) {
        super(application);
    }

    /**
     * Funcion encargada de recibir los datos de repositorio y que la View pueda acceder a ellos
     *
     * @return retorna un mutablelivedata de listado de sismos
     */
    public MutableLiveData<List<QuakeModel>> showQuakeList() {

        if (mQuakeMutableList == null) {
            mQuakeMutableList = new MutableLiveData<>();

            mQuakeRepository = QuakeRepository.getIntance(getApplication());
            mQuakeMutableList = mQuakeRepository.getQuakes();
        }
        return mQuakeMutableList;
    }

    public MutableLiveData<Boolean> isLoading() {
        mQuakeRepository = QuakeRepository.getIntance(getApplication());
        return mQuakeRepository.getIsLoadingQuakes();
    }

    /**
     * La funcion fuerza el refresh de los datos del mutable
     */
    public void refreshMutableQuakeList() {
        mQuakeRepository = QuakeRepository.getIntance(getApplication());
        mQuakeRepository.getQuakes();
    }

    /**
     * Funcion recibe el status de la peticion desde el repositorio y permite que la View pueda
     * acceder el status
     *
     * @return Retorna el MutableLiveData del mensaje estado
     */
    public MutableLiveData<String> showResponseErrorList() {

        mQuakeRepository = QuakeRepository.getIntance(getApplication());
        return mQuakeRepository.getResponseErrorList();
    }

    /**
     * Funcion encargada de enviar el listado filtrado post busqueda hacia la View
     *
     * @return MutableLiveData de los simos filtrados
     */
    public MutableLiveData<List<QuakeModel>> showFilteredQuakeList() {
        return mQuakeMutableFilterList;
    }

    /**
     * Funcion que realiza la busqueda sobre quakeModelList con el Parametro otorgado
     *
     * @param s Texto que ingresa el usuario en la busqueda
     */
    public void doSearch(String s) {

        mQuakeRepository = QuakeRepository.getIntance(getApplication());
        List<QuakeModel> mQuakeList = mQuakeRepository.getQuakeList();

        if (mQuakeList.size() > 0 && !s.isEmpty()) {
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
