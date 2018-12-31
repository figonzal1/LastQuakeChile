package cl.figonzal.lastquakechile;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
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
import java.util.Objects;


public class QuakeFragment extends Fragment implements ResponseNetworkHandler {

    public RecyclerView rv;
    public ProgressBar progressBar;
    public QuakeViewModel viewModel;

    public QuakeFragment() {

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
        viewModel = ViewModelProviders.of(Objects.requireNonNull(getActivity())).get(QuakeViewModel.class);

        /*
            Viewmodel encargado de mostrar mensajes de errores desde Volley (LoadDATA)
         */
        viewModel.getStatusData().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String status) {
                if (status != null) {

                    progressBar.setVisibility(View.INVISIBLE);
                    Snackbar
                            .make(v, status, Snackbar.LENGTH_INDEFINITE)
                            .setAction("Recargar", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    viewModel.refreshQuakeList();
                                    progressBar.setVisibility(View.VISIBLE);
                                }
                            })
                            .show();
                    Log.d(getString(R.string.TAG_PROGRESS_FROM_FRAGMENT), getString(R.string.TAG_PROGRESS_FROM_FRAGMENT_ERROR_SERVER));
                }

            }
        });

        /*
            Flujo de informacion dependiendo de la conexion a internet
         */
        if (QuakeUtils.checkInternet(Objects.requireNonNull(getContext()))) {
            getData();
        } else {
            //Mostrar Snackbar de retry de datos
            showSnackBar(getActivity(), getString(R.string.FLAG_RETRY));
            progressBar.setVisibility(View.INVISIBLE);

            //LOG ZONE
            Log.d(getString(R.string.TAG_PROGRESS_FROM_FRAGMENT), getString(R.string.TAG_PROGRESS_FROM_FRAGMENT_RETRY_RESPONSE));
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


    @Override
    public void getData() {

        viewModel.getQuakeList().observe(this, new Observer<List<QuakeModel>>() {
            @Override
            public void onChanged(@Nullable List<QuakeModel> quakeModelList) {

                //Setear el adapter con la lista de quakes
                QuakeAdapter adapter = new QuakeAdapter(quakeModelList, getContext());
                adapter.notifyDataSetChanged();
                rv.setAdapter(adapter);

                //Progressbar desaparece despues de la descarga de datos
                progressBar.setVisibility(View.INVISIBLE);

                //Mostrar snackbar de sismos actualizados
                //showSnackBar(getActivity(), getString(R.string.FLAG_UPDATE));

                //LOG ZONE
                Log.d(getString(R.string.TAG_PROGRESS_FROM_FRAGMENT), getString(R.string.TAG_PROGRESS_FROM_FRAGMENT_UPDATE_RESPONSE));

            }


        });
    }

    @Override
    public void showSnackBar(Context context, String tipo) {

        if (tipo.equals(getString(R.string.FLAG_RETRY))) {
            Snackbar
                    .make(Objects.requireNonNull(getActivity()).findViewById(R.id.main_container), getString(R.string.SNACKBAR_STATUS_MESSAGE_NOCONNECTION), Snackbar.LENGTH_INDEFINITE)
                    .setAction(getString(R.string.FLAG_RETRY), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            if (QuakeUtils.checkInternet(Objects.requireNonNull(getContext()))) {
                                progressBar.setVisibility(View.VISIBLE);
                                getData();
                            } else {
                                showSnackBar(getActivity(), getString(R.string.FLAG_RETRY));
                            }
                        }
                    })
                    .show();
        } else if (tipo.equals(getString(R.string.FLAG_UPDATE))) {
            Snackbar
                    .make(Objects.requireNonNull(getActivity()).findViewById(R.id.main_container), getString(R.string.SNACKBAR_STATUS_MESSAGE_UPDATE), Snackbar.LENGTH_LONG)
                    .show();
        }
    }
}
