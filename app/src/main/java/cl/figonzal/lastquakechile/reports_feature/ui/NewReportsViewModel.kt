package cl.figonzal.lastquakechile.reports_feature.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.figonzal.lastquakechile.reports_feature.domain.model.Report
import cl.figonzal.lastquakechile.reports_feature.domain.use_case.GetReportsUseCase
import cl.figonzal.lastquakechile.reports_feature.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NewReportsViewModel(private val getReportsUseCase: GetReportsUseCase) : ViewModel() {

    private val _reportState = MutableStateFlow(ReportState())
    val reportState = _reportState.asStateFlow()

    init {

        viewModelScope.launch {

            getReportsUseCase().collect {

                _reportState.value = reportState.value.copy(isLoading = true)

                when (it) {
                    is Resource.Success -> {

                        _reportState.value = reportState.value.copy(
                            isLoading = false,
                            reports = it.data as List<Report>
                        )
                    }
                    is Resource.Error -> {
                        //TODO: Emit to show snackbar with channel
                    }
                }
            }
        }
    }

}