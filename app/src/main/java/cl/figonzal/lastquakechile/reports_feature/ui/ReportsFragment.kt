package cl.figonzal.lastquakechile.reports_feature.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import cl.figonzal.lastquakechile.R
import cl.figonzal.lastquakechile.core.utils.configOptionsMenu
import cl.figonzal.lastquakechile.core.utils.showServerApiError
import cl.figonzal.lastquakechile.databinding.FragmentReportsBinding
import cl.figonzal.lastquakechile.reports_feature.domain.model.Report
import kotlinx.coroutines.flow.collectLatest
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

        configOptionsMenu(fragmentIndex = 3) {
            when (it.itemId) {
                R.id.refresh_menu -> viewModel.getReports()
            }
        }

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
                .collectLatest {

                    when {
                        it.isLoading -> loadingUI()

                        //Check if apiError exists
                        !it.isLoading && it.apiError != null -> handleErrors(it.reports.toList())

                        //If api error is null, show updated list from network
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
            includeErrorMessage.root.visibility = View.GONE
        }
    }

    private fun showListUI(reports: List<Report>) {

        //Load reports
        reportAdapter.reports = reports

        with(binding) {
            View.GONE.apply {
                progressBarReports.visibility = this
                includeErrorMessage.root.visibility = this
            }
        }

        Timber.d(getString(R.string.FRAGMENT_LOAD_LIST))
    }

    /**
     * Handle api error and show cached results
     *
     * If the list is empty show includeErrorMessage
     * Otherwise show toast with error and cached list
     */
    private fun handleErrors(report: List<Report>) {

        reportAdapter.reports = report

        viewLifecycleOwner.lifecycleScope.launch {

            viewModel.errorState
                .flowWithLifecycle(lifecycle, Lifecycle.State.RESUMED)
                .collectLatest {

                    Timber.d("COLLECT ERROR STATE: $it")

                    with(binding) {

                        progressBarReports.visibility = View.GONE

                        when {
                            reportAdapter.reports.isEmpty() -> {
                                includeErrorMessage.root.visibility = View.VISIBLE

                                includeErrorMessage.btnRetry.setOnClickListener {
                                    viewModel.getReports()
                                }
                            }
                            else -> {
                                includeErrorMessage.root.visibility = View.GONE
                            }
                        }
                    }

                    showServerApiError(it) { iconId, message ->
                        configErrorStatusMsg(iconId, message)
                    }
                }
        }
    }

    private fun configErrorStatusMsg(
        @DrawableRes icon: Int,
        errorMsg: String
    ) {
        with(binding.includeErrorMessage) {

            ivWifiOff.setImageDrawable(
                ResourcesCompat.getDrawable(
                    resources,
                    icon,
                    requireContext().theme
                )
            )
            tvMsgNoWifi.text = errorMsg
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}