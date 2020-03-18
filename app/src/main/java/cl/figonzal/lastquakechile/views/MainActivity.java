package cl.figonzal.lastquakechile.views;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;

import cl.figonzal.lastquakechile.FragmentPageAdapter;
import cl.figonzal.lastquakechile.R;
import cl.figonzal.lastquakechile.SettingsActivity;
import cl.figonzal.lastquakechile.services.AdsService;
import cl.figonzal.lastquakechile.services.Utils;
import cl.figonzal.lastquakechile.services.notifications.ChangeLogNotification;
import cl.figonzal.lastquakechile.services.notifications.NotificationService;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class MainActivity extends AppCompatActivity {


    private AppBarLayout mAppBarLayout;
    private ImageView mIvFoto;
    private AdsService adsService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //Setear configuraciones por defecto de ConfigActivity
        PreferenceManager.setDefaultValues(this, R.xml.root_preferences, false);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Configurar MODO NOCHE
        Utils.checkNightMode(MainActivity.this, getWindow());

        //Checkear logica de first run con actividad de welcome
        checkWelcomeActivity();

        //Servicios de google play
        Utils.checkPlayServices(this);

        //Servicios de Firebase
        getFirebaseToken();

        //ADS
        MobileAds.initialize(getApplicationContext(), getString(R.string.ADMOB_MASTER_KEY));
        adsService = new AdsService(getApplicationContext(), getSupportFragmentManager());
        adsService.rewardDialog(this);

        //Creacion de canal de notificaciones para sismos y para changelogs (Requerido para API >26)
        NotificationService.createNotificationChannel(getApplicationContext());

        //Realizar suscripcion el tema 'Quakes' para notificaciones
        NotificationService.checkSuscriptions(this);

        //Enviar notificacion changelog de ser necesario
        new ChangeLogNotification().configNotificationChangeLog(false, getApplicationContext());

        //Setear toolbars, viewpagers y tabs
        setToolbarViewPagerTabs();

        //Setear imagen de toolbar
        loadImageToolbar();
    }

    /**
     * Funcion encargada de realizar la iniciacion de los servicios de FIREBASE
     */
    private void getFirebaseToken() {

        //FIREBASE SECTION
        FirebaseMessaging.getInstance().setAutoInitEnabled(true);
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(this,
                new OnSuccessListener<InstanceIdResult>() {
                    @Override
                    public void onSuccess(InstanceIdResult instanceIdResult) {
                        String token = instanceIdResult.getToken();
                        Log.e(getString(R.string.TAG_FIREBASE_TOKEN), token);

                        //CRASH ANALYTICS LOG
                        Crashlytics.log(Log.DEBUG, getString(R.string.TAG_FIREBASE_TOKEN), token);
                        Crashlytics.setUserIdentifier(token);


                    }
                });
    }

    /**
     * Funcion encargada de realizar el checkeo de first run de la aplicacion para lanzar
     * welcomeActivity
     */
    private void checkWelcomeActivity() {
        Bundle mBundleWelcome = getIntent().getExtras();
        if (mBundleWelcome != null) {
            //Si el usuario viene desde deep link, no se realiza first check (Para que welcome
            // activity no abra 2 veces)
            //Si viene desde Google play, se realiza el check
            if (!mBundleWelcome.getBoolean(getString(R.string.desde_deep_link))) {
                Utils.checkFirstRun(this, false);
            }
        }
    }

    /**
     * Setear elementos de UI necesarios para el funcionamiento de la APP
     */
    private void setToolbarViewPagerTabs() {
        //Buscar toolbar en resources
        Toolbar mToolbar = findViewById(R.id.tool_bar);

        //Setear el toolbar sobre el main activity
        setSupportActionBar(mToolbar);

        //Appbar layout para minimizar el collapse toolbar cuando se presiona el tab de mapa
        mAppBarLayout = findViewById(R.id.app_bar);

        //View pager para los fragments (Solo 1 fragment en esta app)
        ViewPager mViewPager = findViewById(R.id.view_pager);
        mViewPager.setAdapter(new FragmentPageAdapter(getSupportFragmentManager(), getApplicationContext()));


        //Seteo de tabs.
        TabLayout mTabLayout = findViewById(R.id.tabs);
        mTabLayout.setupWithViewPager(mViewPager);
        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {

            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 2) {
                    mAppBarLayout.setExpanded(false);
                } else {
                    mAppBarLayout.setExpanded(true);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        //Setear collapsing toolbar con titulo estatico superior y animacion de colores al recoger
        // toolbar
        CollapsingToolbarLayout mCollapsingToolbarLayout = findViewById(R.id.collapsing_toolbar);
        mCollapsingToolbarLayout.setTitleEnabled(true);

        int modeNightType = getResources().getConfiguration().uiMode &
                Configuration.UI_MODE_NIGHT_MASK;

        //Detecta modo noche automatico como YES
        if (modeNightType == Configuration.UI_MODE_NIGHT_YES) {
            mCollapsingToolbarLayout.setContentScrimColor(getResources().getColor(R.color.colorPrimaryNightMode
                    , getTheme()));
        } else if (modeNightType == Configuration.UI_MODE_NIGHT_NO) {
            mCollapsingToolbarLayout.setContentScrimColor(getResources().getColor(R.color.colorPrimary,
                    getTheme()));
        }
    }

    /**
     * Funcion encargada de cargar la imagen de fondo en el toolbar
     */
    private void loadImageToolbar() {
        mIvFoto = findViewById(R.id.toolbar_image);
        Glide.with(this)
                .load(R.drawable.foto)
                .apply(
                        new RequestOptions()
                                .placeholder(R.drawable.placeholder)
                                .error(R.drawable.not_found)
                )
                .transition(withCrossFade())
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                Target<Drawable> target,
                                                boolean isFirstResource) {
                        mIvFoto.setImageDrawable(getDrawable(R.drawable.not_found));
                        return false;
                    }

                    //No es necesario usarlo (If u want)
                    @Override
                    public boolean onResourceReady(Drawable resource, Object model,
                                                   Target<Drawable> target,
                                                   DataSource dataSource,
                                                   boolean isFirstResource) {

                        return false;
                    }
                })
                .into(mIvFoto);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.invite_menu:
                onInviteClicked();
                return true;

            case R.id.settings_menu:
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Utils.checkPlayServices(this);
        adsService.getRewardedVideoAd().resume(this);
    }

    @Override
    public void onPause() {
        adsService.getRewardedVideoAd().pause(this);
        super.onPause();
    }

    @Override
    public void onDestroy() {
        adsService.getRewardedVideoAd().destroy(this);
        super.onDestroy();
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
}
