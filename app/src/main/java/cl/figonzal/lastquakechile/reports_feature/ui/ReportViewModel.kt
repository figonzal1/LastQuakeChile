package cl.figonzal.lastquakechile.reports_feature.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.figonzal.lastquakechile.core.data.remote.ApiError
import cl.figonzal.lastquakechile.core.data.remote.StatusAPI
import cl.figonzal.lastquakechile.reports_feature.domain.model.Report
import cl.figonzal.lastquakechile.reports_feature.domain.use_case.GetReportsUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ReportViewModel(
    private val getReportsUseCase: GetReportsUseCase
) : ViewModel() {

    private val _reportState = MutableStateFlow(ReportState())
    val reportState = _reportState.asStateFlow()

    private val _errorState = Channel<ApiError>()
    val errorState = _errorState.receiveAsFlow()

    fun getReports() {

        viewModelScope.launch {

            _reportState.update { it.copy(isLoading = true) }

            getReportsUseCase().collect { statusApi ->

                val data = statusApi.data
                val apiError = statusApi.apiError

                when (statusApi) {
                    is StatusAPI.Error -> {
                        _reportState.update {
                            it.copy(
                                isLoading = false,
                                apiError = apiError,
                                reports = data as List<Report>
                            )
                        }

                        apiError?.let { _errorState.send(it) }
                    }
                    is StatusAPI.Success -> {

                        _reportState.update {
                            it.copy(
                                reports = data as List<Report>,
                                isLoading = false,
                                apiError = null
                            )
                        }
                    }
                }
            }
        }
    }
}