package cl.figonzal.lastquakechile.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import cl.figonzal.lastquakechile.model.ReportModel;
import cl.figonzal.lastquakechile.repository.ReportRepository;
import cl.figonzal.lastquakechile.services.SingleLiveEvent;

public class ReportsViewModel extends AndroidViewModel {

    private ReportRepository repository;

    private MutableLiveData<List<ReportModel>> mutableLiveReports;

    public ReportsViewModel(@NonNull Application application) {
        super(application);
    }

    public MutableLiveData<List<ReportModel>> showReports() {
        if (mutableLiveReports == null) {
            mutableLiveReports = new MutableLiveData<>();
            repository = ReportRepository.getIntance(getApplication());
            mutableLiveReports = repository.getReports();
        }
        return mutableLiveReports;
    }

    public SingleLiveEvent<String> showMsgErrorList() {
        repository = ReportRepository.getIntance(getApplication());
        return repository.getResponseMsgErrorList();
    }

    public MutableLiveData<Boolean> isLoading() {
        repository = ReportRepository.getIntance(getApplication());
        return repository.getIsLoadingReports();
    }

    public void refreshMutableReportList() {
        repository = ReportRepository.getIntance(getApplication());
        repository.getReports();
    }
}
