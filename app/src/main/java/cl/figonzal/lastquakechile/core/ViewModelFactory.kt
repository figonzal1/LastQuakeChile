package cl.figonzal.lastquakechile.core

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import cl.figonzal.lastquakechile.quake_feature.data.remote.QuakeRemoteDataSource
import cl.figonzal.lastquakechile.quake_feature.data.repository.QuakeRepositoryImpl
import cl.figonzal.lastquakechile.quake_feature.domain.uses_cases.GetQuakesUseCase
import cl.figonzal.lastquakechile.quake_feature.ui.QuakeViewModel
import cl.figonzal.lastquakechile.reports_feature.data.local.ReportLocalDataSource
import cl.figonzal.lastquakechile.reports_feature.data.remote.ReportRemoteDataSource
import cl.figonzal.lastquakechile.reports_feature.data.repository.ReportRepositoryImpl
import cl.figonzal.lastquakechile.reports_feature.domain.use_case.GetReportsUseCase
import cl.figonzal.lastquakechile.reports_feature.ui.ReportViewModel
import kotlinx.coroutines.Dispatchers

class ViewModelFactory(
    private val application: Application
) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {


        return if (modelClass == QuakeViewModel::class.java) {

            val repo = QuakeRepositoryImpl(QuakeRemoteDataSource(application), Dispatchers.IO)
            val useCase = GetQuakesUseCase(repo)
            QuakeViewModel(useCase) as T
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