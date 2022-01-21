package cl.figonzal.lastquakechile.reports_feature.ui

import android.view.View
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.figonzal.lastquakechile.reports_feature.domain.model.Report
import cl.figonzal.lastquakechile.reports_feature.domain.use_case.GetReportsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class NewReportsViewModel(private val reportUseCase: GetReportsUseCase) : ViewModel() {

    private val _spinner = MutableStateFlow(View.VISIBLE)
    val spinner: StateFlow<Int> get() = _spinner

    private val _reports = MutableStateFlow(listOf<Report>())
    val reports: StateFlow<List<Report>> = _reports

    init {
        viewModelScope.launch {

            reportUseCase().collect {
                _reports.value = it
                _spinner.value = View.INVISIBLE
            }
        }
    }

}