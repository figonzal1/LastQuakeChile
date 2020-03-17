package cl.figonzal.lastquakechile.views.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import cl.figonzal.lastquakechile.QuakeAdapter;
import cl.figonzal.lastquakechile.R;
import cl.figonzal.lastquakechile.model.QuakeModel;
import cl.figonzal.lastquakechile.services.AdsService;
import cl.figonzal.lastquakechile.viewmodel.QuakeListViewModel;


public class QuakeFragment extends Fragment implements SearchView.OnQueryTextListener {

    private Snackbar sSnackbar;
    private RecyclerView mRecycleView;
    private ProgressBar mProgressBar;
    private QuakeListViewModel mViewModel;
    private QuakeAdapter quakeAdapter;
    private CardView mCardViewInfo;
    private SharedPreferences sharedPreferences;

    private List<QuakeModel> quakeModelList;

    private AdView mAdView;
    private TextView tv_quakes_vacio;

    public QuakeFragment() {

    }

    public static QuakeFragment newInstance() {
        return new QuakeFragment();
    }

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {

        setHasOptionsMenu(true);

        // Inflate the layout for thi{s fragment
        final View v = inflater.inflate(R.layout.fragment_quake, container, false);

        instanciarRecursosInterfaz(v);

        iniciarViewModelObservers(v);

        //Seccion SHARED PREF CARD VIEW INFO
        showCardViewInformation(v);

        return v;
    }

    private void instanciarRecursosInterfaz(View v) {

        quakeModelList = new ArrayList<>();

        mCardViewInfo = v.findViewById(R.id.card_view_info);

        mAdView = v.findViewById(R.id.adView);

        AdsService adsService = new AdsService(requireContext(), getParentFragmentManager());
        adsService.configurarIntersitial(mAdView);

        mRecycleView = v.findViewById(R.id.recycle_view);

        LinearLayoutManager ly = new LinearLayoutManager(getContext());
        mRecycleView.setLayoutManager(ly);

        //Setear el layout de la lista
        /*LinearLayoutManager ly = new WrapContentLinearLayoutManager(getContext());
        mRecycleView.setLayoutManager(ly);*/

        tv_quakes_vacio = v.findViewById(R.id.tv_quakes_vacios);
        tv_quakes_vacio.setVisibility(View.INVISIBLE);

        mProgressBar = v.findViewById(R.id.progress_bar_quakes);
        mProgressBar.setVisibility(View.VISIBLE);

        //Instanciar viewmodel
        mViewModel = new ViewModelProvider(requireActivity()).get(QuakeListViewModel.class);

        quakeAdapter = new QuakeAdapter(
                quakeModelList,
                requireContext(),
                requireActivity()
        );
        mRecycleView.setAdapter(quakeAdapter);
    }


    /**
     * Funcion que contiene los ViewModels encargados de cargar los datos asincronamente a la UI
     *
     * @param v Vista necesaria para mostrar componentes UI
     */
    private void iniciarViewModelObservers(final View v) {

        mViewModel.isLoading().observe(requireActivity(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {

                if (aBoolean) {
                    mProgressBar.setVisibility(View.VISIBLE);
                    tv_quakes_vacio.setVisibility(View.INVISIBLE);
                    tv_quakes_vacio.setVisibility(View.INVISIBLE);
                    mRecycleView.setVisibility(View.INVISIBLE);
                } else {
                    mProgressBar.setVisibility(View.INVISIBLE);
                    mRecycleView.setVisibility(View.VISIBLE);
                }
            }
        });

        //Viewmodel encargado de cargar los datos desde internet
        mViewModel.showQuakeList().observe(requireActivity(), new Observer<List<QuakeModel>>() {
            @Override
            public void onChanged(@Nullable List<QuakeModel> list) {

                if (list != null) {
                    quakeModelList = list;
                    quakeAdapter.actualizarLista(quakeModelList);

                    quakeModelList = quakeAdapter.getQuakeList();

                    mProgressBar.setVisibility(View.INVISIBLE);
                    if (quakeModelList.size() == 0) {
                        tv_quakes_vacio.setVisibility(View.VISIBLE);
                    } else {
                        tv_quakes_vacio.setVisibility(View.INVISIBLE);
                    }


                    //LOG ZONE
                    Log.d(getString(R.string.TAG_PROGRESS_FROM_FRAGMENT),
                            getString(R.string.TAG_PROGRESS_FROM_FRAGMENT_UPDATE_RESPONSE));

                    Crashlytics.log(Log.DEBUG, getString(R.string.TAG_PROGRESS_FROM_FRAGMENT),
                            getString(R.string.TAG_PROGRESS_FROM_FRAGMENT_UPDATE_RESPONSE));
                }
            }
        });

        //Viewmodel encargado de mostrar los mensajes de estado en los sSnackbar
        mViewModel.showResponseErrorList().observe(requireActivity(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String status) {
                if (status != null) {
                    mProgressBar.setVisibility(View.INVISIBLE);
                    mRecycleView.setVisibility(View.INVISIBLE);
                    showSnackBar(status, v);
                    quakeAdapter.notifyDataSetChanged();
                }
            }
        });

