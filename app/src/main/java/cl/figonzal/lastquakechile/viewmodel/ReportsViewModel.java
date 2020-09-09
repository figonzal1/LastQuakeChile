package cl.figonzal.lastquakechile.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import cl.figonzal.lastquakechile.model.ReportModel;
import cl.figonzal.lastquakechile.repository.NetworkRepository;
import cl.figonzal.lastquakechile.repository.ReportRepository;
import cl.figonzal.lastquakechile.services.SingleLiveEvent;

public class ReportsViewModel extends AndroidViewModel {


    private NetworkRepository<ReportModel> reportRepository;

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

    public SingleLiveEvent<String> showMsgErrorList() {

        reportRepository = ReportRepository.getIntance(getApplication());

        return reportRepository.getMsgErrorList();
    }

    public MutableLiveData<Boolean> isLoading() {

        reportRepository = ReportRepository.getIntance(getApplication());

        return reportRepository.isLoading();
    }

    public void refreshMutableReportList() {

        reportRepository = ReportRepository.getIntance(getApplication());
        reportRepository.getData();
    }
}
