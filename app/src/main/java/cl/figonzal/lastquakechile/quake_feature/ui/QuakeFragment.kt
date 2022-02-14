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
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import cl.figonzal.lastquakechile.R
import cl.figonzal.lastquakechile.core.ViewModelFactory
import cl.figonzal.lastquakechile.core.utils.SharedPrefUtil
import cl.figonzal.lastquakechile.databinding.FragmentQuakeBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.launch
import timber.log.Timber

class QuakeFragment : Fragment() {

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

    private fun initViewModel() {

        val viewModel: QuakeViewModel = ViewModelProvider(
            requireActivity(),
            ViewModelFactory(
                requireActivity().application
            )
        )[QuakeViewModel::class.java]

        lifecycleScope.launch {

            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                //Error Status
                launch {
                    viewModel.errorStatus.collect {
                        showSnackBar()
                    }
                }

                launch {
                    viewModel.quakeState.collect {

                        binding.progressBarQuakes.visibility = when {
                            it.isLoading -> View.VISIBLE
                            else -> View.GONE
                        }


                        if (it.quakes.isNotEmpty()) {
                            quakeAdapter.updateList(it.quakes)
                            binding.recycleViewQuakes.smoothScrollToPosition(0)
                        }

                        Timber.i(getString(R.string.TAG_FRAGMENT_QUAKE) + ": " + getString(R.string.FRAGMENT_LOAD_LIST))
                    }
                }
            }
        }

    }

    private fun bindingResources() {
        with(binding) {

            recycleViewQuakes.apply {
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(context)

                quakeAdapter = QuakeAdapter(
                    ArrayList(),
                    requireActivity()
                )
                this.adapter = quakeAdapter
            }
        }
    }

    private fun showCvInfo() {

        val isCvShowed = sharedPrefUtil.getData(
            getString(R.string.SHARED_PREF_STATUS_CARD_VIEW_INFO),
            true
        ) as Boolean

        with(binding.include.cvInfo) {
            visibility = when {
                isCvShowed -> View.VISIBLE
                else -> View.GONE
            }

            binding.include.btnInfoAccept.setOnClickListener {

                sharedPrefUtil.saveData(
                    getString(R.string.SHARED_PREF_STATUS_CARD_VIEW_INFO),
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
                Timber.i(getString(R.string.TAG_CARD_VIEW_INFO) + ": " + getString(R.string.SHARED_PREF_STATUS_CARD_VIEW_INFO_RESULT))
            }
        }

    }

    private fun showSnackBar() {
        Snackbar
            .make(binding.root, "Reset conexion", Snackbar.LENGTH_INDEFINITE)
            .setAction(getString(R.string.FLAG_RETRY)) {
                crashlytics.setCustomKey(
                    getString(R.string.SNACKBAR_NOCONNECTION_ERROR_PRESSED),
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