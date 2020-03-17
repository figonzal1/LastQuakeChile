package cl.figonzal.lastquakechile.views.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import java.util.List;

import cl.figonzal.lastquakechile.R;
import cl.figonzal.lastquakechile.model.ReportModel;
import cl.figonzal.lastquakechile.viewmodel.ReportsViewModel;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ReportsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ReportsFragment extends Fragment {

    private List<ReportModel> reportModelList;
    private ReportsViewModel reportsViewModel;
    private ProgressBar progressBar;
    private TextView tv_reportes_vacios;

    public ReportsFragment() {
    }

    public static ReportsFragment newInstance() {
        return new ReportsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_reports, container, false);

        reportsViewModel = new ViewModelProvider(requireActivity()).get(ReportsViewModel.class);

        reportsViewModel.isLoading().observe(requireActivity(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean) {
                    progressBar.setVisibility(View.VISIBLE);
                    tv_reportes_vacios.setVisibility(View.INVISIBLE);
                } else {
                    progressBar.setVisibility(View.INVISIBLE);

                    if (reportModelList.size() == 0) {
                        tv_reportes_vacios.setVisibility(View.VISIBLE);
                    } else {
                        tv_reportes_vacios.setVisibility(View.INVISIBLE);
                    }
                }
            }
        });


        return v;
    }
}
