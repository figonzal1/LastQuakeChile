package cl.figonzal.lastquakechile.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import cl.figonzal.lastquakechile.R;
import cl.figonzal.lastquakechile.model.ChangeLog;

public class ChangeLogAdapter extends RecyclerView.Adapter<ChangeLogAdapter.ChangeLogViewHolder> {

    private List<ChangeLog> changeLogList;

    public ChangeLogAdapter(List<ChangeLog> changeLogList) {
        this.changeLogList = changeLogList;
    }

    @NonNull
    @Override
    public ChangeLogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_change_log, parent, false);

        return new ChangeLogViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ChangeLogViewHolder holder, int position) {

        if (position == 0) {
            holder.tvBadge.setVisibility(View.VISIBLE);
        }

        ChangeLog changeLog = changeLogList.get(position);

        holder.tvVersion.setText(changeLog.getVersion());
        holder.tvReleaseDate.setText(changeLog.getReleaseDate());

        String changes = "";

        for (int i = 0; i < changeLog.getChanges().length; i++) {

            String ch = changeLog.getChanges()[i];

            if (i > 0) {
                changes = changes.concat("\n" + ch);
            } else {
                changes = changes.concat(ch);
            }
        }

        holder.tvChangesList.setText(changes);
    }

    @Override
    public int getItemCount() {
        return changeLogList.size();
    }


    static class ChangeLogViewHolder extends RecyclerView.ViewHolder {

        private TextView tvVersion;
        private TextView tvReleaseDate;
        private TextView tvChangesList;
        private TextView tvBadge;

        private ChangeLogViewHolder(@NonNull View itemView) {
            super(itemView);

            tvVersion = itemView.findViewById(R.id.tv_version);
            tvReleaseDate = itemView.findViewById(R.id.tv_release_date);
            tvChangesList = itemView.findViewById(R.id.tv_changes_list);
            tvBadge = itemView.findViewById(R.id.tv_badge);
        }
    }
}
