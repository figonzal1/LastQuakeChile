package cl.figonzal.lastquakechile.repository;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import cl.figonzal.lastquakechile.viewmodel.SingleLiveEvent;

public interface NetworkRepository<T> {

    @NonNull
    MutableLiveData<List<T>> getData();

    @NonNull
    MutableLiveData<Boolean> isLoading();

    @NonNull
    SingleLiveEvent<String> getMsgErrorList();
}
