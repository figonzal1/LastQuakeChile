package cl.figonzal.lastquakechile;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import cl.figonzal.lastquakechile.services.QuakeUtils;
import cl.figonzal.lastquakechile.views.QuakeDetailsActivity;

public class QuakeAdapter extends RecyclerView.Adapter<QuakeAdapter.QuakeViewHolder> {

    private final List<QuakeModel> quakeModelList;
    private final Context context;
    private final Activity activity;

    public QuakeAdapter(List<QuakeModel> quakeModelList, Context context, Activity activity) {
        this.quakeModelList=quakeModelList;
        this.context =context;
        this.activity = activity;
        setHasStableIds(true);

    }


    public static class QuakeViewHolder extends RecyclerView.ViewHolder {

        private TextView tv_ciudad;
        private final TextView tv_referencia;
        private final TextView tv_magnitud;
        private final TextView tv_hora;
        private final ImageView iv_mag_color;
        private final ImageView iv_sensible;
        private final ConstraintLayout item;


        private QuakeViewHolder(View itemView) {
            super(itemView);

            tv_ciudad = itemView.findViewById(R.id.tv_ciudad);
            tv_referencia = itemView.findViewById(R.id.tv_referencia);
            tv_ciudad = itemView.findViewById(R.id.tv_ciudad);
            tv_magnitud = itemView.findViewById(R.id.tv_magnitud);
            tv_hora = itemView.findViewById(R.id.tv_hora);
            iv_mag_color = itemView.findViewById(R.id.iv_mag_color);
            item = itemView.findViewById(R.id.card_view);
            iv_sensible = itemView.findViewById(R.id.iv_sensible);
        }
    }

    @NonNull
    @Override
    public QuakeViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        //Inflar de layout del cardview
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.quake_cardview,viewGroup,false);
        return new QuakeViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final QuakeViewHolder holder, int position) {

        final QuakeModel model = quakeModelList.get(position);

        holder.tv_ciudad.setText(model.getCiudad());
        holder.tv_referencia.setText(model.getReferencia());

        //Setea la magnitud con un maximo de 1 digito decimal.
        holder.tv_magnitud.setText(String.format(context.getString(R.string.magnitud), model.getMagnitud()));

        //Setear el color de background dependiendo de magnitud del sismo
        holder.iv_mag_color.setColorFilter(context.getColor(QuakeUtils.getMagnitudeColor(model.getMagnitud(), false)));


	    //SETEO DE Textview HORA
	    Map<String, Long> tiempos = QuakeUtils.dateToDHMS(model.getFechaLocal());
	    QuakeUtils.setTimeToTextView(context, tiempos, holder.tv_hora);

        //Sismo sensible
        if (model.getSensible()) {
            holder.iv_sensible.setVisibility(View.VISIBLE);
        }

        holder.item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /*
                    Datos para mostrar en el detalle de sismos
                 */
                Intent intent = new Intent(context, QuakeDetailsActivity.class);
                Bundle b = new Bundle();
                b.putString(context.getString(R.string.INTENT_CIUDAD), model.getCiudad());
                b.putString(context.getString(R.string.INTENT_REFERENCIA), model.getReferencia());
                b.putString(context.getString(R.string.INTENT_LATITUD), model.getLatitud());
                b.putString(context.getString(R.string.INTENT_LONGITUD), model.getLongitud());

	            //CAmbiar la fecha local a string
                SimpleDateFormat format = new SimpleDateFormat(context.getString(R.string.DATETIME_FORMAT), Locale.US);
	            String fecha_local = format.format(model.getFechaLocal());
                b.putString(context.getString(R.string.INTENT_FECHA_LOCAL), fecha_local);

                b.putDouble(context.getString(R.string.INTENT_MAGNITUD), model.getMagnitud());
                b.putDouble(context.getString(R.string.INTENT_PROFUNDIDAD), model.getProfundidad());
                b.putString(context.getString(R.string.INTENT_ESCALA), model.getEscala());
                b.putBoolean(context.getString(R.string.INTENT_SENSIBLE), model.getSensible());
	            b.putString(context.getString(R.string.INTENT_LINK_FOTO), model.getImagenUrl());
                b.putString(context.getString(R.string.INTENT_ESTADO), model.getEstado());

                intent.putExtras(b);

                //LOG
                Log.d(context.getString(R.string.TAG_INTENT), context.getString(R.string.TRY_INTENT_DETALLE));
                Crashlytics.log(Log.DEBUG, context.getString(R.string.TAG_INTENT), context.getString(R.string.TRY_INTENT_DETALLE));

                /*
                    Seccion transiciones animadas de TextViews
                 */
                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(activity,
                        Pair.create((View) holder.iv_mag_color, "color_magnitud"),
                        Pair.create((View) holder.tv_magnitud, "magnitud"),
                        Pair.create((View) holder.tv_ciudad, "ciudad"),
                        Pair.create((View) holder.tv_referencia, "referencia"),
                        Pair.create((View) holder.tv_hora, "hora")
                );
                context.startActivity(intent, options.toBundle());
            }
        });

    }

    @Override
    public int getItemCount() {
        return quakeModelList.size();
    }

    //Permite tener los id's fijos y no tener problemas con boleano sensible.
    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }
}
