package cl.figonzal.lastquakechile.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import cl.figonzal.lastquakechile.model.ReportModel;
import cl.figonzal.lastquakechile.repository.QuakeRepository;

public class ReportsViewModel extends AndroidViewModel {

    private QuakeRepository repository;

    private MutableLiveData<List<ReportModel>> mutableLiveReports;

    public ReportsViewModel(@NonNull Application application) {
        super(application);
    }

    public MutableLiveData<List<ReportModel>> showReports() {
        if (mutableLiveReports == null) {
            mutableLiveReports = new MutableLiveData<>();
            repository = QuakeRepository.getIntance(getApplication());
            mutableLiveReports = repository.getReports();
        }
        return mutableLiveReports;
    }

    public MutableLiveData<String> showErrorList() {
        repository = QuakeRepository.getIntance(getApplication());
        return repository.getResponseErrorList();
    }

    public MutableLiveData<Boolean> isLoading() {
        repository = QuakeRepository.getIntance(getApplication());
        return repository.getIsLoadingReports();
    }
}
