package cl.figonzal.lastquakechile.views.fragments

import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cl.figonzal.lastquakechile.R
import cl.figonzal.lastquakechile.adapter.ReportAdapter
import cl.figonzal.lastquakechile.databinding.FragmentReportsBinding
import cl.figonzal.lastquakechile.model.ReportModel
import cl.figonzal.lastquakechile.repository.ReportRepository
import cl.figonzal.lastquakechile.viewmodel.ReportsViewModel
import cl.figonzal.lastquakechile.viewmodel.ViewModelFactory
import timber.log.Timber
import java.util.*

class ReportsFragment : Fragment() {

    private var reportsViewModel: ReportsViewModel? = null
    private var progressBar: ProgressBar? = null

    private var tv_reportes_vacios: TextView? = null
    private var reportAdapter: ReportAdapter? = null

    private var rv: RecyclerView? = null
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
        instanciarRecursosInterfaz(binding.root)
        iniciarViewModels()
        return binding.root
    }

    private fun iniciarViewModels() {

        //Reports Repository
        val repository = ReportRepository.getIntance(requireContext())

        reportsViewModel =
            ViewModelProvider(
                requireActivity(),
                ViewModelFactory(application, repository)
            )[ReportsViewModel::class.java]

        reportsViewModel!!.isLoading.observe(requireActivity(), { aBoolean: Boolean ->
            if (aBoolean) {
                showProgressBar()
            } else {
                hideProgressBar()
                if (reportAdapter!!.itemCount == 0) {
                    tv_reportes_vacios!!.visibility = View.VISIBLE
                } else {
                    tv_reportes_vacios!!.visibility = View.INVISIBLE
                }
            }
        })
        reportsViewModel!!.showReports()
            .observe(requireActivity(), { reportList: List<ReportModel?>? ->
                reportAdapter!!.updateList(reportList)
                reportAdapter!!.notifyDataSetChanged()

                //LOG ZONE
                Timber.i(getString(R.string.TAG_FRAGMENT_REPORTS) + ": " + getString(R.string.FRAGMENT_LOAD_LIST))
            })
        reportsViewModel!!.showMsgErrorList().observe(requireActivity(), { status: String? ->
            progressBar!!.visibility = View.INVISIBLE
            reportAdapter!!.notifyDataSetChanged()
            reportAdapter!!.notifyDataSetChanged()
        })
    }

    private fun hideProgressBar() {
        progressBar!!.visibility = View.INVISIBLE
        rv!!.visibility = View.VISIBLE
    }

    private fun showProgressBar() {
        progressBar!!.visibility = View.VISIBLE
        tv_reportes_vacios!!.visibility = View.INVISIBLE
        rv!!.visibility = View.INVISIBLE
    }

    private fun instanciarRecursosInterfaz(v: View) {
        rv = binding.recycleViewReports.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
        }

        tv_reportes_vacios = binding.tvReportesVacios
        tv_reportes_vacios!!.visibility = View.INVISIBLE

        progressBar = binding.progressBarReportes
        progressBar!!.visibility = View.VISIBLE

        //Set adapter
        reportAdapter = ReportAdapter(ArrayList(), requireContext())
        rv!!.adapter = reportAdapter
    }

    companion object {
        @JvmStatic
        fun newInstance(): ReportsFragment {
            return ReportsFragment()
        }
    }
}