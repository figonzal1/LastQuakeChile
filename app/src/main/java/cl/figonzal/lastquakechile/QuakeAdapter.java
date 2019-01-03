package cl.figonzal.lastquakechile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class QuakeAdapter extends RecyclerView.Adapter<QuakeAdapter.QuakeViewHolder> {

    private List<QuakeModel> quakeModelList;
    private Context context;

    QuakeAdapter(List<QuakeModel> quakeModelList, Context context){
        this.quakeModelList=quakeModelList;
        this.context =context;
    }


    static class QuakeViewHolder extends RecyclerView.ViewHolder {

        private TextView tv_ciudad;
        private TextView tv_referencia;
        private TextView tv_magnitud;
        protected TextView tv_hora;
        private ImageView iv_mag_color;
        private CardView cardView;


        private QuakeViewHolder(View itemView) {
            super(itemView);

            tv_ciudad = itemView.findViewById(R.id.tv_ciudad);
            tv_referencia = itemView.findViewById(R.id.tv_referencia);
            tv_ciudad = itemView.findViewById(R.id.tv_ciudad);
            tv_magnitud = itemView.findViewById(R.id.tv_magnitud);
            tv_hora = itemView.findViewById(R.id.tv_hora);
            iv_mag_color = itemView.findViewById(R.id.iv_mag_color);
            cardView = itemView.findViewById(R.id.card_view);
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
    public void onBindViewHolder(@NonNull QuakeViewHolder holder, int position) {

        final QuakeModel model = quakeModelList.get(position);

        //Guarda despues de 'DE' en la ciudad
        int inicio = model.getReferencia().indexOf("de")+3;
        final String ciudad = model.getReferencia().substring(inicio, model.getReferencia().length());

        holder.tv_ciudad.setText(ciudad);
        holder.tv_referencia.setText(model.getReferencia());

        //Setea la magnitud con un maximo de 1 digito decimal.
        holder.tv_magnitud.setText(String.format(context.getString(R.string.magnitud), model.getMagnitud()));

        //Setear el color de background dependiendo de magnitud del sismo
        holder.iv_mag_color.setColorFilter(context.getColor(QuakeUtils.getMagnitudeColor(model.getMagnitud())));

        //Calcular el tiempo de sismo
        QuakeUtils.timeToText(context, model.getFecha_local(), holder);

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /*
                    Datos para mostrar en el detalle de sismos
                 */
                Intent intent = new Intent(context, QuakeDetailsActivity.class);
                Bundle b = new Bundle();
                b.putString(context.getString(R.string.INTENT_CIUDAD), ciudad);
                b.putString(context.getString(R.string.INTENT_REFERENCIA), model.getReferencia());
                b.putString(context.getString(R.string.INTENT_LATITUD), model.getLatitud());
                b.putString(context.getString(R.string.INTENT_LONGITUD), model.getLongitud());

                SimpleDateFormat format = new SimpleDateFormat(context.getString(R.string.DATETIME_FORMAT), Locale.US);
                b.putString(context.getString(R.string.INTENT_FECHA_LOCAL), format.format(model.getFecha_local()));

                b.putDouble(context.getString(R.string.INTENT_MAGNITUD), model.getMagnitud());
                b.putDouble(context.getString(R.string.INTENT_PROFUNDIDAD), model.getProfundidad());
                b.putString(context.getString(R.string.INTENT_ESCALA), model.getEscala());
                b.putBoolean(context.getString(R.string.INTENT_SENSIBLE), model.getSensible());
                b.putString(context.getString(R.string.INTENT_LINK_FOTO), model.getImagen_url());

                intent.putExtras(b);
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return quakeModelList.size();
    }


}
