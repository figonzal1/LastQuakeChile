package cl.figonzal.lastquakechile.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import cl.figonzal.lastquakechile.managers.DateManager;
import cl.figonzal.lastquakechile.model.QuakeModel;
import cl.figonzal.lastquakechile.model.ReportModel;
import cl.figonzal.lastquakechile.repository.NetworkRepository;

public class ViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private final Application application;
    private final NetworkRepository<?> repository;

    private DateManager dateManager;

    public ViewModelFactory(Application application, NetworkRepository<?> repository) {
        this.application = application;
        this.repository = repository;
    }

    public ViewModelFactory(Application application, NetworkRepository<?> repository, DateManager dateManager) {
        this.application = application;
        this.repository = repository;
        this.dateManager = dateManager;
    }

    @SuppressWarnings("unchecked")
    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {

        if (modelClass == QuakeListViewModel.class) {
            return (T) new QuakeListViewModel(application, (NetworkRepository<QuakeModel>) repository, dateManager);
        } else {
            return (T) new ReportsViewModel(application, (NetworkRepository<ReportModel>) repository);
        }
    }
}
