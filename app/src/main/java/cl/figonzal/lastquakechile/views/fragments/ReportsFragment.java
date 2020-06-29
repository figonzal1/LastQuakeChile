package cl.figonzal.lastquakechile.views.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.util.ArrayList;
import java.util.List;

import cl.figonzal.lastquakechile.R;
import cl.figonzal.lastquakechile.adapter.ReportAdapter;
import cl.figonzal.lastquakechile.model.ReportModel;
import cl.figonzal.lastquakechile.viewmodel.ReportsViewModel;

public class ReportsFragment extends Fragment {

    private List<ReportModel> reportModelList;
    private ReportsViewModel reportsViewModel;
    private ProgressBar progressBar;
    private TextView tv_reportes_vacios;
    private ReportAdapter reportAdapter;
    private RecyclerView rv;
    private Snackbar sSnackbar;

    private FirebaseCrashlytics crashlytics;

    public ReportsFragment() {
    }

    public static ReportsFragment newInstance() {
        return new ReportsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        crashlytics = FirebaseCrashlytics.getInstance();

        View v = inflater.inflate(R.layout.fragment_reports, container, false);

        instanciarRecursosInterfaz(v);

        iniciarViewModels(v);

        return v;
    }

    private void iniciarViewModels(final View v) {

        reportsViewModel.isLoading().observe(requireActivity(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean) {
                    progressBar.setVisibility(View.VISIBLE);
                    tv_reportes_vacios.setVisibility(View.INVISIBLE);
                    rv.setVisibility(View.INVISIBLE);
                } else {
                    progressBar.setVisibility(View.INVISIBLE);
                    rv.setVisibility(View.VISIBLE);

                    if (reportModelList.size() == 0) {
                        tv_reportes_vacios.setVisibility(View.VISIBLE);
                    } else {
                        tv_reportes_vacios.setVisibility(View.INVISIBLE);
                    }
                }
            }
        });

        reportsViewModel.showReports().observe(requireActivity(), new Observer<List<ReportModel>>() {
            @Override
            public void onChanged(List<ReportModel> reportModels) {

                if (reportModels != null) {
                    reportModelList = reportModels;
                    reportAdapter.actualizarLista(reportModelList);

                    reportModelList = reportAdapter.getReportList();

                    progressBar.setVisibility(View.INVISIBLE);
                    if (reportModelList.size() == 0) {
                        tv_reportes_vacios.setVisibility(View.VISIBLE);
                    } else {
                        tv_reportes_vacios.setVisibility(View.INVISIBLE);
                    }
                }

                //LOG ZONE
                Log.d(getString(R.string.TAG_FRAGMENT_REPORTS),
                        getString(R.string.FRAGMENT_LOAD_LIST));

                crashlytics.log(getString(R.string.TAG_FRAGMENT_REPORTS) + getString(R.string.FRAGMENT_LOAD_LIST));
            }
        });

        reportsViewModel.showMsgErrorList().observe(requireActivity(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String status) {
                if (status != null) {
                    progressBar.setVisibility(View.INVISIBLE);
                    rv.setVisibility(View.INVISIBLE);
                    reportAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    private void instanciarRecursosInterfaz(View v) {

        reportModelList = new ArrayList<>();

        rv = v.findViewById(R.id.recycle_view_reports);
        rv.setHasFixedSize(true);

        LinearLayoutManager ly = new LinearLayoutManager(getContext());
        rv.setLayoutManager(ly);

        tv_reportes_vacios = v.findViewById(R.id.tv_reportes_vacios);
        tv_reportes_vacios.setVisibility(View.INVISIBLE);

        progressBar = v.findViewById(R.id.progress_bar_reportes);
        progressBar.setVisibility(View.VISIBLE);

        reportsViewModel = new ViewModelProvider(requireActivity()).get(ReportsViewModel.class);

        reportAdapter = new ReportAdapter(
                reportModelList,
                requireContext(),
                requireActivity()
        );
        rv.setAdapter(reportAdapter);

    }
}
