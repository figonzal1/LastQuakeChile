package cl.figonzal.lastquakechile.quake_feature.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.figonzal.lastquakechile.core.Resource
import cl.figonzal.lastquakechile.quake_feature.domain.model.Quake
import cl.figonzal.lastquakechile.quake_feature.domain.uses_cases.GetQuakesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class QuakeViewModel(
    private val getQuakesUseCase: GetQuakesUseCase
) : ViewModel() {

    private val _quakeState = MutableStateFlow(QuakeState())
    val quakeState = _quakeState.asStateFlow()

    init {

        viewModelScope.launch {

            getQuakesUseCase().collect {

                when (it) {

                    is Resource.Loading -> {
                        _quakeState.value = quakeState.value.copy(
                            isLoading = true
                        )
                    }

                    is Resource.Success -> {
                        _quakeState.value = quakeState.value.copy(
                            quakes = it.data as List<Quake>,
                            isLoading = false
                        )
                    }

                    //TODO_: error resource
                }
            }
        }
    }

}