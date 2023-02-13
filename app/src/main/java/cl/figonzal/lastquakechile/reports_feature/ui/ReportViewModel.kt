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
import timber.log.Timber

class ReportViewModel(
    private val getReportsUseCase: GetReportsUseCase
) : ViewModel() {

    var actualIndexPage = 1
    private var oldList: MutableList<Report>? = null

    private val _nextPageState = MutableStateFlow(ReportState())
    val nextPagesState = _nextPageState.asStateFlow()

    private val _firstPageState = MutableStateFlow(ReportState())
    val firstPageState = _firstPageState.asStateFlow()

    private val _errorState = Channel<ApiError>()
    val errorState = _errorState.receiveAsFlow()

    fun getFirstPageReports() {

        viewModelScope.launch {

            _firstPageState.update { it.copy(isLoading = true) }

            getReportsUseCase(0).collect { statusApi ->

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
                                    reports = data as List<Report> //cached list
                                )
                            }
                            _errorState.send(it)
                        }
                    }
                    is StatusAPI.Success -> {

                        oldList = data?.toMutableList()

                        _firstPageState.update {
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

    fun getNextPageReports() {

        viewModelScope.launch {

            _nextPageState.update { it.copy(isLoading = true) }

            getReportsUseCase(actualIndexPage).collect { statusApi ->

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
                                    reports = data as List<Report> //cached list
                                )
                            }
                            _errorState.send(it)
                        }
                    }
                    is StatusAPI.Success -> {

                        actualIndexPage++

                        if (oldList == null) {
                            oldList = data as MutableList<Report>
                        } else {
                            val oldData = oldList
                            val newData = data as List<Report>
                            oldData?.addAll(newData)
                        }

                        _nextPageState.update {
                            it.copy(
                                reports = oldList ?: data,
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