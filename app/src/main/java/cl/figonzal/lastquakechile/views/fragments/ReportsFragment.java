package cl.figonzal.lastquakechile.views.fragments;

import android.app.Application;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import cl.figonzal.lastquakechile.R;
import cl.figonzal.lastquakechile.adapter.ReportAdapter;
import cl.figonzal.lastquakechile.model.ReportModel;
import cl.figonzal.lastquakechile.repository.NetworkRepository;
import cl.figonzal.lastquakechile.repository.ReportRepository;
import cl.figonzal.lastquakechile.viewmodel.ReportsViewModel;
import cl.figonzal.lastquakechile.viewmodel.ViewModelFactory;
import timber.log.Timber;

public class ReportsFragment extends Fragment {

    private List<ReportModel> reportModelList;
    private ReportsViewModel reportsViewModel;
    private ProgressBar progressBar;
    private TextView tv_reportes_vacios;
    private ReportAdapter reportAdapter;
    private RecyclerView rv;

    private Application application;

    public ReportsFragment() {
    }

    public static ReportsFragment newInstance() {
        return new ReportsFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        application = requireActivity().getApplication();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_reports, container, false);

        instanciarRecursosInterfaz(v);

        iniciarViewModels(v);

        return v;
    }

    private void iniciarViewModels(final View v) {

        reportsViewModel.isLoading().observe(requireActivity(), aBoolean -> {

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
        });

        reportsViewModel.showReports().observe(requireActivity(), reportModels -> {

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
            Timber.i(getString(R.string.TAG_FRAGMENT_REPORTS) + ": " + getString(R.string.FRAGMENT_LOAD_LIST));
        });

        reportsViewModel.showMsgErrorList().observe(requireActivity(), status -> {

            if (status != null) {

                progressBar.setVisibility(View.INVISIBLE);
                rv.setVisibility(View.INVISIBLE);
                reportAdapter.notifyDataSetChanged();
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

        NetworkRepository<ReportModel> repository = ReportRepository.getIntance(application);
        reportsViewModel = new ViewModelProvider(requireActivity(), new ViewModelFactory(application, repository)).get(ReportsViewModel.class);

        reportAdapter = new ReportAdapter(
                reportModelList,
                requireContext()
        );

        rv.setAdapter(reportAdapter);
    }
}
