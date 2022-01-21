package cl.figonzal.lastquakechile.newcode.ui

import android.view.View
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.figonzal.lastquakechile.model.ReportModel
import cl.figonzal.lastquakechile.newcode.data.NewReportsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class NewReportsViewModel(private val repositoryNew: NewReportsRepository) : ViewModel() {

    private val _spinner = MutableStateFlow(View.VISIBLE)
    val spinner: StateFlow<Int> get() = _spinner

    private val _reports = MutableStateFlow(listOf<ReportModel>())
    val reports: StateFlow<List<ReportModel>> = _reports

    init {
        viewModelScope.launch {
            repositoryNew.getReports().collect {
                _reports.value = it
                _spinner.value = View.INVISIBLE
            }
        }
    }

}