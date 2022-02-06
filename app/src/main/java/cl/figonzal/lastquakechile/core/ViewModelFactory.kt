package cl.figonzal.lastquakechile.core

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import cl.figonzal.lastquakechile.reports_feature.data.local.ReportLocalDataSource
import cl.figonzal.lastquakechile.reports_feature.data.remote.ReportRemoteDataSource
import cl.figonzal.lastquakechile.reports_feature.data.repository.ReportRepositoryImpl
import cl.figonzal.lastquakechile.reports_feature.domain.use_case.GetReportsUseCase
import cl.figonzal.lastquakechile.reports_feature.ui.ReportViewModel
import cl.figonzal.lastquakechile.repository.QuakeRepository
import cl.figonzal.lastquakechile.viewmodel.QuakeListViewModel
import kotlinx.coroutines.Dispatchers

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
                ReportLocalDataSource(application),
                ReportRemoteDataSource(application),
                Dispatchers.IO
            )
            val useCase = GetReportsUseCase(repo)
            ReportViewModel(useCase) as T
        }
    }
}