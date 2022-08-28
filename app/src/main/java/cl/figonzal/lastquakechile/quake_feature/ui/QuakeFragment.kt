package cl.figonzal.lastquakechile.quake_feature.ui

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
import cl.figonzal.lastquakechile.core.utils.SharedPrefUtil
import cl.figonzal.lastquakechile.core.utils.configOptionsMenu
import cl.figonzal.lastquakechile.core.utils.showServerApiError
import cl.figonzal.lastquakechile.databinding.FragmentQuakeBinding
import cl.figonzal.lastquakechile.quake_feature.domain.model.Quake
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import timber.log.Timber

private const val QUERY_PAGE_SIZE: Int = 20

class QuakeFragment(
    private val quakeAdapter: QuakeAdapter
) : Fragment() {

    private val viewModel: QuakeViewModel by sharedViewModel()

    private lateinit var sharedPrefUtil: SharedPrefUtil

    private var _binding: FragmentQuakeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentQuakeBinding.inflate(inflater, container, false)

        sharedPrefUtil = SharedPrefUtil(requireContext())

        bindingResources()
        handleQuakeState()
        handleErrors()

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
        }
    }

    private fun handleQuakeState() {

        viewLifecycleOwner.lifecycleScope.launch {

            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {

                launch {
                    viewModel.firstPage.collectLatest {

                        when {
                            it.isLoading -> loadingUI()
                            !it.isLoading && it.quakes.isNotEmpty() && it.apiError == null -> {
                                showListUI(it.quakes.toList())
                            }
                        }
                    }
                }

                launch {
                    viewModel.quakeState.collectLatest {

                        when {
                            it.isLoading -> loadingUI()
                            !it.isLoading && it.quakes.isNotEmpty() && it.apiError == null -> {

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
            }
        }
        viewModel.getFirstPageQuakes()
    }

    private fun handleErrors() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.errorState
                .flowWithLifecycle(lifecycle, Lifecycle.State.RESUMED)
                .collectLatest {

                    with(binding) {

                        progressBarQuakes.visibility = View.GONE
                        includeNoWifi.root.visibility = when {
                            quakeAdapter.quakes.isEmpty() -> View.VISIBLE
                            else -> View.GONE
                        }

                        includeNoWifi.btnRetry.setOnClickListener {
                            viewModel.getFirstPageQuakes()
                        }
                    }

                    showServerApiError(it) { iconId, message ->
                        configErrorStatusMsg(iconId, message)
                    }
                }
        }
    }

    private fun loadingUI() {
        with(binding) {
            isLoading = true
            progressBarQuakes.visibility = View.VISIBLE
            includeNoWifi.root.visibility = View.GONE
        }
    }

    private fun showListUI(quakes: List<Quake>) {

        with(binding) {
            View.GONE.apply {
                isLoading = false
                progressBarQuakes.visibility = this
                includeNoWifi.root.visibility = this
            }
        }

        //Load quakes
        quakeAdapter.quakes = quakes

        Timber.d(getString(R.string.FRAGMENT_LOAD_LIST))
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
                viewModel.getQuakes()
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

    private fun configErrorStatusMsg(
        @DrawableRes icon: Int,
        errorMsg: String
    ) {
        with(binding.includeNoWifi) {

            ivWifiOff.setImageDrawable(
                ResourcesCompat.getDrawable(resources, icon, requireContext().theme)
            )
            tvMsgNoWifi.text = errorMsg
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}