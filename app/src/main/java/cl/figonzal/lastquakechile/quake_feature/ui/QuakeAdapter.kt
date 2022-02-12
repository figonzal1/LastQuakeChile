package cl.figonzal.lastquakechile.quake_feature.ui

import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import cl.figonzal.lastquakechile.R
import cl.figonzal.lastquakechile.core.utils.getMagnitudeColor
import cl.figonzal.lastquakechile.core.utils.localDateToDHMS
import cl.figonzal.lastquakechile.core.utils.setTimeToTextView
import cl.figonzal.lastquakechile.databinding.CardViewQuakeBinding
import cl.figonzal.lastquakechile.quake_feature.domain.model.Quake
import cl.figonzal.lastquakechile.quake_feature.ui.QuakeAdapter.QuakeViewHolder
import timber.log.Timber


class QuakeAdapter(
    private val quakeList: MutableList<Quake>,
    private val activity: Activity,
) : RecyclerView.Adapter<QuakeViewHolder>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): QuakeViewHolder {

        //Inflar de layout del cardview
        val v = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.card_view_quake, viewGroup, false)
        return QuakeViewHolder(v)
    }

    override fun onBindViewHolder(holder: QuakeViewHolder, position: Int) {
        holder.bind(quakeList[position], activity)
    }

    override fun getItemCount(): Int {
        return quakeList.size
    }

    fun updateList(newList: List<Quake>) {

        val diffCallback = QuakeCallback(quakeList, newList)
        val diffQuakes = DiffUtil.calculateDiff(diffCallback)
        quakeList.clear()
        quakeList.addAll(newList)
        diffQuakes.dispatchUpdatesTo(this)
    }

    inner class QuakeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val binding = CardViewQuakeBinding.bind(itemView)

        fun bind(quake: Quake, activity: Activity) {

            with(binding) {
                tvCity.text = quake.city
                tvReference.text = quake.reference

                //Setea la magnitud con un maximo de 1 digito decimal.
                tvMagnitude.text =
                    String.format(
                        activity.applicationContext.getString(R.string.magnitud),
                        quake.magnitude
                    )

                //Setear el color de background dependiendo de magnitud del sismo
                val idColor = getMagnitudeColor(quake.magnitude, false)
                ivMagColor.setColorFilter(activity.applicationContext.getColor(idColor))

                tvHour.apply {
                    this.setTimeToTextView(quake.localDate.localDateToDHMS())
                }

                ivSensitive.visibility = when {
                    quake.isSensitive -> View.VISIBLE
                    else -> View.GONE
                }

                root.setOnClickListener {

                    /*
                        Datos para mostrar en el detalle de sismos
                     */
                    Intent(activity.applicationContext, QuakeDetailsActivity::class.java).apply {
                        putExtra("quake", quake)


                        //LOG
                        Timber.i(activity.applicationContext.getString(R.string.TRY_INTENT_DETALLE))


                        val options = ActivityOptions.makeSceneTransitionAnimation(
                            activity,
                            Pair(binding.ivMagColor, "color_magnitud"),
                            Pair(binding.tvMagnitude, "magnitud"),
                            Pair(binding.tvCity, "ciudad"),
                            Pair(binding.tvReference, "referencia"),
                            Pair(binding.tvHour, "hora")
                        )
                        activity.startActivity(this, options.toBundle())
                    }
                }

            }
        }
    }

    inner class QuakeCallback(private val oldList: List<Quake>, private val newList: List<Quake>) :
        DiffUtil.Callback() {

        override fun getOldListSize(): Int {
            return oldList.size
        }

        override fun getNewListSize(): Int {
            return newList.size
        }

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].quakeCode == newList[newItemPosition].quakeCode
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].magnitude == newList[newItemPosition].magnitude &&
                    oldList[oldItemPosition].city == newList[newItemPosition].city &&
                    oldList[oldItemPosition].quakeCode == newList[newItemPosition].quakeCode

        }
    }
}

