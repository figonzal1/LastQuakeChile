package cl.figonzal.lastquakechile.reports_feature.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.figonzal.lastquakechile.core.Resource
import cl.figonzal.lastquakechile.reports_feature.domain.model.Report
import cl.figonzal.lastquakechile.reports_feature.domain.use_case.GetReportsUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class ReportViewModel(private val getReportsUseCase: GetReportsUseCase) : ViewModel() {

    private val _reportState = MutableStateFlow(ReportState())
    val reportState = _reportState.asStateFlow()

    private val _errorStatus = Channel<String>()
    val errorStatus = _errorStatus.receiveAsFlow()

    fun getReports() {

        viewModelScope.launch {

            getReportsUseCase().collect {

                when (it) {
                    is Resource.Loading -> {
                        _reportState.value = reportState.value.copy(
                            isLoading = true
                        )
                    }

                    is Resource.Success -> {

                        _reportState.value = reportState.value.copy(
                            isLoading = false,
                            reports = it.data as List<Report>
                        )
                    }
                    is Resource.Error -> {
                        _errorStatus.send(it.message as String)

                        _reportState.value = reportState.value.copy(
                            isLoading = false,
                            reports = emptyList()
                        )
                    }
                }
            }
        }
    }

}