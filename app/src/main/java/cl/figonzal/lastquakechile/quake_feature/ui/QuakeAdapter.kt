package cl.figonzal.lastquakechile.quake_feature.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cl.figonzal.lastquakechile.R
import cl.figonzal.lastquakechile.core.getMagnitudeColor
import cl.figonzal.lastquakechile.core.setTimeToTextView
import cl.figonzal.lastquakechile.core.utils.dateToDHMS
import cl.figonzal.lastquakechile.databinding.CardViewQuakeBinding
import cl.figonzal.lastquakechile.quake_feature.domain.model.Quake
import cl.figonzal.lastquakechile.quake_feature.ui.QuakeAdapter.QuakeViewHolder
import timber.log.Timber

class QuakeAdapter(
    private val quakeList: MutableList<Quake>,
    private val activity: Activity,
) : RecyclerView.Adapter<QuakeViewHolder>() {

    init {
        setHasStableIds(true)
    }

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

    //Permite tener los id's fijos y no tener problemas con boleano sensible.
    override fun getItemId(position: Int): Long {
        //TODO: RETURN ID
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(newList: List<Quake>) {
        quakeList.clear()
        quakeList.addAll(newList)
        notifyDataSetChanged()
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
                    this.setTimeToTextView(dateToDHMS(quake.localDate))
                }

                ivSensitive.visibility = when {
                    quake.isSensitive -> View.VISIBLE
                    else -> View.GONE
                }

            }

            //holder.item.setOnClickListener { v: View? ->

            /*
                Datos para mostrar en el detalle de sismos
             */
            //val intent = Intent(activity.applicationContext, QuakeDetailsActivity::class.java)

            /*Bundle b = new Bundle();
            b.putString(activity.getApplicationContext().getString(R.string.INTENT_CIUDAD), model.getCity());
            b.putString(activity.getApplicationContext().getString(R.string.INTENT_REFERENCIA), model.getReference());
            b.putString(activity.getApplicationContext().getString(R.string.INTENT_LATITUD), model.getLatitud());
            b.putString(activity.getApplicationContext().getString(R.string.INTENT_LONGITUD), model.getLongitud());

            //CAmbiar la fecha local a string
            b.putString(activity.getApplicationContext().getString(R.string.INTENT_FECHA_LOCAL), dateHandler.dateToString(activity, model.getLocalDate()));

            b.putDouble(activity.getApplicationContext().getString(R.string.INTENT_MAGNITUD), model.getMagnitude());
            b.putDouble(activity.getApplicationContext().getString(R.string.INTENT_PROFUNDIDAD), model.getDepth());
            b.putString(activity.getApplicationContext().getString(R.string.INTENT_ESCALA), model.getScale());
            b.putString(activity.getApplicationContext().getString(R.string.INTENT_SENSIBLE), model.isSensitive());
            b.putString(activity.getApplicationContext().getString(R.string.INTENT_LINK_FOTO), model.getImagen_url());
            b.putString(activity.getApplicationContext().getString(R.string.INTENT_ESTADO), model.getEstado());

            intent.putExtras(b);*/

            //LOG
            Timber.i(activity.applicationContext.getString(R.string.TRY_INTENT_DETALLE))

            /*
            val options = ActivityOptions.makeSceneTransitionAnimation(
                activity,
                Pair.create(holder.iv_mag_color, "color_magnitud"),
                Pair.create(holder.tv_magnitud, "magnitud"),
                Pair.create(holder.tv_ciudad, "ciudad"),
                Pair.create(holder.tv_referencia, "referencia"),
                Pair.create(holder.tv_hora, "hora")
            )
            activity.startActivity(intent, options.toBundle())*/
        }
    }


}