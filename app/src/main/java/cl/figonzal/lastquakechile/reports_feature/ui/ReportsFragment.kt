package cl.figonzal.lastquakechile.reports_feature.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import androidx.annotation.DrawableRes
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cl.figonzal.lastquakechile.R
import cl.figonzal.lastquakechile.core.domain.DomainError
import cl.figonzal.lastquakechile.core.utils.views.configOptionsMenu
import cl.figonzal.lastquakechile.core.utils.views.showServerApiError
import cl.figonzal.lastquakechile.databinding.FragmentReportsBinding
import cl.figonzal.lastquakechile.reports_feature.domain.model.Report
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

private const val QUERY_PAGE_SIZE = 5

class ReportsFragment : Fragment() {

    private val reportAdapter: ReportAdapter by inject()
    private val viewModel: ReportViewModel by viewModel()

    private var _binding: FragmentReportsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportsBinding.inflate(inflater, container, false)

        bindingResources()
        handleReportState()

        configOptionsMenu(fragmentIndex = 3) {
            when (it.itemId) {
                R.id.refresh_menu -> {
                    viewModel.getFirstPageReports()
                    binding.recycleViewReports.scrollToPosition(0)
                }
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
                addOnScrollListener(this@ReportsFragment.scrollListener)
            }
        }

    }

    private fun handleReportState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                launch { collectUiState() }
                launch { collectErrors() }
            }
        }
        viewModel.getFirstPageReports()
    }

    private suspend fun collectUiState() {
        viewModel.uiState.collectLatest { state ->
            isLoading = state.isLoading
            isLastPage = state.isLastPage

            when {
                state.isLoading -> loadingUI()
                state.domainError != null -> {
                    with(binding) {
                        progressBarReports.visibility = View.GONE
                        val reports = state.reports
                        when {
                            reports.isEmpty() && state.domainError != DomainError.NoMoreData -> {
                                includeErrorMessage.root.visibility = View.VISIBLE
                                includeErrorMessage.btnRetry.setOnClickListener {
                                    viewModel.getFirstPageReports()
                                }
                                includeErrorMessage.btnRetry.visibility =
                                    if (state.domainError == DomainError.EmptyList) View.GONE else View.VISIBLE
                            }

                            reports.isEmpty() && state.domainError == DomainError.NoMoreData -> {
                                includeErrorMessage.root.visibility = View.GONE
                            }

                            else -> {
                                reportAdapter.reports = reports
                                includeErrorMessage.root.visibility = View.GONE
                            }
                        }
                    }
                }

                state.reports.isNotEmpty() -> showListUI(state.reports)
            }
            if (state.isLastPage) {
                binding.recycleViewReports.setPadding(0, 0, 0, 0)
            }
        }
    }

    private suspend fun collectErrors() {
        viewModel.errorState.collect { error ->
            Timber.d("COLLECT ERROR STATE: $error")
            showServerApiError(error) { iconId, message ->
                configErrorStatusMsg(iconId, message)
            }
        }
    }

    private fun configErrorStatusMsg(@DrawableRes icon: Int, errorMsg: String) {
        with(binding.includeErrorMessage) {

            ivWifiOff.setImageDrawable(
                ResourcesCompat.getDrawable(resources, icon, requireContext().theme)
            )
            tvMsgApiError.text = errorMsg
        }
    }

    private fun loadingUI() {
        with(binding) {
            progressBarReports.visibility = View.VISIBLE
            includeErrorMessage.root.visibility = View.GONE
        }
    }

    private fun showListUI(reports: List<Report>) {
        reportAdapter.reports = reports
        with(binding) {
            progressBarReports.visibility = View.GONE
            includeErrorMessage.root.visibility = View.GONE
            Timber.d("Showing report list in fragment")
        }
    }

    var isLoading = false
    var isLastPage = false
    var isScrolling = false

    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
            val visibleItemCount = layoutManager.childCount
            val totalItemCount = layoutManager.itemCount

            val isNotLoadingAndNotLastPage = !isLoading && !isLastPage
            val isAtLastItem = firstVisibleItemPosition + visibleItemCount >= totalItemCount
            val isNotAtBeginning = firstVisibleItemPosition >= 0
            val isTotalMoreThanVisible = totalItemCount >= QUERY_PAGE_SIZE
            val shouldPaginate =
                isNotLoadingAndNotLastPage && isAtLastItem && isNotAtBeginning &&
                        isTotalMoreThanVisible && isScrolling

            if (shouldPaginate) {
                viewModel.getNextPageReports()
                isScrolling = false
            } else {
                binding.recycleViewReports.setPadding(0, 0, 0, 0)
            }
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                isScrolling = true
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}