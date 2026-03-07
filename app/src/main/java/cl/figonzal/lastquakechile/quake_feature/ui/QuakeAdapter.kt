package cl.figonzal.lastquakechile.quake_feature.ui

import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import cl.figonzal.lastquakechile.core.ui.composables.QuakeCard
import cl.figonzal.lastquakechile.core.ui.composables.theme.AppTheme
import cl.figonzal.lastquakechile.core.utils.openQuakeDetails
import cl.figonzal.lastquakechile.core.utils.views.toast
import cl.figonzal.lastquakechile.R
import cl.figonzal.lastquakechile.quake_feature.domain.model.Quake
import cl.figonzal.lastquakechile.quake_feature.ui.QuakeAdapter.QuakeViewHolder
import timber.log.Timber

class QuakeAdapter : RecyclerView.Adapter<QuakeViewHolder>() {

    private var asyncDiffer: AsyncListDiffer<Quake>
    private val diffCallback = object : DiffUtil.ItemCallback<Quake>() {

        override fun areItemsTheSame(oldItem: Quake, newItem: Quake) =
            oldItem.quakeCode == newItem.quakeCode

        override fun areContentsTheSame(oldItem: Quake, newItem: Quake) = oldItem == newItem
    }

    var quakes: List<Quake>
        get() = asyncDiffer.currentList
        set(value) {
            asyncDiffer.submitList(value)
            recalculateTimeShowed()
        }

    init {
        asyncDiffer = AsyncListDiffer(this, diffCallback)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): QuakeViewHolder {
        val composeView = ComposeView(viewGroup.context).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        return QuakeViewHolder(composeView)
    }

    override fun onBindViewHolder(holder: QuakeViewHolder, position: Int) {
        holder.bind(quakes[position])
    }

    override fun getItemCount(): Int = asyncDiffer.currentList.size

    inner class QuakeViewHolder(private val composeView: ComposeView) :
        RecyclerView.ViewHolder(composeView) {

        fun bind(quake: Quake) {
            composeView.setContent {
                AppTheme {
                    QuakeCard(
                        quake = quake,
                        onClick = { composeView.context.openQuakeDetails(quake) },
                        onVerifiedClick = { composeView.context.toast(R.string.quake_verified_toast) }
                    )
                }
            }
        }
    }

    /***
     * Function that recalculate the time difference between quake time and device time
     */
    private fun recalculateTimeShowed() {
        quakes.indices.forEach { index -> notifyItemChanged(index) }
        Timber.d("Recalculating time shown in quakeList")
    }
}
