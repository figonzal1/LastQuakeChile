package cl.figonzal.lastquakechile.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import cl.figonzal.lastquakechile.R;
import cl.figonzal.lastquakechile.model.QuakesCity;
import cl.figonzal.lastquakechile.model.ReportModel;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ReportViewHolder> {

    private final Context context;
    private List<ReportModel> reportList;

    public ReportAdapter(List<ReportModel> reportList, Context context) {

        this.reportList = reportList;
        this.context = context;
        setHasStableIds(true);
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_reports, parent, false);
        return new ReportViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportAdapter.ReportViewHolder holder, int position) {

        ReportModel reportModel = reportList.get(position);

        String[] split = reportModel.getMes_reporte().split("-");
        String anno = split[0];
        int n_mes = Integer.parseInt(split[1]);

        holder.tv_title_report.setText(String.format(context.getString(R.string.FORMATO_REPORTE), getMonth(n_mes), anno));
        holder.tv_n_quakes_value.setText(String.valueOf(reportModel.getN_sismos()));
        holder.tv_n_sensibles_value.setText(String.valueOf(reportModel.getN_sensibles()));
        holder.tv_prom_magnitud_value.setText(String.format("%s", reportModel.getProm_magnitud()));
        holder.tv_prom_prof_value.setText(String.format("%s km", reportModel.getProm_profundidad()));
        holder.tv_max_mag_value.setText(String.format("%s", reportModel.getMax_magnitud()));
        holder.tv_min_prof_value.setText(String.format("%s km", reportModel.getMin_profundidad()));

        List<QuakesCity> quakesCityList = reportModel.getTop_ciudades();

        holder.tv_nombre_c1.setText(quakesCityList.get(0).getCiudad());
        holder.tv_n_sismos_c1.setText(String.valueOf(quakesCityList.get(0).getN_sismos()));

        holder.tv_nombre_c2.setText(quakesCityList.get(1).getCiudad());
        holder.tv_n_sismos_c2.setText(String.valueOf(quakesCityList.get(1).getN_sismos()));

        holder.tv_nombre_c3.setText(quakesCityList.get(2).getCiudad());
        holder.tv_n_sismos_c3.setText(String.valueOf(quakesCityList.get(2).getN_sismos()));

        holder.tv_nombre_c4.setText(quakesCityList.get(3).getCiudad());
        holder.tv_n_sismos_c4.setText(String.valueOf(quakesCityList.get(3).getN_sismos()));
    }

    private String getMonth(int month) {
        String[] monthNames = {context.getString(R.string.ENERO), context.getString(R.string.FEBRERO), context.getString(R.string.MARZO), context.getString(R.string.ABRIL), context.getString(R.string.MAYO), context.getString(R.string.JUNIO), context.getString(R.string.JULIO), context.getString(R.string.AGOSTO), context.getString(R.string.SEPTIEMBRE), context.getString(R.string.OCTUBRE), context.getString(R.string.NOVIEMBRE), context.getString(R.string.DICIEMBRE)};
        return monthNames[month - 1];
    }

    @Override
    public int getItemCount() {
        return reportList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void actualizarLista(List<ReportModel> list) {

        this.reportList = list;
        notifyDataSetChanged();
    }

    public List<ReportModel> getReportList() {
        return reportList;
    }

    static class ReportViewHolder extends RecyclerView.ViewHolder {

        private final TextView tv_title_report;
        private final TextView tv_n_quakes_value;
        private final TextView tv_n_sensibles_value;
        private final TextView tv_prom_magnitud_value;
        private final TextView tv_prom_prof_value;
        private final TextView tv_max_mag_value;
        private final TextView tv_min_prof_value;

        private final TextView tv_nombre_c1;
        private final TextView tv_nombre_c2;
        private final TextView tv_nombre_c3;
        private final TextView tv_nombre_c4;

        private final TextView tv_n_sismos_c1;
        private final TextView tv_n_sismos_c2;
        private final TextView tv_n_sismos_c3;
        private final TextView tv_n_sismos_c4;


        private ReportViewHolder(@NonNull View itemView) {
            super(itemView);

            tv_title_report = itemView.findViewById(R.id.tv_title_report);
            tv_n_quakes_value = itemView.findViewById(R.id.tv_n_quakes_value);
            tv_n_sensibles_value = itemView.findViewById(R.id.tv_n_sensibles_value);
            tv_prom_magnitud_value = itemView.findViewById(R.id.tv_prom_magnitud_value);
            tv_prom_prof_value = itemView.findViewById(R.id.tv_prom_prof_value);
            tv_max_mag_value = itemView.findViewById(R.id.tv_max_mag_value);
            tv_min_prof_value = itemView.findViewById(R.id.tv_min_prof_value);

            tv_nombre_c1 = itemView.findViewById(R.id.tv_nombre_c1);
            tv_nombre_c2 = itemView.findViewById(R.id.tv_nombre_c2);
            tv_nombre_c3 = itemView.findViewById(R.id.tv_nombre_c3);
            tv_nombre_c4 = itemView.findViewById(R.id.tv_nombre_c4);

            tv_n_sismos_c1 = itemView.findViewById(R.id.tv_n_sismos_c1);
            tv_n_sismos_c2 = itemView.findViewById(R.id.tv_n_sismos_c2);
            tv_n_sismos_c3 = itemView.findViewById(R.id.tv_n_sismos_c3);
            tv_n_sismos_c4 = itemView.findViewById(R.id.tv_n_sismos_c4);
        }
    }
}
