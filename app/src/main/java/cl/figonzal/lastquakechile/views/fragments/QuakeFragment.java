package cl.figonzal.lastquakechile.views.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Application;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.util.ArrayList;
import java.util.List;

import cl.figonzal.lastquakechile.R;
import cl.figonzal.lastquakechile.adapter.QuakeAdapter;
import cl.figonzal.lastquakechile.handlers.DateHandler;
import cl.figonzal.lastquakechile.handlers.ViewsManager;
import cl.figonzal.lastquakechile.model.QuakeModel;
import cl.figonzal.lastquakechile.repository.NetworkRepository;
import cl.figonzal.lastquakechile.repository.QuakeRepository;
import cl.figonzal.lastquakechile.services.AdsService;
import cl.figonzal.lastquakechile.services.SharedPrefService;
import cl.figonzal.lastquakechile.viewmodel.QuakeListViewModel;
import cl.figonzal.lastquakechile.viewmodel.ViewModelFactory;
import timber.log.Timber;


public class QuakeFragment extends Fragment implements SearchView.OnQueryTextListener {

    private Snackbar sSnackbar;
    private RecyclerView mRecycleView;
    private ProgressBar mProgressBar;
    private QuakeListViewModel mViewModel;
    private QuakeAdapter quakeAdapter;
    private CardView mCardViewInfo;

    private List<QuakeModel> quakeModelList;

    private AdView mAdView;
    private TextView tv_quakes_vacio;

    private Application application;

    private FirebaseCrashlytics crashlytics;
    private DateHandler dateHandler;
    private ViewsManager viewsManager;
    private SharedPrefService sharedPrefService;

    public QuakeFragment() {
    }

    @NonNull
    public static QuakeFragment newInstance() {
        return new QuakeFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        application = requireActivity().getApplication();

        dateHandler = new DateHandler();
        crashlytics = FirebaseCrashlytics.getInstance();
        viewsManager = new ViewsManager();

        sharedPrefService = new SharedPrefService(getContext());
    }

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {

        setHasOptionsMenu(true);

        // Inflate the layout for thi{s fragment
        final View v = inflater.inflate(R.layout.fragment_quake, container, false);

        instanciarRecursosInterfaz(v);

        iniciarViewModelObservers();

        //Seccion SHARED PREF CARD VIEW INFO
        showCardViewInformation(v);

        return v;
    }

    private void instanciarRecursosInterfaz(@NonNull View v) {

        mCardViewInfo = v.findViewById(R.id.card_view_info);

        mAdView = v.findViewById(R.id.adView);

        AdsService adsService = new AdsService(requireActivity(), requireContext(), dateHandler);
        adsService.loadBanner(mAdView);

        mRecycleView = v.findViewById(R.id.recycle_view_quakes);
        mRecycleView.setHasFixedSize(true);

        LinearLayoutManager ly = new LinearLayoutManager(getContext());
        mRecycleView.setLayoutManager(ly);

        tv_quakes_vacio = v.findViewById(R.id.tv_quakes_vacios);
        tv_quakes_vacio.setVisibility(View.INVISIBLE);

        mProgressBar = v.findViewById(R.id.progress_bar_quakes);
        mProgressBar.setVisibility(View.VISIBLE);

        quakeAdapter = new QuakeAdapter(
                new ArrayList<>(),
                requireActivity(),
                dateHandler,
                viewsManager
        );

        mRecycleView.setAdapter(quakeAdapter);
    }


