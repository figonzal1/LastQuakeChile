package cl.figonzal.lastquakechile.quake_feature.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Application
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
import cl.figonzal.lastquakechile.core.utils.SharedPrefUtil
import cl.figonzal.lastquakechile.core.utils.configOptionsMenu
import cl.figonzal.lastquakechile.databinding.FragmentQuakeBinding
import cl.figonzal.lastquakechile.quake_feature.domain.model.Quake
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import timber.log.Timber

class QuakeFragment(
    private val quakeAdapter: QuakeAdapter
) : Fragment() {

    private val viewModel: QuakeViewModel by sharedViewModel()
    private var application: Application? = null

    private lateinit var sharedPrefUtil: SharedPrefUtil
    private lateinit var crashlytics: FirebaseCrashlytics

    private var _binding: FragmentQuakeBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        application = requireActivity().application
        crashlytics = FirebaseCrashlytics.getInstance()
        sharedPrefUtil = SharedPrefUtil(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentQuakeBinding.inflate(inflater, container, false)

        bindingResources()
        handleQuakeState()
        showCvInfo()
        configOptionsMenu()

        return binding.root
    }

    private fun bindingResources() {
        with(binding) {

            recycleViewQuakes.apply {
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(context)
                adapter = quakeAdapter
            }
        }
    }

    private fun handleQuakeState() {

        viewLifecycleOwner.lifecycleScope.launch {

            viewModel.quakeState
                .flowWithLifecycle(lifecycle, Lifecycle.State.RESUMED)
                .collect {

                    when {
                        it.isLoading -> loadingUI()
                        !it.isLoading && it.apiError != null -> errorUI(it)
                        !it.isLoading && it.quakes.isNotEmpty() && it.apiError == null -> {
                            showListUI(it.quakes)
                        }
                    }
                }
        }
        viewModel.getQuakes()
    }

    private fun loadingUI() {
        with(binding) {
            progressBarQuakes.visibility = View.VISIBLE
            includeNoWifi.root.visibility = View.GONE
        }
    }

    private fun showListUI(quakes: List<Quake>) {

        with(binding) {
            View.GONE.apply {
                progressBarQuakes.visibility = this
                includeNoWifi.root.visibility = this
            }
        }

        //Load quakes
        quakeAdapter.quakes = quakes

        Timber.d(getString(R.string.FRAGMENT_LOAD_LIST))
    }

    private fun errorUI(state: QuakeState) {

        if (state.apiError != null) {

            with(binding) {
                progressBarQuakes.visibility = View.GONE
                includeNoWifi.root.visibility = when {
                    state.quakes.isEmpty() -> View.VISIBLE
                    else -> View.GONE
                }
            }

            viewLifecycleOwner.lifecycleScope.launch {

                viewModel.errorState
                    .flowWithLifecycle(lifecycle, Lifecycle.State.RESUMED)
                    .collect {
                        when (state.apiError) {
                            ApiError.HttpError -> showSnackBar(getString(R.string.http_error))
                            ApiError.IoError -> showSnackBar(
                                getString(R.string.io_error),
                                getString(R.string.refresh)
                            )
                        }
                    }
            }
        }
    }

    private fun showCvInfo() {

        val isCvShowed = sharedPrefUtil.getData(
            getString(R.string.shared_pref_status_card_view_info),
            true
        ) as Boolean

        with(binding.include.cvInfo) {
            visibility = when {
                isCvShowed -> View.VISIBLE
                else -> View.GONE
            }

            binding.include.btnInfoAccept.setOnClickListener {

                sharedPrefUtil.saveData(
                    getString(R.string.shared_pref_status_card_view_info),
                    false
                )

                this.animate()
                    .translationY(-this.height.toFloat())
                    .alpha(1.0f)
                    .setDuration(500)
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            this@with.visibility = View.GONE
                        }
                    })

                //LOGS
                Timber.d(getString(R.string.CV_INFO) + ": Ok")
            }
        }

    }

    private fun showSnackBar(string: String, action: String? = null) {

        val snackbar = Snackbar.make(binding.root, string, Snackbar.LENGTH_INDEFINITE)

        if (action != null) {
            snackbar
                .setAction(action) { viewModel.getQuakes() }
                .setActionTextColor(
                    resources.getColor(R.color.colorSecondary, requireContext().theme)
                )
        }
        snackbar.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}