package cl.figonzal.lastquakechile.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import cl.figonzal.lastquakechile.model.ReportModel;
import cl.figonzal.lastquakechile.repository.NetworkRepository;

public class ReportsViewModel extends AndroidViewModel {

    private final NetworkRepository<ReportModel> reportRepository;
    private MutableLiveData<List<ReportModel>> mutableLiveReports;

    public ReportsViewModel(@NonNull Application application, NetworkRepository<ReportModel> repository) {
        super(application);
        this.reportRepository = repository;
    }

    public MutableLiveData<List<ReportModel>> showReports() {

        if (mutableLiveReports == null) {

            mutableLiveReports = new MutableLiveData<>();
            mutableLiveReports = reportRepository.getData();
        }

        return mutableLiveReports;
    }

    @NonNull
    public SingleLiveEvent<String> showMsgErrorList() {
        return reportRepository.getMsgErrorList();
    }

    @NonNull
    public LiveData<Boolean> isLoading() {
        return reportRepository.isLoading();
    }

    public void refreshMutableReportList() {
        reportRepository.getData();
    }
}
