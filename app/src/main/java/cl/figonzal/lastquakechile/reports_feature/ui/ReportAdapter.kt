package cl.figonzal.lastquakechile.reports_feature.ui

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cl.figonzal.lastquakechile.R
import cl.figonzal.lastquakechile.databinding.CardViewReportsBinding
import cl.figonzal.lastquakechile.reports_feature.domain.model.Report
import cl.figonzal.lastquakechile.reports_feature.ui.ReportAdapter.ReportViewHolder

class ReportAdapter(
    private var reportList: MutableList<Report>,
    private val context: Context
) :
    RecyclerView.Adapter<ReportViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val v =
            LayoutInflater.from(parent.context).inflate(R.layout.card_view_reports, parent, false)
        return ReportViewHolder(v)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        holder.bind(reportList[position], context)
    }


    override fun getItemCount(): Int {
        return reportList.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(list: List<Report>) {
        reportList.clear()
        reportList.addAll(list)
        notifyDataSetChanged()
    }

    inner class ReportViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val binding = CardViewReportsBinding.bind(itemView)

        fun bind(report: Report, context: Context) {

            val (reportMonth, nSensitive, nQuakes, promMagnitud, promDepth, maxMagnitude, minDepth, quakesCityList) = report
            val split = reportMonth.split("-".toRegex())
            val anno = split[0]
            val nMonth = split[1].toInt()

            with(binding) {

                tvTitleReport.text =
                    String.format(
                        context.getString(R.string.REPORT_FORMAT),
                        getMonth(nMonth),
                        anno
                    )

                tvNQuakesValue.text = nQuakes.toString()
                tvNSensiblesValue.text = nSensitive.toString()
                tvPromMagnitudValue.text = String.format("%s", promMagnitud)
                tvPromProfValue.text = String.format("%s km", promDepth)
                tvMaxMagValue.text = String.format("%s", maxMagnitude)
                tvMinProfValue.text = String.format("%s km", minDepth)

                tvNombreC1.text = quakesCityList[0].city
                tvNSismosC1.text = quakesCityList[0].nQuakes.toString()

                tvNombreC2.text = quakesCityList[1].city
                tvNSismosC2.text = quakesCityList[1].nQuakes.toString()

                tvNombreC3.text = quakesCityList[2].city
                tvNSismosC3.text = quakesCityList[2].nQuakes.toString()

                tvNombreC4.text = quakesCityList[3].city
                tvNSismosC4.text = quakesCityList[3].nQuakes.toString()
            }
        }
    }

    private fun getMonth(month: Int): String {
        val monthNames = arrayOf(
            context.getString(R.string.JAN),
            context.getString(R.string.FEB),
            context.getString(R.string.MAR),
            context.getString(R.string.APR),
            context.getString(R.string.MAY),
            context.getString(R.string.JUN),
            context.getString(R.string.JUL),
            context.getString(R.string.AUG),
            context.getString(R.string.SEP),
            context.getString(R.string.OCT),
            context.getString(R.string.NOV),
            context.getString(R.string.DEC)
        )
        return monthNames[month - 1]
    }

    init {
        setHasStableIds(true)
    }
}