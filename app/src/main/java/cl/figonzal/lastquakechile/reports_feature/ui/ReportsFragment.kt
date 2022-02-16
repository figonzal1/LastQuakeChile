package cl.figonzal.lastquakechile.reports_feature.ui

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
import cl.figonzal.lastquakechile.databinding.FragmentReportsBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.launch
import timber.log.Timber

class ReportsFragment : Fragment() {

    private lateinit var crashlytics: FirebaseCrashlytics
    private lateinit var viewModel: ReportViewModel
    private var reportAdapter: ReportAdapter? = null

    private lateinit var binding: FragmentReportsBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        crashlytics = FirebaseCrashlytics.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentReportsBinding.inflate(inflater, container, false)

        bindingResources()
        initViewModel()

        return binding.root
    }

    private fun bindingResources() {

        with(binding) {

            recycleViewReports.apply {
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(context)

                reportAdapter = ReportAdapter(ArrayList(), requireContext())
                this.adapter = reportAdapter
            }
        }

    }

    private fun initViewModel() {

        viewModel = ViewModelProvider(
            requireActivity(),
            ViewModelFactory(
                requireActivity().application
            )
        )[ReportViewModel::class.java]


        lifecycleScope.launch {

            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                viewModel.getReports()

                //Error Status
                launch {
                    viewModel.errorStatus.collect {
                        showSnackBar(it)
                    }
                }

                launch {
                    viewModel.reportState.collect {

                        binding.progressBarReportes.visibility = when {
                            it.isLoading -> View.VISIBLE
                            else -> View.GONE
                        }

                        if (it.reports.isNotEmpty()) reportAdapter?.updateList(it.reports)
                        Timber.d(getString(R.string.TAG_FRAGMENT_REPORTS) + ": " + getString(R.string.FRAGMENT_LOAD_LIST))
                    }
                }


            }
        }
    }

    private fun showSnackBar(string: String) {
        Snackbar
            .make(binding.root, string, Snackbar.LENGTH_INDEFINITE)
            .setAction(getString(R.string.FLAG_RETRY)) {

                viewModel.getReports()

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
        fun newInstance(): ReportsFragment {
            return ReportsFragment()
        }
    }
}