    /**
     * Funcion que contiene los ViewModels encargados de cargar los datos asincronamente a la UI
     */
    private void iniciarViewModelObservers() {

        //Instanciar viewmodel
        NetworkRepository<QuakeModel> repository = QuakeRepository.getIntance(application.getApplicationContext());
        mViewModel = new ViewModelProvider(requireActivity(), new ViewModelFactory(application, repository)).get(QuakeListViewModel.class);

        mViewModel.isLoading().observe(requireActivity(), aBoolean -> {
            if (aBoolean) {
                showProgressBar();
            } else {
                hideProgressBar();

                if (quakeAdapter.getItemCount() == 0) {
                    tv_quakes_vacio.setVisibility(View.VISIBLE);
                } else {
                    tv_quakes_vacio.setVisibility(View.INVISIBLE);
                }
            }
        });

        //Viewmodel encargado de cargar los datos desde internet
        mViewModel.showQuakeList().observe(requireActivity(), list -> {

            quakeAdapter.updateList(list);
            quakeAdapter.notifyDataSetChanged();

            if (quakeAdapter.getItemCount() == 0) {
                tv_quakes_vacio.setVisibility(View.VISIBLE);
            } else {
                tv_quakes_vacio.setVisibility(View.INVISIBLE);
            }
            //LOG ZONE
            Timber.i(getString(R.string.TAG_FRAGMENT_QUAKE) + ": " + getString(R.string.FRAGMENT_LOAD_LIST));

        });

        //Viewmodel encargado de mostrar los mensajes de estado en los sSnackbar
        mViewModel.showMsgErrorList().observe(requireActivity(), status -> {
            showSnackBar(status, requireActivity().findViewById(android.R.id.content));
            quakeAdapter.notifyDataSetChanged();
        });

        //ViewModel encargado de cargar los datos de sismos post-busqueda de usuario en SearchView
        mViewModel.showFilteredQuakeList().observe(requireActivity(), list -> {

            //Setear el mAdapter con la lista de quakes
            quakeModelList = list;
            quakeAdapter.updateList(quakeModelList);
        });
    }

    private void hideProgressBar() {
        mProgressBar.setVisibility(View.INVISIBLE);
        mRecycleView.setVisibility(View.VISIBLE);
    }

    private void showProgressBar() {
        mProgressBar.setVisibility(View.VISIBLE);
        tv_quakes_vacio.setVisibility(View.INVISIBLE);
        mRecycleView.setVisibility(View.INVISIBLE);
    }

    /**
     * Funcion encargada de moestrar un cardview de aviso al usuario sobre el listado de 15 sismos.
     *
     * @param v Vista necesaria para mostrar el vardview
     */
    private void showCardViewInformation(@NonNull View v) {

        boolean isCardViewShow = (boolean) sharedPrefService.getData(getString(R.string.SHARED_PREF_STATUS_CARD_VIEW_INFO), true);

        if (isCardViewShow) {
            mCardViewInfo.setVisibility(View.VISIBLE);
        } else {
            mCardViewInfo.setVisibility(View.GONE);
        }

        Button mBtnCvInfo = v.findViewById(R.id.btn_info_accept);

        mBtnCvInfo.setOnClickListener(v1 -> {

            sharedPrefService.saveData(getString(R.string.SHARED_PREF_STATUS_CARD_VIEW_INFO), false);

            mCardViewInfo.animate()
                    .translationY(-mCardViewInfo.getHeight())
                    .alpha(1.0f)
                    .setDuration(500)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mCardViewInfo.setVisibility(View.GONE);
                        }
                    });


            //LOGS
            Timber.i(getString(R.string.TAG_CARD_VIEW_INFO) + ": " + getString(R.string.SHARED_PREF_STATUS_CARD_VIEW_INFO_RESULT));
        });
    }

    /**
     * Funcion encargada de mostrar el mensaje de sSnackbar de los mensajes de error de datos.
     *
     * @param status Estado del mensaje (Timeout,server error, etc)
     * @param v      (Vista necesaria para mostrar sSnackbar en coordinator layout)
     */
    private void showSnackBar(@NonNull String status, @NonNull View v) {

        sSnackbar = Snackbar
                .make(v, status, Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(R.string.FLAG_RETRY), v1 -> {

                    mViewModel.refreshMutableQuakeList();

                    mProgressBar.setVisibility(View.VISIBLE);

                    crashlytics.setCustomKey(getString(R.string.SNACKBAR_NOCONNECTION_ERROR_PRESSED), true);
                });

        sSnackbar.setActionTextColor(getResources().getColor(R.color.colorSecondary, requireContext().getTheme()));

        int snackbarTextId = com.google.android.material.R.id.snackbar_text;

        TextView textView = sSnackbar.getView().findViewById(snackbarTextId);
        textView.setTextColor(getResources().getColor(R.color.white, requireContext().getTheme()));
        sSnackbar.show();

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
    public boolean onQueryTextChange(@NonNull String s) {
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

        if (item.getItemId() == R.id.refresh_menu) {

            //Se refresca el listado de sismos
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

}


