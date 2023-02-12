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
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cl.figonzal.lastquakechile.R
import cl.figonzal.lastquakechile.core.data.remote.ApiError
import cl.figonzal.lastquakechile.core.utils.views.configOptionsMenu
import cl.figonzal.lastquakechile.core.utils.views.showServerApiError
import cl.figonzal.lastquakechile.databinding.FragmentReportsBinding
import cl.figonzal.lastquakechile.reports_feature.domain.model.Report
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

private const val QUERY_PAGE_SIZE = 5

class ReportsFragment(
    private val reportAdapter: ReportAdapter
) : Fragment() {

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
                R.id.refresh_menu -> viewModel.getFirstPageReports()
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

                launch { processFirstPage() }

                launch { processNextPage() }
            }
        }
        viewModel.getFirstPageReports()
    }

    private suspend fun processFirstPage() {
        viewModel.firstPageState.collectLatest {

            when {
                it.isLoading -> loadingUI()

                //Check if apiError exists
                it.apiError != null -> handleErrors(it.reports.toList())

                //If api error is null, show updated list from network
                it.reports.isNotEmpty() -> {
                    showListUI(it.reports.toList())
                }
            }
        }

    }

    private suspend fun processNextPage() {
        viewModel.nextPagesState.collectLatest {

            when {
                it.isLoading -> loadingUI()

                //Check if apiError exists
                it.apiError != null -> handleErrors(it.reports.toList())

                //If api error is null, show updated list from network
                it.reports.isNotEmpty() -> {

                    showListUI(it.reports.toList())

                    val totalPages = it.reports.size / QUERY_PAGE_SIZE + 2
                    isLastPage = viewModel.actualIndexPage == totalPages

                    if (isLastPage) {
                        binding.recycleViewReports.setPadding(0, 0, 0, 0)
                    }
                }
            }
        }
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
                                    viewModel.getFirstPageReports()
                                }
                            }
                            else -> {

                                if (it != ApiError.NoMoreData) {
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
            isLoading = true
            progressBarReports.visibility = View.VISIBLE
            includeErrorMessage.root.visibility = View.GONE
        }
    }

    private fun showListUI(reports: List<Report>) {

        //Load reports
        reportAdapter.reports = reports

        with(binding) {
            View.GONE.apply {
                isLoading = false
                progressBarReports.visibility = this
                includeErrorMessage.root.visibility = this
            }
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
                viewModel.getReports()
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