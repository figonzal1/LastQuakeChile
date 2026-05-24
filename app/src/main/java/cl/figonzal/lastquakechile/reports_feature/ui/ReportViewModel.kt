package cl.figonzal.lastquakechile.reports_feature.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.figonzal.lastquakechile.core.data.remote.ApiError
import cl.figonzal.lastquakechile.core.data.remote.StatusAPI
import cl.figonzal.lastquakechile.reports_feature.domain.use_case.GetReportsUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

class ReportViewModel(
    private val getReportsUseCase: GetReportsUseCase
) : ViewModel() {

    private var currentPage = 1

    private val _uiState = MutableStateFlow(ReportState())
    val uiState = _uiState.asStateFlow()

    private val _errorState = Channel<ApiError>()
    val errorState = _errorState.receiveAsFlow()

    fun getFirstPageReports() {
        viewModelScope.launch {
            currentPage = 1
            _uiState.update { it.copy(isLoading = true, apiError = null, isLastPage = false) }

            getReportsUseCase(0).collect { statusApi ->
                Timber.d("FIRST PAGE STATE $statusApi")

                when (statusApi) {
                    is StatusAPI.Error -> {
                        val error = statusApi.apiError ?: return@collect
                        _uiState.update { state ->
                            state.copy(
                                isLoading = false,
                                apiError = error,
                                reports = statusApi.data.orEmpty()
                            )
                        }
                        _errorState.send(error)
                    }
                    is StatusAPI.Success -> {
                        _uiState.update {
                            it.copy(
                                reports = statusApi.data.orEmpty(),
                                isLoading = false,
                                apiError = null
                            )
                        }
                    }
                }
            }
        }
    }

    fun getNextPageReports() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, apiError = null) }

            getReportsUseCase(currentPage).collect { statusApi ->
                Timber.d("NEXT PAGE STATE $statusApi")

                when (statusApi) {
                    is StatusAPI.Error -> {
                        val error = statusApi.apiError ?: return@collect
                        if (error == ApiError.NoMoreData) {
                            _uiState.update { it.copy(isLoading = false, isLastPage = true) }
                        } else {
                            _uiState.update { it.copy(isLoading = false, apiError = error) }
                            _errorState.send(error)
                        }
                    }
                    is StatusAPI.Success -> {
                        currentPage++
                        val newReports = statusApi.data.orEmpty()
                        _uiState.update {
                            it.copy(
                                reports = it.reports + newReports,
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
