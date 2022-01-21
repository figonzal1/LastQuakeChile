package cl.figonzal.lastquakechile.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import cl.figonzal.lastquakechile.newcode.data.NewReportsRepository
import cl.figonzal.lastquakechile.newcode.data.remote.ReportsRemoteDataSource
import cl.figonzal.lastquakechile.newcode.ui.NewReportsViewModel
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
            val repo = NewReportsRepository(
                ReportsRemoteDataSource()
            )
            NewReportsViewModel(repo) as T
        }
    }
}