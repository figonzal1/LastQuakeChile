package cl.figonzal.lastquakechile.views.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
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
import cl.figonzal.lastquakechile.SettingsActivity;
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {


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

        AdsService adsService = new AdsService(getContext(), getParentFragmentManager());
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
        mViewModel = new ViewModelProvider(this).get(QuakeListViewModel.class);

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

                quakeModelList = list;
                quakeAdapter.actualizarLista(quakeModelList);

                quakeModelList = quakeAdapter.getQuakeList();

                if (quakeModelList.size() == 0) {
                    tv_quakes_vacio.setVisibility(View.VISIBLE);
                } else {
                    tv_quakes_vacio.setVisibility(View.INVISIBLE);
                }
                mProgressBar.setVisibility(View.INVISIBLE);

                //LOG ZONE
                Log.d(getString(R.string.TAG_PROGRESS_FROM_FRAGMENT),
                        getString(R.string.TAG_PROGRESS_FROM_FRAGMENT_UPDATE_RESPONSE));

                Crashlytics.log(Log.DEBUG, getString(R.string.TAG_PROGRESS_FROM_FRAGMENT),
                        getString(R.string.TAG_PROGRESS_FROM_FRAGMENT_UPDATE_RESPONSE));
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
        menu.clear();
        inflater.inflate(R.menu.toolbar_menu, menu);

        MenuItem mSearchItem = menu.findItem(R.id.search_menu);

        SearchView mSearchView = (SearchView) mSearchItem.getActionView();
        mSearchView.setOnQueryTextListener(this);

        /*
            Action expand utilizado para identificar cuando el usuario presiona el SEARCH VIEW
         */
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
                        return true;
                    }
                };
        mSearchItem.setOnActionExpandListener(onActionExpandListener);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        Intent intent;

        switch (item.getItemId()) {

            case R.id.settings_menu:
                intent = new Intent(getActivity(), SettingsActivity.class);
                startActivity(intent);
                return true;

            case R.id.refresh_menu:

                //Se refresca el listado de sismos
                mViewModel.refreshMutableQuakeList();

                //Si el sSnackbar de estado de datos esta ON y el usuario presiona refresh desde
                // toolbar
                //el sSnackbar se oculta
                if (sSnackbar != null && sSnackbar.isShown()) {
                    sSnackbar.dismiss();
                }

                return true;

            case R.id.invite_menu:

                //Intent de invitacion
                onInviteClicked();
                return true;
        }
        return true;
    }

    /**
     * Funcion encargada de configurar el intent para la invitacion.
     */
    private void onInviteClicked() {
        Intent mIntent =
                new AppInviteInvitation.IntentBuilder(getString(R.string.INVITATION_TITLE))
                        .setDeepLink(Uri.parse(getString(R.string.DEEP_LINK)))
                        .setEmailHtmlContent("<!DOCTYPE html>\n" +
                                "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n" +
                                "<head>\n" +
                                "  <meta http-equiv=\"Content-Type\" content=\"text/html; " +
                                "charset=utf-8\"" +
                                " " +
                                "/>\n" +
                                "  <meta name=\"viewport\" content=\"width=device-width\">\n" +
                                "  <meta name=\"HandheldFriendly\" content=\"true\" />\n" +
                                "  <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\" />\n" +
                                "  <!--[if gte IE 7]><html class=\"ie8plus\" xmlns=\"http://www" +
                                ".w3" +
                                ".org/1999/xhtml\"><![endif]-->\n" +
                                "  <!--[if IEMobile]><html class=\"ie8plus\" xmlns=\"http://www" +
                                ".w3" +
                                ".org/1999/xhtml\"><![endif]-->\n" +
                                "  <meta name=\"format-detection\" content=\"telephone=no\">\n" +
                                "  <meta name=\"generator\" content=\"EDMdesigner, www" +
                                ".edmdesigner" +
                                ".com\">\n" +
                                "  <title>LastQuakeChile_mail</title>\n" +
                                "\n" +
                                "  <link href=\"https://fonts.googleapis.com/css?family=Bitter\"" +
                                " " +
                                "rel=\"stylesheet\" type=\"text/css\">\n" +
                                "  <link href=\"https://fonts.googleapis.com/css?family=Roboto\"" +
                                " " +
                                "rel=\"stylesheet\" type=\"text/css\">\n" +
                                "\n" +
                                "  <style type=\"text/css\" media=\"screen\">\n" +
                                "  * {line-height: inherit;}\n" +
                                "  .ExternalClass * { line-height: 100%; }\n" +
                                "  body, p{margin:0; padding:0; margin-bottom:0; " +
                                "-webkit-text-size-adjust:none; -ms-text-size-adjust:none;} " +
                                "img{line-height:100%; outline:none; text-decoration:none; " +
                                "-ms-interpolation-mode: bicubic;} a img{border: none;} a, " +
                                "a:link," +
                                " " +
                                ".no-detect-local a, .appleLinks a{color:#5555ff !important; " +
                                "text-decoration: underline;} .ExternalClass {display: block " +
                                "!important;" +
                                " " +
                                "width:100%;} .ExternalClass, .ExternalClass p, .ExternalClass " +
                                "span, " +
                                ".ExternalClass font, .ExternalClass td, .ExternalClass div { " +
                                "line-height: inherit; } table td {border-collapse:collapse;" +
                                "mso-table-lspace: 0pt; mso-table-rspace: 0pt;} sup{position: " +
                                "relative; " +
                                "top: 4px; line-height:7px !important;font-size:11px !important;" +
                                "}" +
                                " " +
                                ".mobile_link a[href^=\"tel\"], .mobile_link a[href^=\"sms\"] " +
                                "{text-decoration: default; color: #5555ff !important;\n" +
                                "    pointer-events: auto; cursor: default;} .no-detect " +
                                "a{text-decoration:" +
                                " none; color: #5555ff; pointer-events: auto; cursor: default;} " +
                                "{color: " +
                                "#5555ff;} span {color: inherit; border-bottom: none;} " +
                                "span:hover" +
                                " " +
                                "{ " +
                                "background-color: transparent; }\n" +
                                "\n" +
                                "    .nounderline {text-decoration: none !important;}\n" +
                                "    h1, h2, h3 { margin:0; padding:0; }\n" +
                                "    p {Margin: 0px !important; }\n" +
                                "\n" +
                                "    table[class=\"email-root-wrapper\"] { width: 600px " +
                                "!important; }\n" +
                                "\n" +
                                "    body {\n" +
                                "      background-color: #707070;\n" +
                                "      background: #707070;\n" +
                                "    }\n" +
                                "    body { min-width: 280px; width: 100%;}\n" +
                                "    td[class=\"pattern\"] .c363p67r { width: 67" +
                                ".30769230769232%;" +
                                "}\n" +
                                "    td[class=\"pattern\"] .c176p32r { width: 32" +
                                ".69230769230768%;" +
                                "}\n" +
                                "    td[class=\"pattern\"] .c399p66r { width: 66" +
                                ".66666666666666%;" +
                                "}\n" +
                                "    td[class=\"pattern\"] .c200p33r { width: 33" +
                                ".33333333333334%;" +
                                "}\n" +
                                "\n" +
                                "  </style>\n" +
                                "  <style>\n" +
                                "  @media only screen and (max-width: 599px),\n" +
                                "  only screen and (max-device-width: 599px),\n" +
                                "  only screen and (max-width: 400px),\n" +
                                "  only screen and (max-device-width: 400px) {\n" +
                                "   .email-root-wrapper { width: 100% !important; }\n" +
                                "   .full-width { width: 100% !important; height: auto " +
                                "!important;" +
                                " " +
                                "text-align:center;}\n" +
                                "   .fullwidthhalfleft {width:100% !important;}\n" +
                                "   .fullwidthhalfright {width:100% !important;}\n" +
                                "   .fullwidthhalfinner {width:100% !important; margin: 0 auto " +
                                "!important;" +
                                " float: none !important; margin-left: auto !important; " +
                                "margin-right: " +
                                "auto !important; clear:both !important; }\n" +
                                "   .hide { display:none !important; width:0px !important;" +
                                "height:0px " +
                                "!important; overflow:hidden; }\n" +
                                "   .desktop-hide { display:block !important; width:100% " +
                                "!important;" +
                                "height:auto !important; overflow:hidden; max-height: inherit " +
                                "!important;" +
                                " }\n" +
                                "   .c363p67r { width: 100% !important; float:none;}\n" +
                                "   .c176p32r { width: 100% !important; float:none;}\n" +
                                "   .c399p66r { width: 100% !important; float:none;}\n" +
                                "   .c200p33r { width: 100% !important; float:none;}\n" +
                                "\n" +
                                " }\n" +
                                "</style>\n" +
                                "<style>\n" +
                                "@media only screen and (min-width: 600px) {\n" +
                                "  td[class=\"pattern\"] .c363p67r { width: 363px !important;}\n" +
                                "  td[class=\"pattern\"] .c176p32r { width: 176px !important;}\n" +
                                "  td[class=\"pattern\"] .c399p66r { width: 399px !important;}\n" +
                                "  td[class=\"pattern\"] .c200p33r { width: 200px !important;}\n" +
                                "\n" +
                                "}\n" +
                                "@media only screen and (max-width: 599px),\n" +
                                "only screen and (max-device-width: 599px),\n" +
                                "only screen and (max-width: 400px),\n" +
                                "only screen and (max-device-width: 400px) {\n" +
                                "  table[class=\"email-root-wrapper\"] { width: 100% !important;" +
                                " " +
                                "}\n" +
                                "  td[class=\"wrap\"] .full-width { width: 100% !important; " +
                                "height: auto" +
                                " " +
                                "!important;}\n" +
                                "\n" +
                                "  td[class=\"wrap\"] .fullwidthhalfleft {width:100% !important;" +
                                "}\n" +
                                "  td[class=\"wrap\"] .fullwidthhalfright {width:100% " +
                                "!important;" +
                                "}\n" +
                                "  td[class=\"wrap\"] .fullwidthhalfinner {width:100% " +
                                "!important;" +
                                " " +
                                "margin:" +
                                " " +
                                "0 auto !important; float: none !important; margin-left: auto " +
                                "!important;" +
                                " margin-right: auto !important; clear:both !important; }\n" +
                                "  td[class=\"wrap\"] .hide { display:none !important; " +
                                "width:0px;" +
                                "height:0px; overflow:hidden; }\n" +
                                "\n" +
                                "  td[class=\"pattern\"] .c363p67r { width: 100% !important; }\n" +
                                "  td[class=\"pattern\"] .c176p32r { width: 100% !important; }\n" +
                                "  td[class=\"pattern\"] .c399p66r { width: 100% !important; }\n" +
                                "  td[class=\"pattern\"] .c200p33r { width: 100% !important; }\n" +
                                "\n" +
                                "}\n" +
                                "\n" +
                                "\n" +
                                "</style>\n" +
                                "<STYLE type=\"text/css\" media=\"(pointer) and " +
                                "(min-color-index:0)\">\n" +
                                "html, body {background-image: none !important; " +
                                "background-color:" +
                                " " +
                                "transparent !important; margin:0 !important; padding:0 " +
                                "!important;}\n" +
                                "</STYLE>\n" +
                                "\n" +
                                "</head>\n" +
                                "<body leftmargin=\"0\" marginwidth=\"0\" topmargin=\"0\" " +
                                "marginheight=\"0\" offset=\"0\" style=\"font-family:Arial, " +
                                "sans-serif; " +
                                "font-size:0px;margin:0;padding:0;background: #707070 " +
                                "!important;" +
                                "\" " +
                                "bgcolor=\"#707070\">\n" +
                                "  <!--[if t]><![endif]--><!--[if t]><![endif]--><!--[if " +
                                "t]><![endif]--><!--[if t]><![endif]--><!--[if " +
                                "t]><![endif]--><!--[if " +
                                "t]><![endif]-->\n" +
                                "  <table align=\"center\" border=\"0\" cellpadding=\"0\" " +
                                "cellspacing=\"0\" height=\"100%\" width=\"100%\"  " +
                                "bgcolor=\"#707070\" " +
                                "style=\"margin:0; padding:0; width:100% !important; background:" +
                                " " +
                                "#707070" +
                                " " +
                                "!important;\">\n" +
                                "    <tr>\n" +
                                "      <td class=\"wrap\" align=\"center\" valign=\"top\" " +
                                "width=\"100%\">\n" +
                                "        <center>\n" +
                                "          <!-- content -->\n" +
                                "          <div  style=\"padding:0px\"><table cellpadding=\"0\" " +
                                "cellspacing=\"0\" border=\"0\" width=\"100%\" " +
                                "bgcolor=\"#FFFFFF\"><tr><td valign=\"top\"  " +
                                "style=\"padding:0px\"><table" +
                                " cellpadding=\"0\" cellspacing=\"0\" width=\"600\" " +
                                "align=\"center\"  " +
                                "style=\"max-width:600px;min-width:240px;margin:0 auto\" " +
                                "class=\"email-root-wrapper\"><tr><td valign=\"top\"  " +
                                "style=\"padding:0px\"><table cellpadding=\"0\" " +
                                "cellspacing=\"0\"" +
                                " " +
                                "border=\"0\" width=\"100%\" bgcolor=\"#303f9f\"  " +
                                "style=\"border:0px " +
                                "none;background-color:#303f9f\"><tr><td valign=\"top\"  " +
                                "style=\"padding:0px\"><table cellpadding=\"0\" " +
                                "cellspacing=\"0\"" +
                                " " +
                                "width=\"100%\"><tr><td  style=\"padding:0px\"><table " +
                                "cellpadding=\"0\" " +
                                "cellspacing=\"0\" border=\"0\" width=\"100%\"><tr><td " +
                                "valign=\"top\"  " +
                                "style=\"padding-top:10px;padding-right:30px;" +
                                "padding-bottom:10px;" +
                                "padding-left:30px\"><div \n" +
                                "            style=\"text-align:left;font-family:Arial;" +
                                "font-size:14px;" +
                                "color:#FFFFFF;line-height:22px;mso-line-height:exactly;" +
                                "mso-text-raise:4px\"><h1 style=\"font-family:Roboto, Helvetica " +
                                "Neue, " +
                                "Helvetica, Arial, sans-serif; font-size: 28px; color: #FFFFFF; " +
                                "line-height: 40px; mso-line-height: exactly; mso-text-raise: " +
                                "6px;" +
                                " " +
                                "padding: 0; margin: 0;\"><span " +
                                "class=\"mso-font-fix-arial\">Last" +
                                " " +
                                "Quake " +
                                "Chile</span></h1></div></td>\n" +
                                "          </tr>\n" +
                                "        </table>\n" +
                                "      </td>\n" +
                                "    </tr>\n" +
                                "  </table>\n" +
                                "</td>\n" +
                                "</tr>\n" +
                                "</table>\n" +
                                "</td>\n" +
                                "</tr>\n" +
                                "</table>\n" +
                                "</td>\n" +
                                "</tr>\n" +
                                "</table>\n" +
                                "<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" " +
                                "width=\"100%\" " +
                                "bgcolor=\"#FFFFFF\"><tr><td valign=\"top\"  " +
                                "style=\"padding:0px\"><table" +
                                " cellpadding=\"0\" cellspacing=\"0\" width=\"600\" " +
                                "align=\"center\"  " +
                                "style=\"max-width:600px;min-width:240px;margin:0 auto\" " +
                                "class=\"email-root-wrapper\"><tr><td valign=\"top\"  " +
                                "style=\"padding:0px\"><table cellpadding=\"0\" " +
                                "cellspacing=\"0\"" +
                                " " +
                                "border=\"0\" width=\"100%\" bgcolor=\"#282828\"  " +
                                "style=\"border:0px " +
                                "none;background-color:#282828;background-image:url" +
                                "('https://images" +
                                ".chamaileon.io/foto_v2.png');background-repeat:repeat-y;" +
                                "background-position:center top\"><tr><td valign=\"top\"  " +
                                "style=\"padding:30px\"><table cellpadding=\"0\" " +
                                "cellspacing=\"0\"" +
                                " " +
                                "width=\"100%\"><tr><td  style=\"padding:0px\" " +
                                "class=\"pattern\"><table " +
                                "cellpadding=\"0\" cellspacing=\"0\" border=\"0\" " +
                                "width=\"100%\"><tr><td" +
                                " " +
                                "valign=\"top\" \n" +
                                "  style=\"padding:0;mso-cellspacing:0in\"><table " +
                                "cellpadding=\"0\" " +
                                "cellspacing=\"0\" border=\"0\" align=\"left\" width=\"363\" " +
                                "id=\"c363p67r\"  style=\"float:left\" " +
                                "class=\"c363p67r\"><tr><td" +
                                " " +
                                "valign=\"top\"  style=\"padding:0px\"><table cellpadding=\"0\" " +
                                "cellspacing=\"0\" border=\"0\" width=\"100%\" bgcolor=\"#\"  " +
                                "style=\"border:0px none;background-color:#\"><tr><td " +
                                "valign=\"top\"  " +
                                "style=\"padding:30px\"><table cellpadding=\"0\" " +
                                "cellspacing=\"0\"" +
                                " " +
                                "width=\"100%\"><tr><td  style=\"padding:0px\"><div  " +
                                "style=\"text-align:left;font-family:Bitter, Georgia, Times, " +
                                "Times" +
                                " New " +
                                "Roman, serif;font-size:14px;color:#000000;line-height:22px;" +
                                "mso-line-height:exactly;mso-text-raise:4px\"><h1\n" +
                                "    style=\"font-family:Roboto, Helvetica Neue, Helvetica, " +
                                "Arial," +
                                " " +
                                "sans-serif; font-size: 28px; color: #FFFFFF; line-height: 40px;" +
                                " " +
                                "mso-line-height: exactly; mso-text-raise: 6px; padding: 0; " +
                                "margin: 0;" +
                                "\"><span class=\"mso-font-fix-arial\">¡Descárgala " +
                                "ahora!</span></h1></div><table cellpadding=\"0\" " +
                                "cellspacing=\"0\" " +
                                "border=\"0\" width=\"100%\"><tr><td valign=\"top\"  " +
                                "style=\"padding:5px\"><div  style=\"text-align:left;" +
                                "font-family:Roboto," +
                                " " +
                                "Helvetica Neue, Helvetica, Arial, sans-serif;font-size:14px;" +
                                "color:#FFFFFF;line-height:30px;mso-line-height:exactly;" +
                                "mso-text-raise:8px\"><p style=\"padding: 0; margin: 0;" +
                                "text-align:" +
                                " " +
                                "justify;\">Recibe alertas de los últimos sismos ocurridos en " +
                                "Chile</p></div></td>\n" +
                                "    </tr>\n" +
                                "  </table>\n" +
                                "  <table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" " +
                                "width=\"100%\"><tr><td valign=\"top\"  " +
                                "style=\"padding:10px\"><table " +
                                "cellpadding=\"0\" cellspacing=\"0\" width=\"100%\"><tr><td  " +
                                "style=\"padding:0px\"><table cellpadding=\"0\" " +
                                "cellspacing=\"0\"" +
                                " " +
                                "border=\"0\" width=\"100%\"  style=\"border-top:2px solid " +
                                "transparent\"><tr><td valign=\"top\"><table cellpadding=\"0\" " +
                                "cellspacing=\"0\" width=\"100%\"><tr><td  " +
                                "style=\"padding:0px\"></td>\n" +
                                "  </tr>\n" +
                                "</table>\n" +
                                "</td>\n" +
                                "</tr>\n" +
                                "</table>\n" +
                                "</td>\n" +
                                "</tr>\n" +
                                "</table>\n" +
                                "</td>\n" +
                                "</tr>\n" +
                                "</table>\n" +
                                "<table cellpadding=\"0\" cellspacing=\"0\" " +
                                "width=\"100%\"><tr><td" +
                                " " +
                                "align=\"left\"  style=\"padding:0px\"><table cellpadding=\"0\" " +
                                "cellspacing=\"0\" border=\"0\" bgcolor=\"#\" align=\"left\" " +
                                "width=\"152\" height=\"59\"  style=\"border:0px none;" +
                                "background-color:#;" +
                                "height:auto\" class=\"full-width\"><tr><td valign=\"top\"  " +
                                "style=\"padding:0px\"><a \n" +
                                "  href=\"https://lastquakechile.page.link/lqch\" " +
                                "target=\"_blank\" " +
                                "class=\"imglink\"><img \n" +
                                "  src=\"https://images.chamaileon.io/google-play-badge.png\" " +
                                "width=\"152\" height=\"59\" alt=\"\" border=\"0\"  " +
                                "style=\"display:block;width:100%;height:auto\" " +
                                "class=\"full-width" +
                                " " +
                                "img152x59\"  /></a></td>\n" +
                                "</tr>\n" +
                                "</table>\n" +
                                "</td>\n" +
                                "</tr>\n" +
                                "</table>\n" +
                                "</td>\n" +
                                "</tr>\n" +
                                "</table>\n" +
                                "</td>\n" +
                                "</tr>\n" +
                                "</table>\n" +
                                "</td>\n" +
                                "</tr>\n" +
                                "</table>\n" +
                                "<!--[if gte mso 9]></td><td valign=\"top\" style=\"padding:0;" +
                                "\"><![endif]-->\n" +
                                "  <table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" " +
                                "align=\"left\"" +
                                " " +
                                "width=\"176\" id=\"c176p32r\"  style=\"float:left\" " +
                                "class=\"c176p32r\"><tr><td valign=\"top\"  " +
                                "style=\"padding:0px\"><table" +
                                " " +
                                "cellpadding=\"0\" cellspacing=\"0\" width=\"100%\"><tr><td " +
                                "align=\"center\"  style=\"padding:0px\"><table " +
                                "cellpadding=\"0\"" +
                                " " +
                                "cellspacing=\"0\" border=\"0\" align=\"center\" width=\"170\" " +
                                "height=\"343\"  style=\"border:0px none;height:auto\" " +
                                "class=\"full-width\"><tr><td valign=\"top\"  " +
                                "style=\"padding:0px\"><img" +
                                " " +
                                "\n" +
                                "    src=\"https://images.chamaileon.io/lastquakechile_pantalla" +
                                ".png\" " +
                                "width=\"170\" height=\"343\" alt=\"\" border=\"0\"  " +
                                "style=\"display:block;width:100%;height:auto\" " +
                                "class=\"full-width" +
                                " " +
                                "img170x343\"  /></td>\n" +
                                "  </tr>\n" +
                                "</table>\n" +
                                "</td>\n" +
                                "</tr>\n" +
                                "</table>\n" +
                                "</td>\n" +
                                "</tr>\n" +
                                "</table>\n" +
                                "</td>\n" +
                                "</tr>\n" +
                                "</table>\n" +
                                "</td>\n" +
                                "</tr>\n" +
                                "</table>\n" +
                                "</td>\n" +
                                "</tr>\n" +
                                "</table>\n" +
                                "</td>\n" +
                                "</tr>\n" +
                                "</table>\n" +
                                "</td>\n" +
                                "</tr>\n" +
                                "</table>\n" +
                                "<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" " +
                                "width=\"100%\" " +
                                "bgcolor=\"#FFFFFF\"><tr><td valign=\"top\"  " +
                                "style=\"padding:0px\"><table" +
                                " cellpadding=\"0\" cellspacing=\"0\" width=\"600\" " +
                                "align=\"center\"  " +
                                "style=\"max-width:600px;min-width:240px;margin:0 auto\" " +
                                "class=\"email-root-wrapper\"><tr><td valign=\"top\"  " +
                                "style=\"padding:0px\"><table cellpadding=\"0\" " +
                                "cellspacing=\"0\"" +
                                " " +
                                "border=\"0\" width=\"100%\" bgcolor=\"#303f9f\"  " +
                                "style=\"border:0px " +
                                "none;background-color:#303f9f\"><tr><td valign=\"top\"  " +
                                "style=\"padding:0px\"><table cellpadding=\"0\" " +
                                "cellspacing=\"0\"" +
                                " " +
                                "width=\"100%\"><tr><td  style=\"padding:0px\" " +
                                "class=\"pattern\"><table " +
                                "cellpadding=\"0\" cellspacing=\"0\" border=\"0\" " +
                                "width=\"100%\"><tr><td" +
                                " " +
                                "valign=\"top\"  style=\"padding:0;mso-cellspacing:0in\"><table " +
                                "cellpadding=\"0\" cellspacing=\"0\" border=\"0\" align=\"left\"" +
                                " " +
                                "width=\"399\" id=\"c399p66r\" \n" +
                                "  style=\"float:left\" class=\"c399p66r\"><tr><td " +
                                "valign=\"top\"" +
                                " " +
                                " " +
                                "style=\"padding:0px\"><table cellpadding=\"0\" " +
                                "cellspacing=\"0\"" +
                                " " +
                                "border=\"0\" width=\"100%\" bgcolor=\"#303f9f\"  " +
                                "style=\"border:0px " +
                                "none;background-color:#303f9f\"><tr><td valign=\"top\"  " +
                                "style=\"padding:0px\"><table cellpadding=\"0\" " +
                                "cellspacing=\"0\"" +
                                " " +
                                "width=\"100%\"><tr><td  style=\"padding:0px\"><table " +
                                "cellpadding=\"0\" " +
                                "cellspacing=\"0\" border=\"0\" width=\"100%\"><tr><td " +
                                "valign=\"top\"  " +
                                "style=\"padding-top:20px;padding-right:30px;" +
                                "padding-bottom:20px;" +
                                "padding-left:30px\"><div  style=\"text-align:left;" +
                                "font-family:Roboto, " +
                                "Helvetica Neue, Helvetica, Arial, sans-serif;font-size:20px;" +
                                "color:#FFFFFF;line-height:22px;mso-line-height:exactly;" +
                                "mso-text-raise:1px\"><p style=\"padding: 0; margin: 0;" +
                                "text-align:" +
                                " " +
                                "center;\">¡Haz recibido invitación de un amigo " +
                                "!</p></div></td>\n" +
                                "  </tr>\n" +
                                "</table>\n" +
                                "</td>\n" +
                                "</tr>\n" +
                                "</table>\n" +
                                "</td>\n" +
                                "</tr>\n" +
                                "</table>\n" +
                                "</td>\n" +
                                "</tr>\n" +
                                "</table>\n" +
                                "<!--[if gte mso 9]></td><td valign=\"top\" style=\"padding:0;" +
                                "\"><![endif]-->\n" +
                                "  <table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" " +
                                "align=\"left\"" +
                                " " +
                                "width=\"200\" id=\"c200p33r\"  style=\"float:left\" " +
                                "class=\"c200p33r\"><tr><td valign=\"top\"  " +
                                "style=\"padding:0px\"><table" +
                                " " +
                                "cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=\"100%\"" +
                                " " +
                                "bgcolor=\"#303f9f\"  style=\"border:0px none;" +
                                "background-color:#303f9f\"><tr><td valign=\"top\"  " +
                                "style=\"padding:0px\"><table cellpadding=\"0\" " +
                                "cellspacing=\"0\"" +
                                " " +
                                "width=\"100%\"><tr><td  style=\"padding:0px\"><table " +
                                "cellpadding=\"0\" " +
                                "cellspacing=\"0\" width=\"100%\"><tr><td align=\"center\"  " +
                                "style=\"padding:0px\"><table cellpadding=\"0\" " +
                                "cellspacing=\"0\"" +
                                " " +
                                "border=\"0\" align=\"center\"  style=\"text-align:center;" +
                                "color:#000\"><tr><td valign=\"top\" align=\"center\"  " +
                                "style=\"padding:15px\"><table cellpadding=\"0\" " +
                                "cellspacing=\"0\"" +
                                " " +
                                "border=\"0\" bgcolor=\"#0B7DFF\" \n" +
                                "    style=\"border:0px none;border-radius:5px;" +
                                "border-collapse:separate " +
                                "!important;background-color:#0B7DFF\"><tr><td valign=\"top\" " +
                                "align=\"justify\"  style=\"padding:10px\"><a\n" +
                                "      href=\"https://lastquakechile.page.link/lqch\" " +
                                "target=\"_blank\" " +
                                " " +
                                "style=\"text-decoration:none\" class=\"edm_button\"><span  " +
                                "style=\"font-family:Roboto, Helvetica Neue, Helvetica, Arial, " +
                                "sans-serif;font-size:13px;color:#FFFFFF;line-height:18px;" +
                                "text-decoration:none\"><strong>Descargar aqu&#xED;" +
                                "</strong></span>\n" +
                                "    </a></td>\n" +
                                "  </tr>\n" +
                                "</table>\n" +
                                "</td>\n" +
                                "</tr>\n" +
                                "</table>\n" +
                                "</td>\n" +
                                "</tr>\n" +
                                "</table>\n" +
                                "</td>\n" +
                                "</tr>\n" +
                                "</table>\n" +
                                "</td>\n" +
                                "</tr>\n" +
                                "</table>\n" +
                                "</td>\n" +
                                "</tr>\n" +
                                "</table>\n" +
                                "</td>\n" +
                                "</tr>\n" +
                                "</table>\n" +
                                "</td>\n" +
                                "</tr>\n" +
                                "</table>\n" +
                                "</td>\n" +
                                "</tr>\n" +
                                "</table>\n" +
                                "</td>\n" +
                                "</tr>\n" +
                                "</table>\n" +
                                "</td>\n" +
                                "</tr>\n" +
                                "</table>\n" +
                                "</div>\n" +
                                "<!-- content end -->\n" +
                                "</center>\n" +
                                "</td>\n" +
                                "</tr>\n" +
                                "</table>\n" +
                                "</body>\n" +
                                "</html>")
                        .setEmailSubject(getString(R.string.INVITATION_SUBJECT))
                        .build();
        startActivityForResult(mIntent, 0);
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


