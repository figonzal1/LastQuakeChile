package cl.figonzal.lastquakechile.views.fragments

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
import cl.figonzal.lastquakechile.adapter.ReportAdapter
import cl.figonzal.lastquakechile.databinding.FragmentReportsBinding
import cl.figonzal.lastquakechile.newcode.ui.NewReportsViewModel
import cl.figonzal.lastquakechile.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*

class ReportsFragment : Fragment() {

    private var reportAdapter: ReportAdapter? = null

    private var application: Application? = null

    private lateinit var binding: FragmentReportsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        application = requireActivity().application
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentReportsBinding.inflate(inflater, container, false)
        instanciarRecursosInterfaz()
        iniciarViewModels()
        return binding.root
    }

    private fun instanciarRecursosInterfaz() {

        with(binding) {
            recycleViewReports.apply {
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(context)

                reportAdapter = ReportAdapter(ArrayList(), requireContext())
                recycleViewReports.adapter = reportAdapter
            }
        }

    }

    private fun iniciarViewModels() {

        val viewModelNew: NewReportsViewModel = ViewModelProvider(
            requireActivity(),
            ViewModelFactory(
                requireActivity().application
            )
        )[NewReportsViewModel::class.java]

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModelNew.reports.collect {
                    reportAdapter?.updateList(it)
                    Timber.i(getString(R.string.TAG_FRAGMENT_REPORTS) + ": " + getString(R.string.FRAGMENT_LOAD_LIST))
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModelNew.spinner.collect {
                    binding.progressBarReportes.visibility = it
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