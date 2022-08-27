package cl.figonzal.lastquakechile.reports_feature.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import cl.figonzal.lastquakechile.R
import cl.figonzal.lastquakechile.core.data.remote.ApiError
import cl.figonzal.lastquakechile.core.utils.configOptionsMenu
import cl.figonzal.lastquakechile.core.utils.toast
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

        configOptionsMenu() {}

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

                includeNoWifi.btnRetry.setOnClickListener {
                    viewModel.getReports()
                }
            }

            viewLifecycleOwner.lifecycleScope.launch {

                viewModel.errorState
                    .flowWithLifecycle(lifecycle, Lifecycle.State.RESUMED)
                    .collectLatest {

                        when (state.apiError) {

                            ApiError.HttpError -> {

                                toast(R.string.http_error)

                                configErrorStatusMsg(
                                    icon = R.drawable.ic_round_report_24,
                                    errorMsg = getString(R.string.http_error)
                                )
                            }
                            ApiError.UnknownError -> {

                                toast(R.string.http_error)

                                configErrorStatusMsg(
                                    icon = R.drawable.ic_round_report_24,
                                    errorMsg = getString(R.string.http_error)
                                )
                            }
                            ApiError.IoError -> {

                                toast(R.string.io_error)

                                configErrorStatusMsg(
                                    icon = R.drawable.ic_round_wifi_off_24,
                                    errorMsg = getString(R.string.io_error)
                                )
                            }
                            ApiError.ServerError -> {

                                toast(R.string.service_error)

                                configErrorStatusMsg(
                                    icon = R.drawable.ic_round_router_24,
                                    errorMsg = getString(R.string.service_error)
                                )
                            }
                            ApiError.TimeoutError -> {

                                toast(R.string.service_error)

                                configErrorStatusMsg(
                                    icon = R.drawable.ic_round_router_24,
                                    errorMsg = getString(R.string.service_error)
                                )
                            }
                            ApiError.ResourceNotFound -> {
                                Toast.makeText(requireContext(), "No hay mas", Toast.LENGTH_LONG)
                                    .show()
                            }
                            else -> {
                                toast(R.string.http_error)

                                configErrorStatusMsg(
                                    icon = R.drawable.ic_round_report_24,
                                    errorMsg = getString(R.string.http_error)
                                )
                            }
                        }
                    }
            }
        }
    }

    private fun configErrorStatusMsg(
        @DrawableRes icon: Int,
        errorMsg: String
    ) {
        with(binding.includeNoWifi) {

            ivWifiOff.setImageDrawable(
                resources.getDrawable(icon, requireContext().theme)
            )
            tvMsgNoWifi.text = errorMsg
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}