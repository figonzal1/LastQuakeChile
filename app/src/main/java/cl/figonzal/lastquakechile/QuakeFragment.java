package cl.figonzal.lastquakechile;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;

import com.crashlytics.android.Crashlytics;

import java.util.List;
import java.util.Objects;


public class QuakeFragment extends Fragment implements SearchView.OnQueryTextListener {

    private RecyclerView rv;
    private ProgressBar progressBar;
    private QuakeViewModel viewModel;
    private QuakeAdapter adapter;
    private CardView cv_info;

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
        setHasOptionsMenu(true);

        //Setear el recycle view
        rv = v.findViewById(R.id.recycle_view);
        rv.setHasFixedSize(true);

        //Setear el layout de la lista
        LinearLayoutManager ly = new LinearLayoutManager(getContext());
        rv.setLayoutManager(ly);

        //Definicion de materiales a usar para checkear internet UI
        rv = v.findViewById(R.id.recycle_view);
        progressBar = v.findViewById(R.id.progress_bar_fragment);
        progressBar.setVisibility(View.VISIBLE);

        //Instanciar viewmodel
        viewModel = ViewModelProviders.of(Objects.requireNonNull(getActivity())).get(QuakeViewModel.class);


        /*
            Checkear los nuevos datos mediante un observable
         */
        viewModel.getMutableQuakeList().observe(this, new Observer<List<QuakeModel>>() {
            @Override
            public void onChanged(@Nullable List<QuakeModel> quakeModelList) {

                //Setear el adapter con la lista de quakes
                adapter = new QuakeAdapter(quakeModelList, getContext(), getActivity());
                adapter.notifyDataSetChanged();
                rv.setAdapter(adapter);

                //Progressbar desaparece despues de la descarga de datos
                progressBar.setVisibility(View.INVISIBLE);

                //LOG ZONE
                Log.d(getString(R.string.TAG_PROGRESS_FROM_FRAGMENT), getString(R.string.TAG_PROGRESS_FROM_FRAGMENT_UPDATE_RESPONSE));

                Crashlytics.log(Log.DEBUG, getString(R.string.TAG_PROGRESS_FROM_FRAGMENT), getString(R.string.TAG_PROGRESS_FROM_FRAGMENT_UPDATE_RESPONSE));
            }
        });

        /*
            Viewmodel encargado de mostrar mensajes de errores desde Volley (LoadDATA)
         */
        viewModel.getStatusData().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String status) {
                if (status != null) {

                    progressBar.setVisibility(View.INVISIBLE);
                    if (status.equals(getString(R.string.VIEWMODEL_ERROR_SERVER))) {
                        Snackbar
                                .make(v, status, Snackbar.LENGTH_INDEFINITE)
                                .setAction(getString(R.string.FLAG_RETRY), new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        viewModel.refreshMutableQuakeList();
                                        progressBar.setVisibility(View.VISIBLE);

                                        Crashlytics.setBool(getString(R.string.SNACKBAR_ERROR_SERVER_PRESSED), true);
                                    }
                                })
                                .show();

                        Log.d(getString(R.string.TAG_PROGRESS_FROM_FRAGMENT), getString(R.string.TAG_PROGRESS_FROM_FRAGMENT_ERROR_SERVER));
                        Crashlytics.log(Log.DEBUG, getString(R.string.TAG_PROGRESS_FROM_FRAGMENT), getString(R.string.TAG_PROGRESS_FROM_FRAGMENT_ERROR_SERVER));
                    } else if (status.equals(getString(R.string.VIEWMODEL_NOCONNECTION_ERROR))) {
                        Snackbar
                                .make(v, status, Snackbar.LENGTH_INDEFINITE)
                                .setAction(getString(R.string.FLAG_RETRY), new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        viewModel.refreshMutableQuakeList();
                                        progressBar.setVisibility(View.VISIBLE);

                                        Crashlytics.setBool(getString(R.string.SNACKBAR_NOCONNECTION_SERVER_PRESSED), true);

                                    }
                                })
                                .show();

                        Log.d(getString(R.string.TAG_PROGRESS_FROM_FRAGMENT), getString(R.string.TAG_PROGRESS_FROM_FRAGMENT_NOCONNECTION));
                        Crashlytics.log(Log.DEBUG, getString(R.string.TAG_PROGRESS_FROM_FRAGMENT), getString(R.string.TAG_PROGRESS_FROM_FRAGMENT_NOCONNECTION));
                    }

                }
            }
        });

        /*
            Seccion SHARED PREF
         */

        cv_info = v.findViewById(R.id.card_view_info);
        final SharedPreferences sharedPreferences = Objects.requireNonNull(getActivity()).getPreferences(Context.MODE_PRIVATE);
        String cv_visto = sharedPreferences.getString(getString(R.string.shared_pref_status_card_view), null);

        if (cv_visto != null && cv_visto.equals(getString(R.string.shared_pref_status_result_card_view))) {

            cv_info.setVisibility(View.GONE);
        }


        Button btn_cv_info = v.findViewById(R.id.btn_info_accept);
        btn_cv_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(getString(R.string.shared_pref_status_card_view), getString(R.string.shared_pref_status_result_card_view));
                editor.apply();

                cv_info.animate()
                        .translationY(-cv_info.getHeight())
                        .alpha(1.0f)
                        .setDuration(500)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                cv_info.setVisibility(View.GONE);
                            }
                        });


                //LOGS
                Log.d(getString(R.string.shared_card_view_info), getString(R.string.shared_pref_status_result_card_view));
                Crashlytics.log(Log.DEBUG, getString(R.string.shared_card_view_info), getString(R.string.shared_pref_status_result_card_view));
            }
        });
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
    public boolean onQueryTextSubmit(String s) {
        //List<QuakeModel> filteredList= viewModel.doSearch(s);
        //viewModel.setFilteredList(filteredList);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        String input = s.toLowerCase();
        List<QuakeModel> filteredList = viewModel.doSearch(input);
        viewModel.setFilteredList(filteredList);
        return true;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(this);

        super.onCreateOptionsMenu(menu, inflater);
    }
}
