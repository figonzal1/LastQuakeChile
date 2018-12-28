package cl.figonzal.lastquakechile;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

        QuakeModel model = quakeModelList.get(position);

        //Guarda despues de 'DE' en la ciudad
        int inicio = model.getReferencia().indexOf("de")+3;
        String ciudad = model.getReferencia().substring(inicio,model.getReferencia().length());

        holder.tv_ciudad.setText(ciudad);
        holder.tv_referencia.setText(model.getReferencia());

        //Setea la magnitud con un maximo de 1 digito decimal.
        holder.tv_magnitud.setText(String.format(Locale.US,"%.1f",model.getMagnitud()));

        //Instancia de utilidades
        QuakeUtils qt = new QuakeUtils();

        //Setear el color de background dependiendo de magnitud del sismo
        holder.iv_mag_color.setColorFilter(context.getColor(qt.getMagnitudeColor(model.getMagnitud())));

        //Calcular el tiempo de sismo
        qt.timeToText(context,model.getFecha_local(),holder);

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(v.getContext(), "Click", Toast.LENGTH_LONG).show();
            }
        });

    }

    @Override
    public int getItemCount() {
        return quakeModelList.size();
    }


}
