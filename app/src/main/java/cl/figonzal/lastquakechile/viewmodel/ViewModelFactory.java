package cl.figonzal.lastquakechile.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import cl.figonzal.lastquakechile.model.QuakeModel;
import cl.figonzal.lastquakechile.model.ReportModel;
import cl.figonzal.lastquakechile.repository.NetworkRepository;

public class ViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private Application application;
    private NetworkRepository<?> repository;

    public ViewModelFactory(Application application, NetworkRepository<?> repository) {
        this.application = application;
        this.repository = repository;
    }

    @SuppressWarnings("unchecked")
    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {

        if (modelClass == QuakeListViewModel.class) {
            return (T) new QuakeListViewModel(application, (NetworkRepository<QuakeModel>) repository);
        } else {
            return (T) new ReportsViewModel(application, (NetworkRepository<ReportModel>) repository);
        }
    }
}
