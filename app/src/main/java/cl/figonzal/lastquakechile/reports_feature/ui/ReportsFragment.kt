package cl.figonzal.lastquakechile.reports_feature.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import cl.figonzal.lastquakechile.R
import cl.figonzal.lastquakechile.core.data.remote.ApiError
import cl.figonzal.lastquakechile.core.utils.configOptionsMenu
import cl.figonzal.lastquakechile.core.utils.showSnackBar
import cl.figonzal.lastquakechile.databinding.FragmentReportsBinding
import cl.figonzal.lastquakechile.reports_feature.domain.model.Report
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class ReportsFragment(
    private val reportAdapter: ReportAdapter
) : Fragment() {

    private val viewModel: ReportViewModel by viewModel()

    private var _binding: FragmentReportsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportsBinding.inflate(inflater, container, false)

        bindingResources()
        handleReportState()

        configOptionsMenu()

        return binding.root
    }

    private fun bindingResources() {

        with(binding) {

            recycleViewReports.apply {
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(context)
                adapter = reportAdapter
            }
        }

    }

    private fun handleReportState() {

        viewLifecycleOwner.lifecycleScope.launch {


            viewModel.reportState
                .flowWithLifecycle(lifecycle, Lifecycle.State.CREATED)
                .collect {

                    when {
                        it.isLoading -> loadingUI()
                        !it.isLoading && it.apiError != null -> errorUI(it)
                        !it.isLoading && it.reports.isNotEmpty() && it.apiError == null -> {
                            showListUI(it.reports)
                        }
                    }
                }
        }
        viewModel.getReports()
    }

    private fun loadingUI() {
        with(binding) {
            progressBarReports.visibility = View.VISIBLE
            includeNoWifi.root.visibility = View.GONE
        }
    }

    private fun showListUI(reports: List<Report>) {

        with(binding) {
            View.GONE.apply {
                progressBarReports.visibility = this
                includeNoWifi.root.visibility = this
            }
        }

        //Load reports
        reportAdapter.reports = reports

        Timber.d(getString(R.string.FRAGMENT_LOAD_LIST))
    }

    private fun errorUI(state: ReportState) {

        if (state.apiError != null) {

            with(binding) {
                progressBarReports.visibility = View.GONE
                includeNoWifi.root.visibility = when {
                    state.reports.isEmpty() -> View.VISIBLE
                    else -> View.GONE
                }
            }

            viewLifecycleOwner.lifecycleScope.launch {

                viewModel.errorState
                    .flowWithLifecycle(lifecycle, Lifecycle.State.RESUMED)
                    .collect {
                        when (state.apiError) {
                            ApiError.HttpError -> {
                                showSnackBar(
                                    binding.root,
                                    getString(R.string.http_error)
                                ) { viewModel.getReports() }
                            }
                            ApiError.IoError -> {
                                showSnackBar(
                                    binding.root,
                                    getString(R.string.io_error),
                                    getString(R.string.refresh)
                                ) { viewModel.getReports() }
                            }
                        }
                    }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}