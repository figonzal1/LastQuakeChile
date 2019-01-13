package cl.figonzal.lastquakechile;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
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
import com.google.android.gms.appinvite.AppInviteInvitation;

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
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {


        // Inflate the layout for thi{s fragment
        final View v = inflater.inflate(R.layout.fragment_quake, container, false);


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

                    //TIMEOUT ERROR
                    if (status.equals(getString(R.string.VIEWMODEL_TIMEOUT_ERROR))) {
                        Snackbar
                                .make(v, status, Snackbar.LENGTH_INDEFINITE)
                                .setAction(getString(R.string.FLAG_RETRY), new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        viewModel.refreshMutableQuakeList();
                                        progressBar.setVisibility(View.VISIBLE);

                                        Crashlytics.setBool(getString(R.string.SNACKBAR_TIMEOUT_ERROR_PRESSED), true);
                                    }
                                })
                                .show();

                        Log.d(getString(R.string.TAG_PROGRESS_FROM_FRAGMENT), getString(R.string.TAG_PROGRESS_FROM_FRAGMENT_TIMEOUT_ERROR));
                        Crashlytics.log(Log.DEBUG, getString(R.string.TAG_PROGRESS_FROM_FRAGMENT), getString(R.string.TAG_PROGRESS_FROM_FRAGMENT_TIMEOUT_ERROR));
                    }

                    //SERVER ERROR
                    else if (status.equals(getString(R.string.VIEWMODEL_SERVER_ERROR))) {
                        Snackbar
                                .make(v, status, Snackbar.LENGTH_INDEFINITE)
                                .setAction(getString(R.string.FLAG_RETRY), new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        viewModel.refreshMutableQuakeList();
                                        progressBar.setVisibility(View.VISIBLE);

                                        Crashlytics.setBool(getString(R.string.SNACKBAR_SERVER_ERROR_PRESSED), true);
                                    }
                                })
                                .show();

                        Log.d(getString(R.string.TAG_PROGRESS_FROM_FRAGMENT), getString(R.string.TAG_PROGRESS_FROM_FRAGMENT_SERVER_ERROR));
                        Crashlytics.log(Log.DEBUG, getString(R.string.TAG_PROGRESS_FROM_FRAGMENT), getString(R.string.TAG_PROGRESS_FROM_FRAGMENT_SERVER_ERROR));

                    }

                    //NOCONNECTION ERROR
                    else if (status.equals(getString(R.string.VIEWMODEL_NOCONNECTION_ERROR))) {
                        Snackbar
                                .make(v, status, Snackbar.LENGTH_INDEFINITE)
                                .setAction(getString(R.string.FLAG_RETRY), new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        viewModel.refreshMutableQuakeList();
                                        progressBar.setVisibility(View.VISIBLE);

                                        Crashlytics.setBool(getString(R.string.SNACKBAR_NOCONNECTION_ERROR_PRESSED), true);
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
        menu.clear();
        inflater.inflate(R.menu.toolbar_menu, menu);

        MenuItem menuItem = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setOnQueryTextListener(this);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.refresh:

                //Se refresca el listado de sismos
                viewModel.refreshMutableQuakeList();

                //Progress bar desaparece despues de la descarga de datos
                progressBar.setVisibility(View.INVISIBLE);

                return true;

            case R.id.contact:

                //Intent de contacto
                Intent intent = new Intent(getActivity(), ContactActivity.class);
                startActivity(intent);

                return true;


            case R.id.invite:

                //Intent de invitacion
                onInviteClicked();
                return true;
        }
        return true;
    }

    /**
     * Funcion encargada de configurar el intent para la invitacion.
     */
    //TODO: Agregar un metodo para escoger entre compartir el link a redes sociales o enviarlo por mail
    private void onInviteClicked() {
        Intent intent = new AppInviteInvitation.IntentBuilder(getString(R.string.INVITATION_TITLE))
                .setMessage(getString(R.string.INVITATION_MESSAGE))
                .setDeepLink(Uri.parse(getString(R.string.INVITATION_DEEP_LINK)))
                .setEmailHtmlContent("<!DOCTYPE html>\n" +
                        "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n" +
                        "<head>\n" +
                        "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />\n" +
                        "<meta name=\"viewport\" content=\"width=device-width\">\n" +
                        "<meta name=\"HandheldFriendly\" content=\"true\" />\n" +
                        "<meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\" />\n" +
                        "<!--[if gte IE 7]><html class=\"ie8plus\" xmlns=\"http://www.w3.org/1999/xhtml\"><![endif]-->\n" +
                        "<!--[if IEMobile]><html class=\"ie8plus\" xmlns=\"http://www.w3.org/1999/xhtml\"><![endif]-->\n" +
                        "<meta name=\"format-detection\" content=\"telephone=no\">\n" +
                        "<meta name=\"generator\" content=\"EDMdesigner, www.edmdesigner.com\">\n" +
                        "<title>Untitled 12 Jan 2019 20:14:17 GMT</title>\n" +
                        "\n" +
                        "<link href=\"https://fonts.googleapis.com/css?family=Roboto\" rel=\"stylesheet\" type=\"text/css\">\n" +
                        "\n" +
                        "<style type=\"text/css\" media=\"screen\">\n" +
                        "* {line-height: inherit;}\n" +
                        ".ExternalClass * { line-height: 100%; }\n" +
                        "body, p{margin:0; padding:0; margin-bottom:0; -webkit-text-size-adjust:none; -ms-text-size-adjust:none;} img{line-height:100%; outline:none; text-decoration:none; -ms-interpolation-mode: bicubic;} a img{border: none;} a, a:link, .no-detect-local a, .appleLinks a{color:#5555ff !important; text-decoration: underline;} .ExternalClass {display: block !important; width:100%;} .ExternalClass, .ExternalClass p, .ExternalClass span, .ExternalClass font, .ExternalClass td, .ExternalClass div { line-height: inherit; } table td {border-collapse:collapse;mso-table-lspace: 0pt; mso-table-rspace: 0pt;} sup{position: relative; top: 4px; line-height:7px !important;font-size:11px !important;} .mobile_link a[href^=\"tel\"], .mobile_link a[href^=\"sms\"] {text-decoration: default; color: #5555ff !important;\n" +
                        "pointer-events: auto; cursor: default;} .no-detect a{text-decoration: none; color: #5555ff; pointer-events: auto; cursor: default;} {color: #5555ff;} span {color: inherit; border-bottom: none;} span:hover { background-color: transparent; }\n" +
                        "\n" +
                        ".nounderline {text-decoration: none !important;}\n" +
                        "h1, h2, h3 { margin:0; padding:0; }\n" +
                        "p {Margin: 0px !important; }\n" +
                        "\n" +
                        "table[class=\"email-root-wrapper\"] { width: 600px !important; }\n" +
                        "\n" +
                        "body {\n" +
                        "background-color: #707070;\n" +
                        "background: #707070;\n" +
                        "}\n" +
                        "body { min-width: 280px; width: 100%;}\n" +
                        "td[class=\"pattern\"] .c350p67r { width: 67.30769230769232%;}\n" +
                        "td[class=\"pattern\"] .c169p32r { width: 32.69230769230768%;}\n" +
                        "td[class=\"pattern\"] .c399p66r { width: 66.66666666666666%;}\n" +
                        "td[class=\"pattern\"] .c200p33r { width: 33.33333333333334%;}\n" +
                        "\n" +
                        "</style>\n" +
                        "<style>\n" +
                        "@media only screen and (max-width: 599px),\n" +
                        "only screen and (max-device-width: 599px),\n" +
                        "only screen and (max-width: 400px),\n" +
                        "only screen and (max-device-width: 400px) {\n" +
                        " .email-root-wrapper { width: 100% !important; }\n" +
                        " .full-width { width: 100% !important; height: auto !important; text-align:center;}\n" +
                        " .fullwidthhalfleft {width:100% !important;}\n" +
                        " .fullwidthhalfright {width:100% !important;}\n" +
                        " .fullwidthhalfinner {width:100% !important; margin: 0 auto !important; float: none !important; margin-left: auto !important; margin-right: auto !important; clear:both !important; }\n" +
                        " .hide { display:none !important; width:0px !important;height:0px !important; overflow:hidden; }\n" +
                        " .desktop-hide { display:block !important; width:100% !important;height:auto !important; overflow:hidden; max-height: inherit !important; }\n" +
                        "\t.c350p67r { width: 100% !important; float:none;}\n" +
                        ".c169p32r { width: 100% !important; float:none;}\n" +
                        ".c399p66r { width: 100% !important; float:none;}\n" +
                        ".c200p33r { width: 100% !important; float:none;}\n" +
                        "\n" +
                        "}\n" +
                        "</style>\n" +
                        "<style>\n" +
                        "@media only screen and (min-width: 600px) {\n" +
                        "  td[class=\"pattern\"] .c350p67r { width: 350px !important;}\n" +
                        "td[class=\"pattern\"] .c169p32r { width: 169px !important;}\n" +
                        "td[class=\"pattern\"] .c399p66r { width: 399px !important;}\n" +
                        "td[class=\"pattern\"] .c200p33r { width: 200px !important;}\n" +
                        "\n" +
                        "}\n" +
                        "@media only screen and (max-width: 599px),\n" +
                        "only screen and (max-device-width: 599px),\n" +
                        "only screen and (max-width: 400px),\n" +
                        "only screen and (max-device-width: 400px) {\n" +
                        "  table[class=\"email-root-wrapper\"] { width: 100% !important; }\n" +
                        "  td[class=\"wrap\"] .full-width { width: 100% !important; height: auto !important;}\n" +
                        "\n" +
                        "  td[class=\"wrap\"] .fullwidthhalfleft {width:100% !important;}\n" +
                        "  td[class=\"wrap\"] .fullwidthhalfright {width:100% !important;}\n" +
                        "  td[class=\"wrap\"] .fullwidthhalfinner {width:100% !important; margin: 0 auto !important; float: none !important; margin-left: auto !important; margin-right: auto !important; clear:both !important; }\n" +
                        "  td[class=\"wrap\"] .hide { display:none !important; width:0px;height:0px; overflow:hidden; }\n" +
                        "\n" +
                        "  td[class=\"pattern\"] .c350p67r { width: 100% !important; }\n" +
                        "td[class=\"pattern\"] .c169p32r { width: 100% !important; }\n" +
                        "td[class=\"pattern\"] .c399p66r { width: 100% !important; }\n" +
                        "td[class=\"pattern\"] .c200p33r { width: 100% !important; }\n" +
                        "\n" +
                        "}\n" +
                        "\n" +
                        "\n" +
                        "</style>\n" +
                        "\n" +
                        "<!--[if (gte IE 7) & (vml)]>\n" +
                        "<style type=\"text/css\">\n" +
                        "html, body {margin:0 !important; padding:0px !important;}\n" +
                        "img.full-width { position: relative !important; }\n" +
                        "\n" +
                        ".img170x262 { width: 170px !important; height: 262px !important;}\n" +
                        "\n" +
                        "</style>\n" +
                        "<![endif]-->\n" +
                        "\n" +
                        "<!--[if gte mso 9]>\n" +
                        "<style type=\"text/css\">\n" +
                        ".mso-font-fix-arial { font-family: Arial, sans-serif;}\n" +
                        ".mso-font-fix-georgia { font-family: Georgia, sans-serif;}\n" +
                        ".mso-font-fix-tahoma { font-family: Tahoma, sans-serif;}\n" +
                        ".mso-font-fix-times_new_roman { font-family: 'Times New Roman', sans-serif;}\n" +
                        ".mso-font-fix-trebuchet_ms { font-family: 'Trebuchet MS', sans-serif;}\n" +
                        ".mso-font-fix-verdana { font-family: Verdana, sans-serif;}\n" +
                        "</style>\n" +
                        "<![endif]-->\n" +
                        "\n" +
                        "<!--[if gte mso 9]>\n" +
                        "<style type=\"text/css\">\n" +
                        "table, td {\n" +
                        "border-collapse: collapse !important;\n" +
                        "mso-table-lspace: 0px !important;\n" +
                        "mso-table-rspace: 0px !important;\n" +
                        "}\n" +
                        "\n" +
                        ".email-root-wrapper { width 600px !important;}\n" +
                        ".imglink { font-size: 0px; }\n" +
                        ".edm_button { font-size: 0px; }\n" +
                        "</style>\n" +
                        "<![endif]-->\n" +
                        "\n" +
                        "<!--[if gte mso 15]>\n" +
                        "<style type=\"text/css\">\n" +
                        "table {\n" +
                        "font-size:0px;\n" +
                        "mso-margin-top-alt:0px;\n" +
                        "}\n" +
                        "\n" +
                        ".fullwidthhalfleft {\n" +
                        "width: 49% !important;\n" +
                        "float:left !important;\n" +
                        "}\n" +
                        "\n" +
                        ".fullwidthhalfright {\n" +
                        "width: 50% !important;\n" +
                        "float:right !important;\n" +
                        "}\n" +
                        "</style>\n" +
                        "<![endif]-->\n" +
                        "<STYLE type=\"text/css\" media=\"(pointer) and (min-color-index:0)\">\n" +
                        "html, body {background-image: none !important; background-color: transparent !important; margin:0 !important; padding:0 !important;}\n" +
                        "</STYLE>\n" +
                        "\n" +
                        "</head>\n" +
                        "<body leftmargin=\"0\" marginwidth=\"0\" topmargin=\"0\" marginheight=\"0\" offset=\"0\" style=\"font-family:Arial, sans-serif; font-size:0px;margin:0;padding:0;background: #707070 !important;\" bgcolor=\"#707070\">\n" +
                        "<!--[if t]><![endif]--><!--[if t]><![endif]--><!--[if t]><![endif]--><!--[if t]><![endif]--><!--[if t]><![endif]--><!--[if t]><![endif]-->\n" +
                        "  <table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" height=\"100%\" width=\"100%\"  bgcolor=\"#707070\" style=\"margin:0; padding:0; width:100% !important; background: #707070 !important;\">\n" +
                        "    <tr>\n" +
                        "        <td class=\"wrap\" align=\"center\" valign=\"top\" width=\"100%\">\n" +
                        "          <center>\n" +
                        "<!-- content -->\n" +
                        "<div  style=\"padding:0px\"><table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=\"100%\" bgcolor=\"#FFFFFF\"><tr><td valign=\"top\"  style=\"padding:0px\"><table cellpadding=\"0\" cellspacing=\"0\" width=\"600\" align=\"center\"  style=\"max-width:600px;min-width:240px;margin:0 auto\" class=\"email-root-wrapper\"><tr><td valign=\"top\"  style=\"padding:0px\"><table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=\"100%\" bgcolor=\"#999999\"  style=\"border:0px none;background-color:#999999\"><tr><td valign=\"top\"  style=\"padding:0px\"><table cellpadding=\"0\" cellspacing=\"0\" width=\"100%\"><tr><td  style=\"padding:0px\"><table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=\"100%\"><tr><td valign=\"top\"  style=\"padding:10px\"><div \n" +
                        "style=\"text-align:left;font-family:Arial;font-size:14px;color:#000000;line-height:22px;mso-line-height:exactly;mso-text-raise:4px\"><p style=\"padding: 0; margin: 0;\">Last Quake Chile</p></div></td>\n" +
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
                        "<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=\"100%\" bgcolor=\"#FFFFFF\"><tr><td valign=\"top\"  style=\"padding:0px\"><table cellpadding=\"0\" cellspacing=\"0\" width=\"600\" align=\"center\"  style=\"max-width:600px;min-width:240px;margin:0 auto\" class=\"email-root-wrapper\"><tr><td valign=\"top\"  style=\"padding:0px\"><table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=\"100%\" bgcolor=\"#282828\"  style=\"border:0px none;background-color:#282828;background-image:url('https://images.chamaileon.io/foto.png');background-repeat:no-repeat;background-position:center top\"><tr><td valign=\"top\"  style=\"padding-top:120px;padding-right:40px;padding-left:40px\"><table cellpadding=\"0\" cellspacing=\"0\" width=\"100%\"><tr><td  style=\"padding:0px\" class=\"pattern\"><table cellpadding=\"0\" cellspacing=\"0\" border=\"0\"\n" +
                        "width=\"100%\"><tr><td valign=\"top\"  style=\"padding:0;mso-cellspacing:0in\"><table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" align=\"left\" width=\"350\" id=\"c350p67r\"  style=\"float:left\" class=\"c350p67r\"><tr><td valign=\"top\"  style=\"padding:0px\"></td>\n" +
                        "</tr>\n" +
                        "</table>\n" +
                        "<!--[if gte mso 9]></td><td valign=\"top\" style=\"padding:0;\"><![endif]-->\n" +
                        "<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" align=\"left\" width=\"169\" id=\"c169p32r\"  style=\"float:left\" class=\"c169p32r\"><tr><td valign=\"top\"  style=\"padding:0px\"><table cellpadding=\"0\" cellspacing=\"0\" width=\"100%\"><tr><td align=\"center\"  style=\"padding:0px\"><table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" align=\"center\" width=\"170\" height=\"262\"  style=\"border:0px none;height:auto\" class=\"full-width\"><tr><td valign=\"top\"  style=\"padding:0px\"><img \n" +
                        "src=\"https://images.chamaileon.io/580b57fbd9996e24bc43bf8a_1547325626879.png\" width=\"170\" height=\"262\" alt=\"\" border=\"0\"  style=\"display:block;width:100%;height:auto\" class=\"full-width img170x262\"  /></td>\n" +
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
                        "<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=\"100%\" bgcolor=\"#FFFFFF\"><tr><td valign=\"top\"  style=\"padding:0px\"><table cellpadding=\"0\" cellspacing=\"0\" width=\"600\" align=\"center\"  style=\"max-width:600px;min-width:240px;margin:0 auto\" class=\"email-root-wrapper\"><tr><td valign=\"top\"  style=\"padding:0px\"><table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=\"100%\" bgcolor=\"#303f9f\"  style=\"border:0px none;background-color:#303f9f\"><tr><td valign=\"top\"  style=\"padding:0px\"><table cellpadding=\"0\" cellspacing=\"0\" width=\"100%\"><tr><td  style=\"padding:0px\" class=\"pattern\"><table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=\"100%\"><tr><td valign=\"top\"  style=\"padding:0;mso-cellspacing:0in\"><table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" align=\"left\" width=\"399\" id=\"c399p66r\" \n" +
                        "style=\"float:left\" class=\"c399p66r\"><tr><td valign=\"top\"  style=\"padding:0px\"><table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=\"100%\" bgcolor=\"#303f9f\"  style=\"border:0px none;background-color:#303f9f\"><tr><td valign=\"top\"  style=\"padding:0px\"><table cellpadding=\"0\" cellspacing=\"0\" width=\"100%\"><tr><td  style=\"padding:0px\"><table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=\"100%\"><tr><td valign=\"top\"  style=\"padding:20px\"><div  style=\"text-align:left;font-family:Arial;font-size:20px;color:#000000;line-height:22px;mso-line-height:exactly;mso-text-raise:1px\"><p style=\"padding: 0; margin: 0;\">¡Haz recibido una invitación !</p></div></td>\n" +
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
                        "<!--[if gte mso 9]></td><td valign=\"top\" style=\"padding:0;\"><![endif]-->\n" +
                        "<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" align=\"left\" width=\"200\" id=\"c200p33r\"  style=\"float:left\" class=\"c200p33r\"><tr><td valign=\"top\"  style=\"padding:0px\"><table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=\"100%\" bgcolor=\"#303f9f\"  style=\"border:0px none;background-color:#303f9f\"><tr><td valign=\"top\"  style=\"padding:0px\"><table cellpadding=\"0\" cellspacing=\"0\" width=\"100%\"><tr><td  style=\"padding:0px\"><table cellpadding=\"0\" cellspacing=\"0\" width=\"100%\"><tr><td align=\"center\"  style=\"padding:0px\"><table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" align=\"center\"  style=\"text-align:center;color:#000\"><tr><td valign=\"top\" align=\"center\"  style=\"padding:15px\"><table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" bgcolor=\"#3680E2\" \n" +
                        "style=\"border:0px none;border-radius:5px;border-collapse:separate !important;background-color:#3680E2\"><tr><td valign=\"top\" align=\"center\"  style=\"padding:10px\"><a\n" +
                        "href=\"\" target=\"_blank\"  style=\"text-decoration:none\" class=\"edm_button\"><span  style=\"font-family:Roboto, Helvetica Neue, Helvetica, Arial, sans-serif;font-size:12px;color:#000000;line-height:14px;text-decoration:none\"><span class=\"mso-font-fix-arial\">Descargarla aqu&#xED;</span></span>\n" +
                        "</a></td>\n" +
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
                        "</td>\n" +
                        "</tr>\n" +
                        "</table>\n" +
                        "</div>\n" +
                        "<!-- content end -->\n" +
                        "          </center>\n" +
                        "        </td>\n" +
                        "    </tr>\n" +
                        "  </table>\n" +
                        "</body>\n" +
                        "</html>")
                .setEmailSubject(getString(R.string.INVITATION_SUBJECT))
                .build();
        startActivityForResult(intent, 0);
    }


    /**
     * Funcion encargada de manejar el envio de la invication
     *
     * @param requestCode Fija el resultado de la operacion desde la activity
     * @param resultCode  Entrega el resultado del intent
     * @param data        parametro que entrega la informacion de los contactos
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("INTENT", "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);

        if (requestCode == 0) {
            if (resultCode == Activity.RESULT_OK) {
                // Get the invitation IDs of all sent messages
                String[] ids = AppInviteInvitation.getInvitationIds(resultCode, data);
                for (String id : ids) {
                    Log.d("INTENT", "Enviando invitacion a" + id);
                }
            } else {
                Snackbar.make(Objects.requireNonNull(getActivity()).findViewById(R.id.fragment), "Invitación cancelada", Snackbar.LENGTH_LONG).show();
            }
        }
    }
}


