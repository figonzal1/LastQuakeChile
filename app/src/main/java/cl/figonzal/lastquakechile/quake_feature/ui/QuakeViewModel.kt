package cl.figonzal.lastquakechile.quake_feature.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.figonzal.lastquakechile.core.domain.DomainError
import cl.figonzal.lastquakechile.core.domain.DomainResult
import cl.figonzal.lastquakechile.quake_feature.domain.use_case.GetQuakesUseCase
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

class QuakeViewModel(
    private val getQuakesUseCase: GetQuakesUseCase
) : ViewModel() {

    private var currentPage = 1

    private val _uiState = MutableStateFlow(QuakeState())
    val uiState = _uiState.asStateFlow()

    private val _errorState = MutableSharedFlow<DomainError>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val errorState = _errorState.asSharedFlow()

    fun getFirstPageQuakes() {
        viewModelScope.launch {
            currentPage = 1
            _uiState.update { it.copy(isLoading = true, domainError = null, isLastPage = false) }

            getQuakesUseCase(0).collect { result ->
                Timber.d("FIRST PAGE STATE $result")

                when (result) {
                    is DomainResult.Error -> {
                        _uiState.update { state ->
                            state.copy(
                                isLoading = false,
                                domainError = result.error,
                                quakes = result.data
                            )
                        }
                        _errorState.tryEmit(result.error)
                    }

                    is DomainResult.Success -> {
                        _uiState.update {
                            it.copy(
                                quakes = result.data,
                                isLoading = false,
                                domainError = null
                            )
                        }
                    }
                }
            }
        }
    }

    fun getNextPageQuakes() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, domainError = null) }

            getQuakesUseCase(currentPage).collect { result ->
                Timber.d("NEXT PAGE STATE $result")

                when (result) {
                    is DomainResult.Error -> {
                        if (result.error == DomainError.NoMoreData) {
                            _uiState.update { it.copy(isLoading = false, isLastPage = true) }
                        } else {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    domainError = result.error
                                )
                            }
                            _errorState.tryEmit(result.error)
                        }
                    }

                    is DomainResult.Success -> {
                        currentPage++
                        _uiState.update {
                            it.copy(
                                quakes = it.quakes + result.data,
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
