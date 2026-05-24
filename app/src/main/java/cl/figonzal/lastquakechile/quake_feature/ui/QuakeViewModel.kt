package cl.figonzal.lastquakechile.quake_feature.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.figonzal.lastquakechile.core.data.remote.ApiError
import cl.figonzal.lastquakechile.core.data.remote.StatusAPI
import cl.figonzal.lastquakechile.quake_feature.domain.use_case.GetQuakesUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

class QuakeViewModel(
    private val getQuakesUseCase: GetQuakesUseCase
) : ViewModel() {

    private var currentPage = 1

    private val _uiState = MutableStateFlow(QuakeState())
    val uiState = _uiState.asStateFlow()

    private val _errorState = Channel<ApiError>()
    val errorState = _errorState.receiveAsFlow()

    fun getFirstPageQuakes() {
        viewModelScope.launch {
            currentPage = 1
            _uiState.update { it.copy(isLoading = true, apiError = null, isLastPage = false) }

            getQuakesUseCase(0).collect { statusApi ->
                Timber.d("FIRST PAGE STATE $statusApi")

                when (statusApi) {
                    is StatusAPI.Error -> {
                        val error = statusApi.apiError ?: return@collect
                        _uiState.update { state ->
                            state.copy(
                                isLoading = false,
                                apiError = error,
                                quakes = statusApi.data.orEmpty()
                            )
                        }
                        _errorState.send(error)
                    }
                    is StatusAPI.Success -> {
                        _uiState.update {
                            it.copy(
                                quakes = statusApi.data.orEmpty(),
                                isLoading = false,
                                apiError = null
                            )
                        }
                    }
                }
            }
        }
    }

    fun getNextPageQuakes() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, apiError = null) }

            getQuakesUseCase(currentPage).collect { statusApi ->
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
                        val newQuakes = statusApi.data.orEmpty()
                        _uiState.update {
                            it.copy(
                                quakes = it.quakes + newQuakes,
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
