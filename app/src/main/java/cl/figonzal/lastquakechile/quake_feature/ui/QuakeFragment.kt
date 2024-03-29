package cl.figonzal.lastquakechile.quake_feature.ui

import android.os.Build
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
import cl.figonzal.lastquakechile.core.services.notifications.utils.handleCvAlertPermission
import cl.figonzal.lastquakechile.core.utils.SharedPrefUtil
import cl.figonzal.lastquakechile.core.utils.views.configOptionsMenu
import cl.figonzal.lastquakechile.core.utils.views.showServerApiError
import cl.figonzal.lastquakechile.databinding.FragmentQuakeBinding
import cl.figonzal.lastquakechile.quake_feature.domain.model.Quake
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import timber.log.Timber

private const val QUERY_PAGE_SIZE: Int = 20

class QuakeFragment(
    private val quakeAdapter: QuakeAdapter
) : Fragment() {

    private val viewModel: QuakeViewModel by activityViewModel()

    private var _binding: FragmentQuakeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentQuakeBinding.inflate(inflater, container, false)

        bindingResources()
        handleQuakeState()

        configOptionsMenu(fragmentIndex = 1) {
            when (it.itemId) {
                R.id.refresh_menu -> viewModel.getFirstPageQuakes()
            }
        }

        return binding.root
    }

    private fun bindingResources() {

        with(binding) {

            recycleViewQuakes.apply {
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(context)
                adapter = quakeAdapter
                addOnScrollListener(this@QuakeFragment.scrollListener)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                handleCvAlertPermission(binding, SharedPrefUtil(requireContext()))
            }
        }
    }

    private fun handleQuakeState() {

        viewLifecycleOwner.lifecycleScope.launch {

            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {

                launch { processFirstPage() }

                launch { processNextPage() }
            }
        }
        viewModel.getFirstPageQuakes()
    }

    private suspend fun processFirstPage() {
        viewModel.firstPageState.collectLatest {

            when {
                it.isLoading -> loadingUI()

                //Check if apiError exists
                it.apiError != null -> handleErrors(it.quakes.toList())

                //If api error is null, show updated list from network
                it.quakes.isNotEmpty() -> {
                    showListUI(it.quakes.toList())
                }
            }
        }

    }

    private suspend fun processNextPage() {
        viewModel.nextPagesState.collectLatest {

            when {
                it.isLoading -> loadingUI()

                //Check if apiError exists
                it.apiError != null -> handleErrors(it.quakes.toList())

                //If api error is null, show updated list from network
                it.quakes.isNotEmpty() -> {

                    showListUI(it.quakes.toList())

                    val totalPages = it.quakes.size / QUERY_PAGE_SIZE + 2
                    isLastPage = viewModel.actualIndexPage == totalPages

                    if (isLastPage) {
                        binding.recycleViewQuakes.setPadding(0, 0, 0, 0)
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
    private fun handleErrors(quakes: List<Quake>) {

        viewLifecycleOwner.lifecycleScope.launch {

            viewModel.errorState
                .flowWithLifecycle(lifecycle, Lifecycle.State.RESUMED)
                .collectLatest {

                    Timber.d("COLLECT ERROR STATE: $it")

                    with(binding) {

                        progressBarQuakes.visibility = View.GONE

                        when {
                            quakes.isEmpty() && it != ApiError.NoMoreData -> {
                                includeErrorMessage.root.visibility = View.VISIBLE
                                tvCacheCopy.visibility = View.GONE

                                includeErrorMessage.btnRetry.setOnClickListener {
                                    viewModel.getFirstPageQuakes()
                                }

                                if (it == ApiError.EmptyList) {
                                    includeErrorMessage.btnRetry.visibility = View.GONE
                                }
                            }
                            quakes.isEmpty() && it == ApiError.NoMoreData -> {
                                includeErrorMessage.root.visibility = View.GONE
                            }
                            else -> {
                                quakeAdapter.quakes = quakes
                                tvCacheCopy.visibility = View.VISIBLE
                            }
                        }
                        showServerApiError(it) { iconId, message ->
                            configErrorStatusMsg(iconId, message)
                        }

                    }
                }
        }
    }

    private fun configErrorStatusMsg(@DrawableRes icon: Int, errorMsg: String) =
        with(binding.includeErrorMessage) {

            ivWifiOff.setImageDrawable(
                ResourcesCompat.getDrawable(resources, icon, requireContext().theme)
            )
            tvMsgApiError.text = errorMsg
        }

    private fun loadingUI() {
        with(binding) {
            isLoading = true
            progressBarQuakes.visibility = View.VISIBLE
            includeErrorMessage.root.visibility = View.GONE
        }
    }

    private fun showListUI(quakes: List<Quake>) {

        //Load quakes
        quakeAdapter.quakes = quakes

        with(binding) {
            View.GONE.apply {
                isLoading = false
                progressBarQuakes.visibility = this
                includeErrorMessage.root.visibility = this
                tvCacheCopy.visibility = this
            }
            Timber.d("Showing quake list in fragment")
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
                viewModel.getNextPageQuakes()
                isScrolling = false
            } else {
                binding.recycleViewQuakes.setPadding(0, 0, 0, 0)
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