        //ViewModel encargado de cargar los datos de sismos post-busqueda de usuario en SearchView
        mViewModel.showFilteredQuakeList().observe(requireActivity(), new Observer<List<QuakeModel>>() {
            @Override
            public void onChanged(@Nullable List<QuakeModel> list) {
                //Setear el mAdapter con la lista de quakes

                quakeModelList = list;
                quakeAdapter.actualizarLista(quakeModelList);
            }
        });
    }

    /**
     * Funcion encargada de moestrar un cardview de aviso al usuario sobre el listado de 15 sismos.
     *
     * @param v Vista necesaria para mostrar el vardview
     */
    private void showCardViewInformation(View v) {

        final SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(requireActivity().getString(R.string.MAIN_SHARED_PREF_KEY), Context.MODE_PRIVATE);
        boolean isCardViewShow = sharedPreferences.getBoolean(getString(R.string.SHARED_PREF_STATUS_CARD_VIEW_INFO), true);

        if (isCardViewShow) {
            mCardViewInfo.setVisibility(View.VISIBLE);
        } else {
            mCardViewInfo.setVisibility(View.GONE);
        }


        Button mBtnCvInfo = v.findViewById(R.id.btn_info_accept);
        mBtnCvInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(getString(R.string.SHARED_PREF_STATUS_CARD_VIEW_INFO), false);
                editor.apply();

                mCardViewInfo.animate()
                        .translationY(-mCardViewInfo.getHeight())
                        .alpha(1.0f)
                        .setDuration(500)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                mCardViewInfo.setVisibility(View.GONE);
                            }
                        });


                //LOGS
                Log.d(getString(R.string.TAG_CARD_VIEW_INFO),
                        getString(R.string.SHARED_PREF_STATUS_CARD_VIEW_INFO_RESULT));
                Crashlytics.log(Log.DEBUG, getString(R.string.TAG_CARD_VIEW_INFO),
                        getString(R.string.SHARED_PREF_STATUS_CARD_VIEW_INFO_RESULT));
            }
        });
    }

    /**
     * Funcion encargada de mostrar el mensaje de sSnackbar de los mensajes de error de datos.
     *
     * @param status Estado del mensaje (Timeout,server error, etc)
     * @param v      (Vista necesaria para mostrar sSnackbar en coordinator layout)
     */
    private void showSnackBar(String status, View v) {

        //TIMEOUT ERROR
        if (status.equals(getString(R.string.VIEWMODEL_TIMEOUT_ERROR))) {
            sSnackbar = Snackbar.make(v, status, Snackbar.LENGTH_INDEFINITE)
                    .setAction(getString(R.string.FLAG_RETRY), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mViewModel.refreshMutableQuakeList();
                            mProgressBar.setVisibility(View.VISIBLE);

                            Crashlytics.setBool(getString(R.string.SNACKBAR_TIMEOUT_ERROR_PRESSED)
                                    , true);
                        }
                    });
            sSnackbar.setActionTextColor(getResources().getColor(R.color.colorSecondary, requireContext().getTheme()));
            int snackbarTextId = com.google.android.material.R.id.snackbar_text;
            TextView textView = sSnackbar.getView().findViewById(snackbarTextId);
            textView.setTextColor(getResources().getColor(R.color.white, requireContext().getTheme()));
            sSnackbar.show();

            Log.d(getString(R.string.TAG_PROGRESS_FROM_FRAGMENT),
                    getString(R.string.TAG_PROGRESS_FROM_FRAGMENT_TIMEOUT_ERROR));
            Crashlytics.log(Log.DEBUG, getString(R.string.TAG_PROGRESS_FROM_FRAGMENT),
                    getString(R.string.TAG_PROGRESS_FROM_FRAGMENT_TIMEOUT_ERROR));
        }

        //SERVER ERROR
        else if (status.equals(getString(R.string.VIEWMODEL_SERVER_ERROR))) {
            sSnackbar = Snackbar.make(v, status, Snackbar.LENGTH_INDEFINITE)
                    .setAction(getString(R.string.FLAG_RETRY), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            mViewModel.refreshMutableQuakeList();
                            mProgressBar.setVisibility(View.VISIBLE);

                            Crashlytics.setBool(getString(R.string.SNACKBAR_SERVER_ERROR_PRESSED),
                                    true);
                        }
                    });
            sSnackbar.setActionTextColor(getResources().getColor(R.color.colorSecondary, requireContext().getTheme()));
            int snackbarTextId = com.google.android.material.R.id.snackbar_text;
            TextView textView = sSnackbar.getView().findViewById(snackbarTextId);
            textView.setTextColor(getResources().getColor(R.color.white, requireContext().getTheme()));
            sSnackbar.show();

            Log.d(getString(R.string.TAG_PROGRESS_FROM_FRAGMENT),
                    getString(R.string.TAG_PROGRESS_FROM_FRAGMENT_SERVER_ERROR));
            Crashlytics.log(Log.DEBUG, getString(R.string.TAG_PROGRESS_FROM_FRAGMENT),
                    getString(R.string.TAG_PROGRESS_FROM_FRAGMENT_SERVER_ERROR));

        }

        //NOCONNECTION ERROR
        else if (status.equals(getString(R.string.VIEWMODEL_NOCONNECTION_ERROR))) {
            sSnackbar = Snackbar
                    .make(v, status, Snackbar.LENGTH_INDEFINITE)
                    .setAction(getString(R.string.FLAG_RETRY), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mViewModel.refreshMutableQuakeList();
                            mProgressBar.setVisibility(View.VISIBLE);

                            Crashlytics.setBool(getString(R.string.SNACKBAR_NOCONNECTION_ERROR_PRESSED), true);
                        }
                    });
            sSnackbar.setActionTextColor(getResources().getColor(R.color.colorSecondary, requireContext().getTheme()));
            int snackbarTextId = com.google.android.material.R.id.snackbar_text;
            TextView textView = sSnackbar.getView().findViewById(snackbarTextId);
            textView.setTextColor(getResources().getColor(R.color.white, requireContext().getTheme()));
            sSnackbar.show();

            Log.d(getString(R.string.TAG_PROGRESS_FROM_FRAGMENT),
                    getString(R.string.TAG_PROGRESS_FROM_FRAGMENT_NOCONNECTION));
            Crashlytics.log(Log.DEBUG, getString(R.string.TAG_PROGRESS_FROM_FRAGMENT),
                    getString(R.string.TAG_PROGRESS_FROM_FRAGMENT_NOCONNECTION));
        }
    }


    @Override
    public void onResume() {
        mAdView.resume();
        super.onResume();
    }

    @Override
    public void onPause() {
        mAdView.pause();
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        //List<QuakeModel> filteredList= mViewModel.doSearch(s);
        //mViewModel.setFilteredList(filteredList);
        return false;
    }

    /**
     * Funcion encargada de realizar la busqueda de sismos cada vez que el usuario ingresa un
     * caracter.
     *
     * @param s Caracter o palabra ingresada por el usuario.
     * @return Booleano
     */
    @Override
    public boolean onQueryTextChange(String s) {
        String input = s.toLowerCase();
        mViewModel.doSearch(input);
        return true;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull final Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();

        inflater.inflate(R.menu.fragment_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.search_menu);

        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setOnQueryTextListener(this);

        MenuItem.OnActionExpandListener onActionExpandListener =
                new MenuItem.OnActionExpandListener() {
                    @Override
                    public boolean onMenuItemActionExpand(MenuItem item) {

                        //Se oculta boton refresh
                        menu.findItem(R.id.refresh_menu).setVisible(false);
                        return true;
                    }

                    @Override
                    public boolean onMenuItemActionCollapse(MenuItem item) {

                        mViewModel.refreshMutableQuakeList();
                        //Se vuelve a mostrar boton refresh
                        menu.findItem(R.id.refresh_menu).setVisible(true);
                        requireActivity().invalidateOptionsMenu();
                        return true;
                    }
                };
        menuItem.setOnActionExpandListener(onActionExpandListener);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.refresh_menu) {//Se refresca el listado de sismos
            mViewModel.refreshMutableQuakeList();

            //Si el sSnackbar de estado de datos esta ON y el usuario presiona refresh desde
            // toolbar
            //el sSnackbar se oculta
            if (sSnackbar != null && sSnackbar.isShown()) {
                sSnackbar.dismiss();
            }

            return true;
        }
        return false;
    }


    /**
     * Funcion encargada de manejar el envio de la invitacion
     *
     * @param requestCode Fija el resultado de la operacion desde la activity
     * @param resultCode  Entrega el resultado del intent
     * @param data        parametro que entrega la informacion de los contactos
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d("INTENT",
                "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);

        if (requestCode == 0) {
            if (resultCode == Activity.RESULT_OK) {
                // Get the invitation IDs of all sent messages
                String[] mIds = AppInviteInvitation.getInvitationIds(resultCode, data);
                for (String id : mIds) {
                    Log.d("INTENT", "Enviando invitacion a" + id);
                }
            } else {
                Log.d("INTENT", "Inviacion cancelada");
                Toast.makeText(requireContext(), "Invitación cancelada", Toast.LENGTH_LONG).show();
            }
        }
    }

}


