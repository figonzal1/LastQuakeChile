package cl.figonzal.lastquakechile.quake_feature.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.figonzal.lastquakechile.core.utils.StatusAPI
import cl.figonzal.lastquakechile.quake_feature.domain.model.Quake
import cl.figonzal.lastquakechile.quake_feature.domain.uses_cases.GetQuakesUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class QuakeViewModel(
    private val getQuakesUseCase: GetQuakesUseCase
) : ViewModel() {

    private val _quakeState = MutableStateFlow(QuakeState())
    val quakeState = _quakeState.asStateFlow()

    private val _errorStatus = Channel<String>()
    val errorStatus = _errorStatus.receiveAsFlow()

    fun getQuakes() {

        viewModelScope.launch {

            getQuakesUseCase().collect {

                when (it) {

                    is StatusAPI.Loading -> {
                        _quakeState.value = quakeState.value.copy(isLoading = true)
                    }

                    is StatusAPI.Success -> {
                        _quakeState.value = quakeState.value.copy(
                            quakes = it.data as List<Quake>,
                            isLoading = false
                        )
                    }

                    is StatusAPI.Error -> {
                        _errorStatus.send(it.message as String)

                        _quakeState.value = quakeState.value.copy(
                            isLoading = false,
                            quakes = emptyList()
                        )
                    }
                }
            }
        }
    }

}