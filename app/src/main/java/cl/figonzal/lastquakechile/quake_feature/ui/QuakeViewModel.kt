package cl.figonzal.lastquakechile.quake_feature.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.figonzal.lastquakechile.core.data.remote.ApiError
import cl.figonzal.lastquakechile.core.data.remote.NewStatusAPI
import cl.figonzal.lastquakechile.quake_feature.domain.model.Quake
import cl.figonzal.lastquakechile.quake_feature.domain.uses_cases.GetQuakesUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class QuakeViewModel(
    private val getQuakesUseCase: GetQuakesUseCase
) : ViewModel() {

    private val _quakeState = MutableStateFlow(QuakeState())
    val quakeState = _quakeState.asStateFlow()

    private val _errorState = Channel<ApiError>()
    val errorState = _errorState.receiveAsFlow()

    fun getQuakes() {

        viewModelScope.launch {

            _quakeState.update { it.copy(isLoading = true) }

            getQuakesUseCase().collect { statusApi ->

                val data = statusApi.data
                val apiError = statusApi.apiError

                when (statusApi) {

                    is NewStatusAPI.Error -> {
                        _quakeState.update {
                            it.copy(
                                isLoading = false,
                                apiError = apiError,
                                quakes = data as List<Quake> //cached list
                            )
                        }

                        apiError?.let { _errorState.send(it) }
                    }
                    is NewStatusAPI.Success -> {

                        _quakeState.update {
                            it.copy(
                                quakes = data as List<Quake>,
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