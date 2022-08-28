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

    private val _nextPageState = MutableStateFlow(QuakeState())
    val nextPagesState = _nextPageState.asStateFlow()

    private val _firstPageState = MutableStateFlow(QuakeState())
    val firstPageState = _firstPageState.asStateFlow()

    private val _errorState = Channel<ApiError>()
    val errorState = _errorState.receiveAsFlow()

    fun getFirstPageQuakes() {

        viewModelScope.launch {

            _firstPageState.update { it.copy(isLoading = true) }

            getQuakesUseCase(0).collect { statusApi ->

                Timber.d("FIRST PAGE STATE $statusApi")

                val data = statusApi.data
                val apiError = statusApi.apiError

                when (statusApi) {

                    is StatusAPI.Error -> {

                        apiError?.let {
                            _firstPageState.update { state ->
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

                        _firstPageState.update {
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

    fun getQuakes() {

        viewModelScope.launch {

            _nextPageState.update { it.copy(isLoading = true) }

            getQuakesUseCase(actualIndexPage).collect { statusApi ->

                Timber.d("NEXT PAGE STATE $statusApi")

                val data = statusApi.data
                val apiError = statusApi.apiError

                when (statusApi) {

                    is StatusAPI.Error -> {

                        apiError?.let {
                            _nextPageState.update { state ->
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


                        _nextPageState.update {
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
}