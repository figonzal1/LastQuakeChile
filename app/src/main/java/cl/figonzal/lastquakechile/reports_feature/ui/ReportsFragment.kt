package cl.figonzal.lastquakechile.reports_feature.ui

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
import cl.figonzal.lastquakechile.databinding.FragmentReportsBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class ReportsFragment(
    private val reportAdapter: ReportAdapter
) : Fragment() {

    private lateinit var crashlytics: FirebaseCrashlytics

    private val viewModel: ReportViewModel by viewModel()

    private var _binding: FragmentReportsBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        crashlytics = FirebaseCrashlytics.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportsBinding.inflate(inflater, container, false)

        bindingResources()
        initViewModel()

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

    private fun initViewModel() {

        viewLifecycleOwner.lifecycleScope.launch {

            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.CREATED) {

                //Error Status
                launch {
                    viewModel.errorStatus.collect {

                        binding.includeNoWifi.root.visibility = View.VISIBLE
                        binding.progressBarReportes.visibility = View.GONE
                        showSnackBar(it)
                    }
                }

                launch {
                    viewModel.reportState.collect {

                        when {
                            it.isLoading -> {
                                binding.progressBarReportes.visibility = View.VISIBLE
                            }
                            !it.isLoading && it.reports.isNotEmpty() -> {

                                binding.progressBarReportes.visibility = View.GONE

                                reportAdapter.reports = it.reports
                                Timber.d(getString(R.string.FRAGMENT_REPORTS) + ": " + getString(R.string.FRAGMENT_LOAD_LIST))
                            }
                        }
                    }
                }
            }
        }
        viewModel.getReports()
    }

    private fun showSnackBar(string: String) = Snackbar
        .make(binding.root, string, Snackbar.LENGTH_INDEFINITE)
        .setAction(getString(R.string.snackbar_retry)) {

            viewModel.getReports()

            crashlytics.setCustomKey(getString(R.string.snackbar_error), true)
        }
        .setActionTextColor(resources.getColor(R.color.colorSecondary, requireContext().theme))
        .show()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}