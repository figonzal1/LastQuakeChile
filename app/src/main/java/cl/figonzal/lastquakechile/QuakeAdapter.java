package cl.figonzal.lastquakechile;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
        private TextView tv_hora;

        private QuakeViewHolder(View itemView) {
            super(itemView);

            tv_ciudad = itemView.findViewById(R.id.tv_ciudad);
            tv_referencia = itemView.findViewById(R.id.tv_referencia);
            tv_ciudad = itemView.findViewById(R.id.tv_ciudad);
            tv_magnitud = itemView.findViewById(R.id.tv_magnitud);
            tv_hora = itemView.findViewById(R.id.tv_hora);
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

        int inicio = model.getReferencia().indexOf("de")+3;
        String ciudad = model.getReferencia().substring(inicio,model.getReferencia().length());

        holder.tv_ciudad.setText(ciudad);
        holder.tv_magnitud.setText(String.format(Locale.US,"%.1f",model.getMagnitud()));
        holder.tv_referencia.setText(model.getReferencia());

        SimpleDateFormat format = new SimpleDateFormat(context.getString(R.string.TIME_FORMAT), Locale.US);
        String date = format.format(model.getFecha_local());
        holder.tv_hora.setText(date);

    }

    @Override
    public int getItemCount() {
        return quakeModelList.size();
    }
}