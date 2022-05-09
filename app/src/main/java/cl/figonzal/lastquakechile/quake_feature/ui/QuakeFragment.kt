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
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import cl.figonzal.lastquakechile.R
import cl.figonzal.lastquakechile.core.utils.SharedPrefUtil
import cl.figonzal.lastquakechile.databinding.FragmentQuakeBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import timber.log.Timber

class QuakeFragment : Fragment() {

    private val viewModel: QuakeViewModel by sharedViewModel()
    private var application: Application? = null

    private lateinit var sharedPrefUtil: SharedPrefUtil
    private lateinit var crashlytics: FirebaseCrashlytics
    private lateinit var quakeAdapter: QuakeAdapter

    private lateinit var binding: FragmentQuakeBinding

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
        setHasOptionsMenu(true)

        // Inflate the layout for thi{s fragment
        binding = FragmentQuakeBinding.inflate(inflater, container, false)

        bindingResources()
        initViewModel()

        showCvInfo()

        return binding.root
    }

    private fun bindingResources() {
        with(binding) {

            recycleViewQuakes.apply {
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(context)

                quakeAdapter = QuakeAdapter(ArrayList(), requireActivity())
                this.adapter = quakeAdapter
            }
        }
    }

    private fun initViewModel() {

        viewLifecycleOwner.lifecycleScope.launch {

            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                //Error Status
                launch {
                    viewModel.errorStatus.collect {

                        binding.includeNoWifi.root.visibility = View.VISIBLE
                        binding.progressBarQuakes.visibility = View.GONE
                        showSnackBar(it)
                    }
                }

                launch {
                    viewModel.quakeState.collect {

                        when {
                            it.isLoading -> {
                                binding.progressBarQuakes.visibility = View.VISIBLE
                            }
                            !it.isLoading && it.quakes.isNotEmpty() -> {

                                binding.progressBarQuakes.visibility = View.GONE

                                quakeAdapter.updateList(it.quakes)
                                Timber.d(getString(R.string.FRAGMENT_QUAKE) + ": " + getString(R.string.FRAGMENT_LOAD_LIST))
                            }
                        }
                    }
                }
            }
        }
        viewModel.getQuakes()
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

    private fun showSnackBar(string: String) {
        Snackbar
            .make(binding.root, string, Snackbar.LENGTH_INDEFINITE)
            .setAction(getString(R.string.snackbar_retry)) {

                viewModel.getQuakes()

                crashlytics.setCustomKey(
                    getString(R.string.snackbar_error),
                    true
                )
            }
            .setActionTextColor(resources.getColor(R.color.colorSecondary, requireContext().theme))
            .show()
    }

    companion object {
        @JvmStatic
        fun newInstance(): QuakeFragment {
            return QuakeFragment()
        }
    }
}