package cl.figonzal.lastquakechile.quake_feature.ui

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import cl.figonzal.lastquakechile.R
import cl.figonzal.lastquakechile.core.utils.openQuakeDetails
import cl.figonzal.lastquakechile.core.utils.views.configSensitive
import cl.figonzal.lastquakechile.core.utils.views.configVerified
import cl.figonzal.lastquakechile.core.utils.views.formatFilterColor
import cl.figonzal.lastquakechile.core.utils.views.formatMagnitude
import cl.figonzal.lastquakechile.core.utils.views.formatQuakeTime
import cl.figonzal.lastquakechile.core.utils.views.layoutInflater
import cl.figonzal.lastquakechile.core.utils.views.toast
import cl.figonzal.lastquakechile.databinding.CardViewQuakeBinding
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

    private var binding: CardViewQuakeBinding? = null

    var quakes: List<Quake>
        get() = asyncDiffer.currentList
        set(value) {
            asyncDiffer.submitList(value)
            recalculateTimeShowed()
        }

    init {
        asyncDiffer = AsyncListDiffer(this, diffCallback)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int) =
        QuakeViewHolder(viewGroup.layoutInflater(R.layout.card_view_quake))

    override fun onBindViewHolder(holder: QuakeViewHolder, position: Int) {
        holder.bind(quakes[position])
    }

    override fun getItemCount(): Int = asyncDiffer.currentList.size


    inner class QuakeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {


        fun bind(quake: Quake) {

            binding = CardViewQuakeBinding.bind(itemView)

            with(binding!!) {

                tvCity.text = quake.city
                tvReference.text = quake.reference

                tvMagnitude.formatMagnitude(quake)

                ivMagColor.formatFilterColor(itemView.context, quake)

                tvHour.formatQuakeTime(quake, true)

                ivSensitive.configSensitive(quake)

                with(ivVerified) {
                    configVerified(quake)
                    setOnClickListener {
                        itemView.context.toast(R.string.quake_verified_toast)
                    }
                }

                root.setOnClickListener { itemView.context.openQuakeDetails(quake) }
            }
        }
    }

    /***
     * Function that recalculate the time difference between quake time and device time
     */
    private fun recalculateTimeShowed() {

        quakes.forEachIndexed { index, quake ->
            binding?.tvHour?.formatQuakeTime(quake, true)
            notifyItemChanged(index)
        }
        Timber.d("Recalculating time shown in quakeList")
    }
}

