package cl.figonzal.lastquakechile.repository;

import androidx.lifecycle.MutableLiveData;

import java.util.List;

import cl.figonzal.lastquakechile.viewmodel.SingleLiveEvent;

public interface NetworkRepository<T> {

    MutableLiveData<List<T>> getData();

    MutableLiveData<Boolean> isLoading();

    SingleLiveEvent<String> getMsgErrorList();
}
