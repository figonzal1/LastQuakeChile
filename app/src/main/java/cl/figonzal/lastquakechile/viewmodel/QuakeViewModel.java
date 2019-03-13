package cl.figonzal.lastquakechile.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import cl.figonzal.lastquakechile.QuakeModel;
import cl.figonzal.lastquakechile.repository.QuakeRepository;


/**
 * Clase ideada para cargar UI bajo el cambio de orientacion de pantalla
 * independiente de la creacion de una nueva activity
 */
public class QuakeViewModel extends AndroidViewModel {

    private QuakeRepository repository;
    private MutableLiveData<List<QuakeModel>> quakeMutableList;
    private MutableLiveData<List<QuakeModel>> quakeMutableFilteredList = new MutableLiveData<>();

    //Contructor para usar context dentro de la clase ViewModel
    public QuakeViewModel(@NonNull Application application) {
        super(application);
    }

    /**
     * Funcion encargada de recibir los datos de repositorio y que la View pueda acceder a ellos
     * @return retorna un mutablelivedata de listado de sismos
     */
    public MutableLiveData<List<QuakeModel>> showQuakeList() {

        if (quakeMutableList == null) {
            quakeMutableList = new MutableLiveData<>();

            repository = QuakeRepository.getIntance(getApplication());
            quakeMutableList = repository.getMutableQuakeList();
        }
        return quakeMutableList;
    }
    /**
     * La funcion fuerza el refresh de los datos del mutable
     */
    public void refreshMutableQuakeList() {
        repository = QuakeRepository.getIntance(getApplication());
        quakeMutableList = repository.getMutableQuakeList();
    }

    /**
     * Funcion recibe el status de la peticion desde el repositorio y permite que la View pueda acceder el status
     * @return Retorna el MutableLiveData del mensaje estado
     */
    public MutableLiveData<String> showStatusData() {

        repository = QuakeRepository.getIntance(getApplication());
        return repository.getStatusData();
    }

    /**
     * Funcion encargada de enviar el listado filtrado post busqueda hacia la View
     *
     * @return MutableLiveData de los simos filtrados
     */
    public MutableLiveData<List<QuakeModel>> showFilteredQuakeList() {
        return quakeMutableFilteredList;
    }

    /**
     * Funcion que realiza la busqueda sobre quakeModelList con el Parametro otorgado
     * @param s Texto que ingresa el usuario en la busqueda
     */
    public void doSearch(String s) {

        repository = QuakeRepository.getIntance(getApplication());
        List<QuakeModel> quakeList = repository.getQuakeList();

        if (quakeList.size() > 0 && !s.isEmpty()) {
            //Lista utilizada para el searchView
            List<QuakeModel> filteredList = new ArrayList<>();

            for (QuakeModel l : quakeList) {

                //Filtrar por lugar de referencia
                if (l.getReferencia().toLowerCase().contains(s)) {
                    filteredList.add(l);
                }

                //Filtrar por magnitud de sismo
                if (l.getMagnitud().toString().contains(s)) {
                    filteredList.add(l);
                }
            }
            quakeMutableFilteredList.postValue(filteredList);
        }

    }

    @Override
    protected void onCleared() {
        super.onCleared();
    }
}
