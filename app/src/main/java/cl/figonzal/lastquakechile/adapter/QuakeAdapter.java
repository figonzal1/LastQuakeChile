package cl.figonzal.lastquakechile.adapter;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import cl.figonzal.lastquakechile.R;
import cl.figonzal.lastquakechile.handlers.DateHandler;
import cl.figonzal.lastquakechile.handlers.ViewsManager;
import cl.figonzal.lastquakechile.model.QuakeModel;
import cl.figonzal.lastquakechile.views.activities.QuakeDetailsActivity;
import timber.log.Timber;

public class QuakeAdapter extends RecyclerView.Adapter<QuakeAdapter.QuakeViewHolder> {

    private List<QuakeModel> quakeModelList;
    private final Activity activity;
    private final DateHandler dateHandler;
    private final ViewsManager viewsManager;

    public QuakeAdapter(List<QuakeModel> quakeModelList, Activity activity, DateHandler dateHandler, ViewsManager viewsManager) {

        this.quakeModelList = quakeModelList;
        this.activity = activity;
        this.dateHandler = dateHandler;
        this.viewsManager = viewsManager;

        setHasStableIds(true);

    }

    @NonNull
    @Override
    public QuakeViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        //Inflar de layout del cardview
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_view_quake, viewGroup, false);

        return new QuakeViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final QuakeViewHolder holder, int position) {

        final QuakeModel model = quakeModelList.get(position);

        holder.tv_ciudad.setText(model.getCiudad());
        holder.tv_referencia.setText(model.getReferencia());

        //Setea la magnitud con un maximo de 1 digito decimal.
        holder.tv_magnitud.setText(String.format(activity.getApplicationContext().getString(R.string.magnitud), model.getMagnitud()));

        //Setear el color de background dependiendo de magnitud del sismo
        int idColor = viewsManager.getMagnitudeColor(model.getMagnitud(), false);
        holder.iv_mag_color.setColorFilter(activity.getApplicationContext().getColor(idColor));

        //SETEO DE Textview HORA
        Map<String, Long> tiempos = dateHandler.dateToDHMS(model.getFecha_local());
        viewsManager.setTimeToTextView(activity.getApplicationContext(), tiempos, holder.tv_hora);

        //Sismo sensible
        if (model.getSensible()) {
            holder.iv_sensible.setVisibility(View.VISIBLE);
        }

        holder.item.setOnClickListener(v -> {

            /*
                Datos para mostrar en el detalle de sismos
             */
            Intent intent = new Intent(activity.getApplicationContext(), QuakeDetailsActivity.class);

            Bundle b = new Bundle();
            b.putString(activity.getApplicationContext().getString(R.string.INTENT_CIUDAD), model.getCiudad());
            b.putString(activity.getApplicationContext().getString(R.string.INTENT_REFERENCIA), model.getReferencia());
            b.putString(activity.getApplicationContext().getString(R.string.INTENT_LATITUD), model.getLatitud());
            b.putString(activity.getApplicationContext().getString(R.string.INTENT_LONGITUD), model.getLongitud());

            //CAmbiar la fecha local a string
            SimpleDateFormat format = new SimpleDateFormat(activity.getApplicationContext().getString(R.string.DATETIME_FORMAT), Locale.US);
            String fecha_local = format.format(model.getFecha_local());
            b.putString(activity.getApplicationContext().getString(R.string.INTENT_FECHA_LOCAL), fecha_local);

            b.putDouble(activity.getApplicationContext().getString(R.string.INTENT_MAGNITUD), model.getMagnitud());
            b.putDouble(activity.getApplicationContext().getString(R.string.INTENT_PROFUNDIDAD), model.getProfundidad());
            b.putString(activity.getApplicationContext().getString(R.string.INTENT_ESCALA), model.getEscala());
            b.putBoolean(activity.getApplicationContext().getString(R.string.INTENT_SENSIBLE), model.getSensible());
            b.putString(activity.getApplicationContext().getString(R.string.INTENT_LINK_FOTO), model.getImagen_url());
            b.putString(activity.getApplicationContext().getString(R.string.INTENT_ESTADO), model.getEstado());

            intent.putExtras(b);

            //LOG
            Timber.i(activity.getApplicationContext().getString(R.string.TRY_INTENT_DETALLE));

            /*
                Seccion transiciones animadas de TextViews
             */
            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(activity,
                    Pair.create(holder.iv_mag_color, "color_magnitud"),
                    Pair.create(holder.tv_magnitud, "magnitud"),
                    Pair.create(holder.tv_ciudad, "ciudad"),
                    Pair.create(holder.tv_referencia, "referencia"),
                    Pair.create(holder.tv_hora, "hora")
            );
            activity.startActivity(intent, options.toBundle());
        });

    }

    @Override
    public int getItemCount() {
        return quakeModelList != null ? quakeModelList.size() : 0;
    }

    //Permite tener los id's fijos y no tener problemas con boleano sensible.
    @Override
    public long getItemId(int position) {
        return position;
    }

    public void updateList(List<QuakeModel> list) {
        this.quakeModelList = list;
    }

    static class QuakeViewHolder extends RecyclerView.ViewHolder {

        private final TextView tv_referencia;
        private final TextView tv_magnitud;
        private final TextView tv_hora;
        private final ImageView iv_mag_color;
        private final ImageView iv_sensible;
        private final ConstraintLayout item;
        private TextView tv_ciudad;

        private QuakeViewHolder(@NonNull View itemView) {
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
}
