package cl.figonzal.lastquakechile.core

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import cl.figonzal.lastquakechile.quake_feature.data.local.QuakeLocalDataSource
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

@Suppress("UNCHECKED_CAST")
class ViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {


    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        when {
            modelClass.isAssignableFrom(QuakeViewModel::class.java) -> return QuakeViewModel(
                GetQuakesUseCase(
                    QuakeRepositoryImpl(
                        QuakeLocalDataSource(application),
                        QuakeRemoteDataSource(application),
                        Dispatchers.IO
                    )
                )
            ) as T
            modelClass.isAssignableFrom(ReportViewModel::class.java) -> return ReportViewModel(
                GetReportsUseCase(
                    ReportRepositoryImpl(
                        ReportLocalDataSource(application),
                        ReportRemoteDataSource(application),
                        Dispatchers.IO
                    )
                )
            ) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }


    }
}