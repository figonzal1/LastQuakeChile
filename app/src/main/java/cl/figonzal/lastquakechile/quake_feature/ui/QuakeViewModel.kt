package cl.figonzal.lastquakechile.quake_feature.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.figonzal.lastquakechile.core.data.remote.ApiError
import cl.figonzal.lastquakechile.core.data.remote.StatusAPI
import cl.figonzal.lastquakechile.quake_feature.domain.model.Quake
import cl.figonzal.lastquakechile.quake_feature.domain.uses_cases.GetQuakesUseCase
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

    var actualIndexPage = 1
    private var oldList: MutableList<Quake>? = null

    private val _quakeState = MutableStateFlow(QuakeState())
    val quakeState = _quakeState.asStateFlow()

    private val _firstPage = MutableStateFlow(QuakeState())
    val firstPage = _firstPage.asStateFlow()

    private val _errorState = Channel<ApiError>()
    val errorState = _errorState.receiveAsFlow()


    fun getQuakes() {

        viewModelScope.launch {

            _quakeState.update { it.copy(isLoading = true) }

            getQuakesUseCase(actualIndexPage).collect { statusApi ->

                Timber.e("NEXT PAGE STATE ${statusApi.data}")

                val data = statusApi.data
                val apiError = statusApi.apiError

                when (statusApi) {

                    is StatusAPI.Error -> {

                        apiError?.let {
                            _quakeState.update { state ->
                                state.copy(
                                    isLoading = false,
                                    apiError = it,
                                    quakes = data as List<Quake> //cached list
                                )
                            }
                            _errorState.send(it)
                        }
                    }
                    is StatusAPI.Success -> {

                        actualIndexPage++

                        if (oldList == null) {
                            oldList = data as MutableList<Quake>
                        } else {
                            val oldData = oldList
                            val newData = data as List<Quake>
                            oldData?.addAll(newData)
                        }


                        _quakeState.update {
                            it.copy(
                                quakes = oldList ?: data,
                                isLoading = false,
                                apiError = null
                            )
                        }
                    }
                }
            }
        }
    }

    fun getFirstPageQuakes() {

        viewModelScope.launch {

            _firstPage.update { it.copy(isLoading = true) }

            getQuakesUseCase(0).collect { statusApi ->

                Timber.e("FIRST PAGE STATE ${statusApi.data}")

                val data = statusApi.data
                val apiError = statusApi.apiError

                when (statusApi) {

                    is StatusAPI.Error -> {

                        apiError?.let {
                            _firstPage.update { state ->
                                state.copy(
                                    isLoading = false,
                                    apiError = it,
                                    quakes = data as List<Quake> //cached list
                                )
                            }
                            _errorState.send(it)
                        }
                    }
                    is StatusAPI.Success -> {

                        oldList = data?.toMutableList()

                        _firstPage.update {
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