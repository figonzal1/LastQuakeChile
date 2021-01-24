package cl.figonzal.lastquakechile.views.fragments;

import android.app.Application;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import cl.figonzal.lastquakechile.R;
import cl.figonzal.lastquakechile.adapter.ReportAdapter;
import cl.figonzal.lastquakechile.model.ReportModel;
import cl.figonzal.lastquakechile.repository.NetworkRepository;
import cl.figonzal.lastquakechile.repository.ReportRepository;
import cl.figonzal.lastquakechile.viewmodel.ReportsViewModel;
import cl.figonzal.lastquakechile.viewmodel.ViewModelFactory;
import timber.log.Timber;

public class ReportsFragment extends Fragment {

    private ReportsViewModel reportsViewModel;
    private ProgressBar progressBar;
    private TextView tv_reportes_vacios;
    private ReportAdapter reportAdapter;
    private RecyclerView rv;

    private Application application;

    public ReportsFragment() {
    }

    @NonNull
    public static ReportsFragment newInstance() {
        return new ReportsFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        application = requireActivity().getApplication();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_reports, container, false);

        iniciarViewModels();

        instanciarRecursosInterfaz(v);

        return v;
    }

    private void iniciarViewModels() {

        //Reports Repository
        NetworkRepository<ReportModel> repository = ReportRepository.getIntance(application.getApplicationContext());
        reportsViewModel = new ViewModelProvider(requireActivity(), new ViewModelFactory(application, repository)).get(ReportsViewModel.class);

        reportsViewModel.isLoading().observe(requireActivity(), aBoolean -> {

            if (aBoolean) {
                showProgressBar();
            } else {
                hideProgressBar();

                if (reportAdapter.getItemCount() == 0) {
                    tv_reportes_vacios.setVisibility(View.VISIBLE);
                } else {
                    tv_reportes_vacios.setVisibility(View.INVISIBLE);
                }
            }
        });

        reportsViewModel.showReports().observe(requireActivity(), reportList -> {

            reportAdapter.updateList(reportList);
            reportAdapter.notifyDataSetChanged();

            //LOG ZONE
            Timber.i(getString(R.string.TAG_FRAGMENT_REPORTS) + ": " + getString(R.string.FRAGMENT_LOAD_LIST));
        });

        reportsViewModel.showMsgErrorList().observe(requireActivity(), status -> {

            progressBar.setVisibility(View.INVISIBLE);
            reportAdapter.notifyDataSetChanged();
            reportAdapter.notifyDataSetChanged();
        });
    }

    private void hideProgressBar() {
        progressBar.setVisibility(View.INVISIBLE);
        rv.setVisibility(View.VISIBLE);
    }

    private void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
        tv_reportes_vacios.setVisibility(View.INVISIBLE);
        rv.setVisibility(View.INVISIBLE);
    }

    private void instanciarRecursosInterfaz(@NonNull View v) {

        rv = v.findViewById(R.id.recycle_view_reports);
        rv.setHasFixedSize(true);

        LinearLayoutManager ly = new LinearLayoutManager(getContext());
        rv.setLayoutManager(ly);

        tv_reportes_vacios = v.findViewById(R.id.tv_reportes_vacios);
        tv_reportes_vacios.setVisibility(View.INVISIBLE);

        progressBar = v.findViewById(R.id.progress_bar_reportes);
        progressBar.setVisibility(View.VISIBLE);

        //Set adapter
        reportAdapter = new ReportAdapter(reportsViewModel.showReports().getValue(), requireContext());
        rv.setAdapter(reportAdapter);
    }
}
