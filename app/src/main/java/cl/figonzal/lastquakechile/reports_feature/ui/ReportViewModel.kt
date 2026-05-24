package cl.figonzal.lastquakechile.reports_feature.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.figonzal.lastquakechile.core.domain.DomainError
import cl.figonzal.lastquakechile.core.domain.DomainResult
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

    private val _errorState = Channel<DomainError>()
    val errorState = _errorState.receiveAsFlow()

    fun getFirstPageReports() {
        viewModelScope.launch {
            currentPage = 1
            _uiState.update { it.copy(isLoading = true, domainError = null, isLastPage = false) }

            getReportsUseCase(0).collect { result ->
                Timber.d("FIRST PAGE STATE $result")

                when (result) {
                    is DomainResult.Error -> {
                        _uiState.update { state ->
                            state.copy(
                                isLoading = false,
                                domainError = result.error,
                                reports = result.data
                            )
                        }
                        _errorState.send(result.error)
                    }
                    is DomainResult.Success -> {
                        _uiState.update {
                            it.copy(
                                reports = result.data,
                                isLoading = false,
                                domainError = null
                            )
                        }
                    }
                }
            }
        }
    }

    fun getNextPageReports() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, domainError = null) }

            getReportsUseCase(currentPage).collect { result ->
                Timber.d("NEXT PAGE STATE $result")

                when (result) {
                    is DomainResult.Error -> {
                        if (result.error == DomainError.NoMoreData) {
                            _uiState.update { it.copy(isLoading = false, isLastPage = true) }
                        } else {
                            _uiState.update { it.copy(isLoading = false, domainError = result.error) }
                            _errorState.send(result.error)
                        }
                    }
                    is DomainResult.Success -> {
                        currentPage++
                        _uiState.update {
                            it.copy(
                                reports = it.reports + result.data,
                                isLoading = false,
                                domainError = null
                            )
                        }
                    }
                }
            }
        }
    }
}
