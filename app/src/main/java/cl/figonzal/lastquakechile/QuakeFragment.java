package cl.figonzal.lastquakechile;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import java.util.List;


public class QuakeFragment extends Fragment {

    public RecyclerView rv;
    public ProgressBar progressBar;

    public QuakeFragment() {
        // Required empty public constructor
    }

    public static QuakeFragment newInstance() {
        return new QuakeFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View v = inflater.inflate(R.layout.fragment_quake, container, false);

        //Setear el recycle view
        rv = v.findViewById(R.id.recycle_view);
        rv.setHasFixedSize(true);

        //Setear el layout de la lista
        LinearLayoutManager ly = new LinearLayoutManager(getContext());
        rv.setLayoutManager(ly);

        //Definicion de materiales a usar para checkear internet UI
        rv = v.findViewById(R.id.recycle_view);
        progressBar = v.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);
        final QuakeViewModel viewModel = ViewModelProviders.of(this).get(QuakeViewModel.class);

        /*
         * No funciona el retry desde fragment
         */
        if (QuakeUtils.checkInternet(getContext())) {
            getData(viewModel);
        } else {
            showSnackBar(viewModel, "Retry");
            progressBar.setVisibility(View.INVISIBLE);
        }

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    private void getData(QuakeViewModel viewModel) {
        viewModel.getQuakeList().observe(this, new Observer<List<QuakeModel>>() {
            @Override
            public void onChanged(@Nullable List<QuakeModel> quakeModelList) {

                //Setear el adapter con la lista de quakes
                QuakeAdapter adapter = new QuakeAdapter(quakeModelList, getContext());
                adapter.notifyDataSetChanged();
                rv.setAdapter(adapter);
                progressBar.setVisibility(View.INVISIBLE);

                Log.d("PROGRESS_FRAGMENT", "UPDATE INFORMATION - FRAGMENT");

            }
        });
    }

    private void showSnackBar(final QuakeViewModel viewModel, String tipo) {

        if (tipo.equals("Retry")) {
            Snackbar
                    .make(getActivity().getWindow().getDecorView().getRootView(), "Sin conexion a internet", Snackbar.LENGTH_INDEFINITE)
                    .setAction("Retry", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            if (QuakeUtils.checkInternet(getContext())) {
                                getData(viewModel);
                                showSnackBar(viewModel, "Update");
                            } else {
                                showSnackBar(viewModel, "Retry");
                            }
                        }
                    })
                    .show();
        } else if (tipo.equals("Update")) {
            Snackbar
                    .make(getActivity().getWindow().getDecorView().getRootView(), "Sismos Actualizados", Snackbar.LENGTH_LONG)
                    .show();
        }

    }

}
