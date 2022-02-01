package cl.figonzal.lastquakechile.views.fragments

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
import cl.figonzal.lastquakechile.databinding.FragmentReportsBinding
import cl.figonzal.lastquakechile.reports_feature.ui.NewReportsViewModel
import cl.figonzal.lastquakechile.reports_feature.ui.ReportAdapter
import cl.figonzal.lastquakechile.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch
import timber.log.Timber

class ReportsFragment : Fragment() {

    private var reportAdapter: ReportAdapter? = null
    private lateinit var binding: FragmentReportsBinding


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

        val viewModelNew: NewReportsViewModel = ViewModelProvider(
            requireActivity(),
            ViewModelFactory(
                requireActivity().application
            )
        )[NewReportsViewModel::class.java]


        lifecycleScope.launch {

            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    viewModelNew.reportState.collect {

                        binding.progressBarReportes.visibility = when {
                            it.isLoading -> View.VISIBLE
                            else -> View.GONE
                        }

                        reportAdapter?.updateList(it.reports)
                        Timber.i(getString(R.string.TAG_FRAGMENT_REPORTS) + ": " + getString(R.string.FRAGMENT_LOAD_LIST))
                    }
                }
            }
        }
    }


    companion object {
        @JvmStatic
        fun newInstance(): ReportsFragment {
            return ReportsFragment()
        }
    }
}