package cl.figonzal.lastquakechile.quake_feature.ui

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import org.koin.android.ext.android.inject
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cl.figonzal.lastquakechile.R
import cl.figonzal.lastquakechile.core.domain.DomainError
import cl.figonzal.lastquakechile.core.services.notifications.utils.handleCvAlertPermission
import cl.figonzal.lastquakechile.core.services.notifications.utils.onNotificationPermissionResult
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

class QuakeFragment : Fragment() {

    private val quakeAdapter: QuakeAdapter by inject()
    private val viewModel: QuakeViewModel by activityViewModel()

    private var _binding: FragmentQuakeBinding? = null
    private val binding get() = _binding!!

    // Must be registered before onStart — fragment property initializer is the correct place.
    private val notificationPermissionLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            _binding?.let { handlePermissionResult(isGranted, it) }
        }

    private fun handlePermissionResult(isGranted: Boolean, binding: FragmentQuakeBinding) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val sharedPrefUtil = SharedPrefUtil(requireContext())
            onNotificationPermissionResult(isGranted, sharedPrefUtil)
            handleCvAlertPermission(binding, sharedPrefUtil, notificationPermissionLauncher)
        }
    }

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
                R.id.refresh_menu -> {
                    viewModel.getFirstPageQuakes()
                    binding.recycleViewQuakes.scrollToPosition(0)
                }
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
                handleCvAlertPermission(binding, SharedPrefUtil(requireContext()), notificationPermissionLauncher)
            }
        }
    }

    private fun handleQuakeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                launch { collectUiState() }
                launch { collectErrors() }
            }
        }
        viewModel.getFirstPageQuakes()
    }

    private suspend fun collectUiState() {
        viewModel.uiState.collectLatest { state ->
            isLoading = state.isLoading
            isLastPage = state.isLastPage

            when {
                state.isLoading -> loadingUI()
                state.domainError != null -> {
                    with(binding) {
                        progressBarQuakes.visibility = View.GONE
                        val quakes = state.quakes
                        when {
                            quakes.isEmpty() && state.domainError != DomainError.NoMoreData -> {
                                includeErrorMessage.root.visibility = View.VISIBLE
                                tvCacheCopy.visibility = View.GONE
                                includeErrorMessage.btnRetry.setOnClickListener {
                                    viewModel.getFirstPageQuakes()
                                }
                                includeErrorMessage.btnRetry.visibility =
                                    if (state.domainError == DomainError.EmptyList) View.GONE else View.VISIBLE
                            }
                            quakes.isEmpty() && state.domainError == DomainError.NoMoreData -> {
                                includeErrorMessage.root.visibility = View.GONE
                            }
                            else -> {
                                quakeAdapter.quakes = quakes
                                tvCacheCopy.visibility = View.VISIBLE
                                includeErrorMessage.root.visibility = View.GONE
                            }
                        }
                    }
                }
                state.quakes.isNotEmpty() -> showListUI(state.quakes)
            }
            if (state.isLastPage) {
                binding.recycleViewQuakes.setPadding(0, 0, 0, 0)
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

    private fun configErrorStatusMsg(@DrawableRes icon: Int, errorMsg: String) =
        with(binding.includeErrorMessage) {

            ivWifiOff.setImageDrawable(
                ResourcesCompat.getDrawable(resources, icon, requireContext().theme)
            )
            tvMsgApiError.text = errorMsg
        }

    private fun loadingUI() {
        with(binding) {
            progressBarQuakes.visibility = View.VISIBLE
            includeErrorMessage.root.visibility = View.GONE
        }
    }

    private fun showListUI(quakes: List<Quake>) {
        quakeAdapter.quakes = quakes
        with(binding) {
            progressBarQuakes.visibility = View.GONE
            includeErrorMessage.root.visibility = View.GONE
            tvCacheCopy.visibility = View.GONE
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

    override fun onResume() {
        super.onResume()
        // Re-evaluate the permission cardview whenever the fragment becomes visible.
        // Covers the case where the user returns from system Settings after granting/revoking
        // POST_NOTIFICATIONS — otherwise the card stays stuck on its previous state.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            _binding?.let {
                handleCvAlertPermission(it, SharedPrefUtil(requireContext()), notificationPermissionLauncher)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
