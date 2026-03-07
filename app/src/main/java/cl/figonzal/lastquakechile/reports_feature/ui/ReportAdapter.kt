package cl.figonzal.lastquakechile.reports_feature.ui

import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import cl.figonzal.lastquakechile.core.ui.composables.ReportCard
import cl.figonzal.lastquakechile.core.ui.composables.theme.AppTheme
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val composeView = ComposeView(parent.context).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        return ReportViewHolder(composeView)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        holder.bind(asyncDiffer.currentList[position])
    }

    override fun getItemCount() = asyncDiffer.currentList.size

    inner class ReportViewHolder(private val composeView: ComposeView) :
        RecyclerView.ViewHolder(composeView) {

        fun bind(report: Report) {
            composeView.setContent {
                AppTheme {
                    ReportCard(report = report)
                }
            }
        }
    }
}
