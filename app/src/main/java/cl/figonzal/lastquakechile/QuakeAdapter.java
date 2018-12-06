package cl.figonzal.lastquakechile;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class QuakeAdapter extends RecyclerView.Adapter<QuakeAdapter.QuakeViewHolder> {

    private List<QuakeModel> quakeModelList;

    QuakeAdapter(List<QuakeModel> quakeModelList){
        this.quakeModelList=quakeModelList;
    }


    static class QuakeViewHolder extends RecyclerView.ViewHolder {

        private TextView tv_nombre;

        private QuakeViewHolder(View itemView) {
            super(itemView);

            tv_nombre = itemView.findViewById(R.id.tv_nombre);
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

    }

    @Override
    public int getItemCount() {
        return quakeModelList.size();
    }
}
