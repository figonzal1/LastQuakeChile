package cl.figonzal.lastquakechile.reports_feature.ui

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import cl.figonzal.lastquakechile.R
import cl.figonzal.lastquakechile.core.utils.views.REPORT_FORMAT
import cl.figonzal.lastquakechile.core.utils.views.getMonth
import cl.figonzal.lastquakechile.core.utils.views.layoutInflater
import cl.figonzal.lastquakechile.databinding.CardViewReportsBinding
import cl.figonzal.lastquakechile.reports_feature.domain.model.Report
import cl.figonzal.lastquakechile.reports_feature.ui.ReportAdapter.ReportViewHolder

class ReportAdapter : RecyclerView.Adapter<ReportViewHolder>() {

    private var asyncDiffer: AsyncListDiffer<Report>
    private val diffCallback = object : DiffUtil.ItemCallback<Report>() {
        override fun areItemsTheSame(oldItem: Report, newItem: Report) = oldItem == newItem

        override fun areContentsTheSame(oldItem: Report, newItem: Report) = oldItem == newItem
    }

    var reports: List<Report>
        get() = asyncDiffer.currentList
        set(value) = asyncDiffer.submitList(value)

    init {
        asyncDiffer = AsyncListDiffer(this, diffCallback)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ReportViewHolder(parent.layoutInflater(R.layout.card_view_reports))

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        holder.bind(asyncDiffer.currentList[position])
    }

    override fun getItemCount() = asyncDiffer.currentList.size

    inner class ReportViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val binding = CardViewReportsBinding.bind(itemView)

        fun bind(report: Report) {

            val (reportMonth, nSensitive, nQuakes, promMagnitud, promDepth, maxMagnitude, minDepth, quakesCityList) = report
            val split = reportMonth.split("-".toRegex())
            val anno = split[0]
            val nMonth = split[1].toInt()

            with(binding) {

                tvTitleReport.text =
                    String.format(
                        REPORT_FORMAT,
                        itemView.context.getMonth(nMonth),
                        anno
                    )

                tvNQuakesValue.text = nQuakes.toString()
                tvNSensiblesValue.text = nSensitive.toString()
                tvPromMagnitudValue.text = String.format("%s", promMagnitud)
                tvPromProfValue.text = String.format("%s km", promDepth)
                tvMaxMagValue.text = String.format("%s", maxMagnitude)
                tvMinProfValue.text = String.format("%s km", minDepth)

                listOf(
                    tvNombreC1 to tvNSismosC1,
                    tvNombreC2 to tvNSismosC2,
                    tvNombreC3 to tvNSismosC3,
                    tvNombreC4 to tvNSismosC4
                ).forEachIndexed { index, (tvName: TextView, tvCount: TextView) ->
                    val city = quakesCityList.getOrNull(index)
                    if (city != null) {
                        tvName.visibility = View.VISIBLE
                        tvCount.visibility = View.VISIBLE
                        tvName.text = city.city
                        tvCount.text = city.nQuakes.toString()
                    } else {
                        tvName.visibility = View.GONE
                        tvCount.visibility = View.GONE
                    }
                }
            }
        }
    }
}