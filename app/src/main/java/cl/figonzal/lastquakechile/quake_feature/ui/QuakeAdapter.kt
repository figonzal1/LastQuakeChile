package cl.figonzal.lastquakechile.quake_feature.ui

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import cl.figonzal.lastquakechile.R
import cl.figonzal.lastquakechile.core.utils.openQuakeDetails
import cl.figonzal.lastquakechile.core.utils.views.QUAKE_DETAILS_MAGNITUDE_FORMAT
import cl.figonzal.lastquakechile.core.utils.views.getMagnitudeColor
import cl.figonzal.lastquakechile.core.utils.views.layoutInflater
import cl.figonzal.lastquakechile.core.utils.views.timeToText
import cl.figonzal.lastquakechile.core.utils.views.toast
import cl.figonzal.lastquakechile.databinding.CardViewQuakeBinding
import cl.figonzal.lastquakechile.quake_feature.domain.model.Quake
import cl.figonzal.lastquakechile.quake_feature.ui.QuakeAdapter.QuakeViewHolder
import timber.log.Timber
import java.util.Locale

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

                tvMagnitude.text = String.format(
                    Locale.getDefault(),
                    QUAKE_DETAILS_MAGNITUDE_FORMAT, quake.magnitude
                )

                val idColor = getMagnitudeColor(quake.magnitude, false)
                ivMagColor.setColorFilter(
                    itemView.resources.getColor(idColor, itemView.context.theme)
                )

                tvHour.timeToText(quake, true)

                ivSensitive.visibility = when {
                    quake.isSensitive -> View.VISIBLE
                    else -> View.GONE
                }

                //Verified status
                ivVerified.visibility = when {
                    quake.isVerified -> View.VISIBLE
                    else -> View.GONE
                }

                ivVerified.setOnClickListener {
                    itemView.context.toast(R.string.quake_verified_toast)
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
            binding?.tvHour?.timeToText(quake, true)
            notifyItemChanged(index)
        }
        Timber.d("Recalculating time shown in quakeList")
    }
}

