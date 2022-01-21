package cl.figonzal.lastquakechile.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import cl.figonzal.lastquakechile.reports_feature.data.remote.ReportsRemoteDataSource
import cl.figonzal.lastquakechile.reports_feature.data.repository.ReportRepositoryImpl
import cl.figonzal.lastquakechile.reports_feature.domain.use_case.GetReportsUseCase
import cl.figonzal.lastquakechile.reports_feature.ui.NewReportsViewModel
import cl.figonzal.lastquakechile.repository.QuakeRepository

class ViewModelFactory(
    private val application: Application
) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {


        return if (modelClass == QuakeListViewModel::class.java) {
            QuakeListViewModel(
                application,
                QuakeRepository.getIntance(application.applicationContext)
            ) as T
        } else {
            val repo = ReportRepositoryImpl(
                ReportsRemoteDataSource()
            )
            val useCase = GetReportsUseCase(repo)
            NewReportsViewModel(useCase) as T
        }
    }
}