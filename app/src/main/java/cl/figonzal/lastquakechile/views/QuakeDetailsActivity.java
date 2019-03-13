package cl.figonzal.lastquakechile.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.crashlytics.android.Crashlytics;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareHashtag;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;

import cl.figonzal.lastquakechile.R;
import cl.figonzal.lastquakechile.services.QuakeUtils;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class QuakeDetailsActivity extends AppCompatActivity {

    private ShareDialog shareDialog;
    private SharePhotoContent sharePhotoContent;
    private SharePhoto sharePhoto;
    private CallbackManager callbackManager;
    private Uri bitmapUri;
    private TextView tv_ciudad, tv_referencia, tv_escala, tv_magnitud, tv_profundidad, tv_fecha, tv_hora, tv_gms, fab_text_fb, fab_text_wsp, fab_text_gm, tv_estado;
    private ImageView iv_mapa, iv_sensible, iv_mag_color, iv_estado;
    private String ciudad, referencia, dms_lat, dms_long, fecha_local, escala, foto_url, estado;
    private Double magnitud, profundidad;
    private Map<String, Long> tiempos;
    private boolean sensible;
    private Bitmap bitmapFB;
    private boolean isFABOpen = false;
    private FloatingActionButton fab_share, fab_fb, fab_whatsapp, fab_gmail;
    private View overlay;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quake_details);

        //Setting toolbar
        Toolbar toolbar = findViewById(R.id.tool_bar_detail);
        setSupportActionBar(toolbar);

        //Muestra la flecha en toolbar para volver atras
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        //Obtener datos desde intent
        Bundle b = getIntent().getExtras();

        /*
            INICIACION DE RECURSOS
         */

        //TEXTVIEWS
        tv_ciudad = findViewById(R.id.tv_ciudad_detail);
        tv_referencia = findViewById(R.id.tv_referencia_detail);
        tv_escala = findViewById(R.id.tv_escala);
        tv_magnitud = findViewById(R.id.tv_magnitud_detail);
        tv_profundidad = findViewById(R.id.tv_epicentro);
        tv_fecha = findViewById(R.id.tv_fecha);
        tv_gms = findViewById(R.id.tv_gms);
        tv_hora = findViewById(R.id.tv_hora_detail);
        tv_estado = findViewById(R.id.tv_estado);

        //IMAGE VIEWS
        iv_sensible = findViewById(R.id.iv_sensible_detail);
        iv_mag_color = findViewById(R.id.iv_mag_color_detail);
        iv_estado = findViewById(R.id.iv_estado);

        iv_mapa = findViewById(R.id.iv_map_quake);

        if (b != null) {

            /*
                OBTENCION DE INFO DESDE INTENT
             */
            ciudad = b.getString(getString(R.string.INTENT_CIUDAD));
            referencia = b.getString(getString(R.string.INTENT_REFERENCIA));
            magnitud = b.getDouble(getString(R.string.INTENT_MAGNITUD));
            profundidad = b.getDouble(getString(R.string.INTENT_PROFUNDIDAD));
            escala = b.getString(getString(R.string.INTENT_ESCALA));
            //TODO: Agregar sensible a los detalles del sismo
            sensible = b.getBoolean(getString(R.string.INTENT_SENSIBLE));
            foto_url = b.getString(getString(R.string.INTENT_LINK_FOTO));
            estado = b.getString(getString(R.string.INTENT_ESTADO));

            /*
                SECCION DE TRANSFORMACION LAT-LONG TO GMS
             */
            final String latitud = b.getString(getString(R.string.INTENT_LATITUD));
            final String longitud = b.getString(getString(R.string.INTENT_LONGITUD));

            //Conversion de latitud a dms
            double lat_ubicacion = Double.parseDouble(Objects.requireNonNull(latitud));
            if (lat_ubicacion < 0) {
                dms_lat = getString(R.string.coordenadas_sur);
            } else {
                dms_lat = getString(R.string.coordenadas_norte);
            }

            //Calculo de lat to GMS
            Map<String, Double> lat_dms = QuakeUtils.toDMS(lat_ubicacion);
            Double lat_grados_dsm = lat_dms.get("grados");
            Double lat_minutos_dsm = lat_dms.get("minutos");
            Double lat_segundos_dsm = lat_dms.get("segundos");
            dms_lat = String.format(Locale.US, "%.1f° %.1f' %.1f'' %s", lat_grados_dsm, lat_minutos_dsm, lat_segundos_dsm, dms_lat);

            double long_ubicacion = Double.parseDouble(Objects.requireNonNull(longitud));
            if (long_ubicacion < 0) {
                dms_long = getString(R.string.coordenadas_oeste);
            } else {
                dms_long = getString(R.string.coordenadas_este);
            }

            //Calculo de long to GMS
            Map<String, Double> long_dms = QuakeUtils.toDMS(long_ubicacion);
            Double long_grados_dsm = long_dms.get("grados");
            Double long_minutos_dsm = long_dms.get("minutos");
            Double long_segundos_dsm = long_dms.get("segundos");
            dms_long = String.format(Locale.US, "%.1f° %.1f' %.1f'' %s", long_grados_dsm, long_minutos_dsm, long_segundos_dsm, dms_long);


            /*
                SECCION CONVERSION DE TIEMPO UTC-LOCAL
             */
            tiempos = null;
            //Si el bundle viene del adapter, usar directamente TIME LOCAL
            fecha_local = b.getString(getString(R.string.INTENT_FECHA_LOCAL));
            if (fecha_local != null) {
                SimpleDateFormat format = new SimpleDateFormat(getString(R.string.DATETIME_FORMAT), Locale.US);
                Date local_date = null;
                try {
                    local_date = format.parse(fecha_local);

                } catch (ParseException e) {
                    e.printStackTrace();
                }

                tiempos = QuakeUtils.timeToText(local_date);
                Log.d("ADAPTER", "LOCAL: " + format.format(local_date));
            }

            //Si el bundle viene de notificacion, transformar UTC a TIME LOCAL
            String fecha_utc = b.getString(getString(R.string.INTENT_FECHA_UTC));
            if (fecha_utc != null) {
                SimpleDateFormat format = new SimpleDateFormat(getString(R.string.DATETIME_FORMAT_SLASH), Locale.US);
                format.setTimeZone(TimeZone.getDefault());

                Date utc_date = null;
                try {
                    utc_date = format.parse(fecha_utc);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                Date local_date = QuakeUtils.utcToLocal(Objects.requireNonNull(utc_date));
                fecha_local = format.format(local_date);
                tiempos = QuakeUtils.timeToText(local_date);

                Log.d("NOTIFICATION", "UTC: " + fecha_utc + "- LOCAL: " + fecha_local);
            }

            /*
                SECCION DE SETEO DE TEXTVIEWS
             */
            setTextViews();

            
            /*
                SECCION DE FLOATING BUTTONS
             */


            fab_share = findViewById(R.id.fab_share);
            fab_fb = findViewById(R.id.fab_fb);
            fab_whatsapp = findViewById(R.id.fab_wsp);
            fab_gmail = findViewById(R.id.fab_gmail);

            fab_share.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    overlay = findViewById(R.id.quake_details_container);

                    if (!isFABOpen) {
                        showFabMenu();

                    } else {
                        closeFabMenu();
                    }
                }
            });


            fab_fb.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = getPackageManager().getLaunchIntentForPackage(getString(R.string.PACKAGE_NAME_FB));

                    //Si no existe el paquete
                    if (intent == null) {
                        doInstallation(getString(R.string.PACKAGE_NAME_FB));
                    } else {

                        //Si esta instalada hacer share
                        Log.d(getString(R.string.TAG_INTENT_SHARE), getString(R.string.TAG_INTENT_SHARE_FB));
                        Crashlytics.log(Log.DEBUG, getString(R.string.TAG_INTENT_SHARE), getString(R.string.TAG_INTENT_SHARE_FB));

                        callbackManager = CallbackManager.Factory.create();
                        shareDialog = new ShareDialog(QuakeDetailsActivity.this);


                        //Share foto del sismo
                        sharePhoto = new SharePhoto.Builder()
                                .setBitmap(bitmapFB)
                                .build();

                        sharePhotoContent = new SharePhotoContent.Builder()
                                .addPhoto(sharePhoto)
                                .setShareHashtag(new ShareHashtag.Builder()
                                        .setHashtag("#SismoChile")
                                        .build())
                                .build();

                        shareDialog.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>() {
                            @Override
                            public void onSuccess(Sharer.Result result) {
                                Toast.makeText(getApplicationContext(), getString(R.string.TAG_TOAST_SHARE_FB_OK), Toast.LENGTH_LONG).show();
                                Log.d(getString(R.string.TAG_INTENT_SHARE_FB_LOG), getString(R.string.TAG_INTENT_SHARE_FB_OK_MESSAGE));
                                Crashlytics.log(Log.DEBUG, getString(R.string.TAG_INTENT_SHARE_FB_LOG), getString(R.string.TAG_INTENT_SHARE_FB_OK_MESSAGE));
                            }

                            @Override
                            public void onCancel() {
                                Toast.makeText(getApplicationContext(), getString(R.string.TAG_TOAST_SHARE_FB_CANCEL), Toast.LENGTH_LONG).show();
                                Log.d(getString(R.string.TAG_INTENT_SHARE_FB_LOG), getString(R.string.TAG_INTENT_SHARE_FB_CANCEL_MESSAGE));
                                Crashlytics.log(Log.DEBUG, getString(R.string.TAG_INTENT_SHARE_FB_LOG), getString(R.string.TAG_INTENT_SHARE_FB_CANCEL_MESSAGE));
                            }

                            @Override
                            public void onError(FacebookException error) {
                                Toast.makeText(getApplicationContext(), getString(R.string.TAG_TOAST_SHARE_FB_ERROR), Toast.LENGTH_LONG).show();
                                Log.d(getString(R.string.TAG_INTENT_SHARE_FB_LOG), getString(R.string.TAG_INTENT_SHARE_FB_ERROR_MESSAGE) + "-" + error);
                                Crashlytics.log(Log.DEBUG, getString(R.string.TAG_INTENT_SHARE_FB_LOG), getString(R.string.TAG_INTENT_SHARE_FB_ERROR_MESSAGE) + "-" + error);
                            }
                        });

                        if (ShareDialog.canShow(SharePhotoContent.class)) {
                            shareDialog.show(sharePhotoContent);
                            Log.d(getString(R.string.TAG_INTENT_SHARE_DIALOG), getString(R.string.TAG_INTENT_SHARE_DIALOG_MESSAGE));
                            Crashlytics.log(Log.DEBUG, getString(R.string.TAG_INTENT_SHARE_DIALOG), getString(R.string.TAG_INTENT_SHARE_DIALOG_MESSAGE));
                        }
                    }
                }
            });

            fab_whatsapp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = getPackageManager().getLaunchIntentForPackage(getString(R.string.PACKAGE_NAME_WSP));

                    //Si no existe el paquete
                    if (intent == null) {
                        doInstallation(getString(R.string.PACKAGE_NAME_WSP));
                    } else {
                        Log.d(getString(R.string.TAG_INTENT_SHARE), getString(R.string.TAG_INTENT_SHARE_WSP));
                        Crashlytics.log(Log.DEBUG, getString(R.string.TAG_INTENT_SHARE), getString(R.string.TAG_INTENT_SHARE_WSP));

                        Intent wspIntent = new Intent();
                        wspIntent.setAction(Intent.ACTION_SEND);
                        wspIntent.setPackage(getString(R.string.PACKAGE_NAME_WSP));
                        wspIntent.putExtra(Intent.EXTRA_TEXT, String.format(Locale.US,
                                "[Alerta sísmica]\n\n" +
                                        "Información sismológica\n" +
                                        "Ciudad: %1$s\n" +
                                        "Hora Local: %2$s\n" +
                                        "Magnitud: %3$.1f %4$s\n" +
                                        "Profundidad: %5$.1f Km\n" +
                                        "Georeferencia: %6$s\n\n" +
                                        "Para más información descarga la app LastQuakeChile aquí\n" +
                                        "%7$s"
                                , ciudad, fecha_local, magnitud, escala, profundidad, referencia, getString(R.string.DEEP_LINK)
                        ));
                        wspIntent.putExtra(Intent.EXTRA_STREAM, bitmapUri);
                        wspIntent.setType("image/*");
                        //wspIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                        startActivity(wspIntent);
                    }
                }
            });

            fab_gmail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = getPackageManager().getLaunchIntentForPackage(getString(R.string.PACKAGE_NAME_GMAIL));

                    //Si no existe el paquete
                    if (intent == null) {
                        doInstallation(getString(R.string.PACKAGE_NAME_GMAIL));
                    } else {

                        Log.d(getString(R.string.TAG_INTENT_SHARE), getString(R.string.TAG_INTENT_SHARE_GM));
                        Crashlytics.log(Log.DEBUG, getString(R.string.TAG_INTENT_SHARE), getString(R.string.TAG_INTENT_SHARE_GM));

                        Intent gmIntent = new Intent();
                        gmIntent.setAction(Intent.ACTION_SEND);
                        gmIntent.setPackage(getString(R.string.PACKAGE_NAME_GMAIL));
                        gmIntent.putExtra(Intent.EXTRA_SUBJECT, String.format(Locale.US, "[Alerta sísmica] - %1$.1f Richter en %2$s", magnitud, ciudad));
                        gmIntent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(
                                String.format(Locale.US,
                                        "<h3>\n" +
                                                "  Información sismológica\n" +
                                                "</h3>\n" +
                                                "\n" +
                                                "<table>\n" +
                                                "  <tr><td>Hora Local: </td><td>%1$s</td></tr><br>\n" +
                                                "  <tr><td>Ciudad: </td><td>%2$s</td></tr><br>\n" +
                                                "  <tr><td>Magnitud: </td><td>%3$.1f %4$s</td></tr><br>\n" +
                                                "  <tr><td>Profundidad: </td><td>%5$.1f Km</td></tr><br>\n" +
                                                "  <tr><td>Georeferencia: </td><td>%6$s</td></tr><br>\n" +
                                                "  <tr><td>Latitud: </td><td>%7$s</td></tr><br>\n" +
                                                "  <tr><td>Longitud: </td><td>%8$s</td></tr><br>\n" +
                                                "  <tr><td>Posicion GMS: </td><td>%9$s - %10$s</td></tr><br>\n" +
                                                " \n" +
                                                "</table>\n" +
                                                "\n" +
                                                "<h5>\n" +
                                                "  Para más información descarga la app LastQuakeChile aquí %11$s \n" +
                                                "</h5>"
                                        , fecha_local, ciudad, magnitud, escala, profundidad, referencia, latitud, longitud, dms_lat, dms_long, getString(R.string.DEEP_LINK))));

                        gmIntent.putExtra(Intent.EXTRA_STREAM, bitmapUri);
                        gmIntent.setType("image/*");

                        startActivity(gmIntent);
                    }
                }
            });
        }
    }


    /**
     * Funcion que abre el floating button menu
     */
    private void showFabMenu() {
        isFABOpen = true;

        fab_fb.show();
        fab_whatsapp.show();
        fab_gmail.show();

        fab_text_fb = findViewById(R.id.fab_text_fb);
        fab_text_wsp = findViewById(R.id.fab_text_wsp);
        fab_text_gm = findViewById(R.id.fab_text_gm);

        //Seteado de text en alpha 0 y visible
        fab_text_fb.setAlpha(0f);
        fab_text_fb.setVisibility(View.VISIBLE);
        fab_text_wsp.setAlpha(0f);
        fab_text_wsp.setVisibility(View.VISIBLE);
        fab_text_gm.setAlpha(0f);
        fab_text_gm.setVisibility(View.VISIBLE);

        //trasnlaciones de fabs y textos
        fab_fb.animate().translationY(-getResources().getDimension(R.dimen.standard_65));
        fab_whatsapp.animate().translationY(-getResources().getDimension(R.dimen.standard_130));
        fab_gmail.animate().translationY(-getResources().getDimension(R.dimen.standard_195));
        fab_text_fb.animate().translationY(-getResources().getDimension(R.dimen.standard_65));
        fab_text_wsp.animate().translationY(-getResources().getDimension(R.dimen.standard_130));
        fab_text_gm.animate().translationY(-getResources().getDimension(R.dimen.standard_195));

        //Animacion de alpha para textos
        fab_text_wsp.animate().alpha(1.0f).setDuration(500);
        fab_text_fb.animate().alpha(1.0f).setDuration(500);
        fab_text_gm.animate().alpha(1.0f).setDuration(500);

        overlay.setAlpha(0f);
        overlay.setVisibility(View.VISIBLE);
        overlay.animate().alpha(0.85f).setDuration(500);


    }

    /**
     * Funcion que cierra el floating button menu
     */
    private void closeFabMenu() {
        isFABOpen = false;
        fab_fb.animate().translationY(0);
        fab_whatsapp.animate().translationY(0);
        fab_gmail.animate().translationY(0);

        fab_gmail.hide();
        fab_whatsapp.hide();
        fab_fb.hide();

        fab_text_fb.animate().translationY(0);
        fab_text_fb.setVisibility(View.GONE);
        fab_text_gm.animate().translationY(0);
        fab_text_gm.setVisibility(View.GONE);
        fab_text_wsp.animate().translationY(0);
        fab_text_wsp.setVisibility(View.GONE);

        //Animacion de alpha para textos
        fab_text_wsp.animate().alpha(0.0f).setDuration(500);
        fab_text_fb.animate().alpha(0.0f).setDuration(500);
        fab_text_gm.animate().alpha(0.0f).setDuration(500);

        //Animacion CLOSE de overlay
        overlay.setAlpha(0.85f);
        overlay.animate().alpha(0.0f).setDuration(500).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation, boolean isReverse) {
                overlay.setVisibility(View.GONE);
            }
        });

    }

    @Override
    public void onBackPressed() {

        if (!isFABOpen) {
            super.onBackPressed();
        } else {
            closeFabMenu();
        }
    }

    /*
        Funcion que permite el callback de facebook
    */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }


    /**
     * Funcion que permite setear los textview del detalle con la información procesada
     */
    private void setTextViews() {
        //Setear titulo de ciudad en activity
        Objects.requireNonNull(getSupportActionBar()).setTitle(ciudad);

        //Setear nombre ciudad
        tv_ciudad.setText(ciudad);

        //Setear referencia
        tv_referencia.setText(referencia);

        //Setear magnitud en en circulo de color
        tv_magnitud.setText(String.format(getString(R.string.magnitud), magnitud));

        //Setear el color de background dependiendo de magnitud del sismo
        iv_mag_color.setColorFilter(getColor(QuakeUtils.getMagnitudeColor(magnitud)));

        //Setear profundidad
        tv_profundidad.setText(String.format(Locale.US, getString(R.string.quake_details_profundidad), profundidad));

        //Setear fecha
        tv_fecha.setText(fecha_local);

        //Setear posicionamiento
        tv_gms.setText(String.format(getString(R.string.format_coordenadas), dms_lat, dms_long));

        /*
            SECCION ESTADO
         */
        if (estado.equals("preliminar")) {
            tv_estado.setText(String.format(Locale.US, "Sismo %s", estado));
            iv_estado.setImageDrawable(getDrawable(R.drawable.ic_progress_check_24));
        } else if (estado.equals("verificado")) {
            tv_estado.setText(String.format(Locale.US, "Sismo %s", estado));
            iv_estado.setImageDrawable(getDrawable(R.drawable.ic_baseline_check_circle_24px));
        }

        /*
            SECCION HORA
         */

        if (tiempos != null) {

            Long dias = tiempos.get(getString(R.string.UTILS_TIEMPO_DIAS));
            Long minutos = tiempos.get(getString(R.string.UTILS_TIEMPO_MINUTOS));
            Long horas = tiempos.get(getString(R.string.UTILS_TIEMPO_HORAS));
            Long segundos = tiempos.get(getString(R.string.UTILS_TIEMPO_SEGUNDOS));

            //Condiciones días.
            if (dias != null && dias == 0) {

                if (horas != null && horas >= 1) {
                    tv_hora.setText(String.format(getString(R.string.quake_time_hour), horas));
                } else {
                    tv_hora.setText(String.format(getString(R.string.quake_time_minute), minutos));

                    if (minutos != null && minutos < 1) {
                        tv_hora.setText(String.format(getString(R.string.quake_time_second), segundos));
                    }
                }
            } else if (dias != null && dias > 0) {

                if (horas != null && horas == 0) {
                    tv_hora.setText(String.format(getString(R.string.quake_time_day), dias));
                } else if (horas != null && horas >= 1) {
                    tv_hora.setText(String.format(getString(R.string.quake_time_day_hour), dias, horas / 24));
                }
            }
        }


        /*
            Seccion Tipo Escala
         */
        if (escala != null) {

            switch (escala) {
                case "Ml":
                    tv_escala.setText(String.format(getString(R.string.quake_details_escala), getString(R.string.quake_details_magnitud_local)));
                    break;

                case "Mw":
                    tv_escala.setText(String.format(getString(R.string.quake_details_escala), getString(R.string.quake_details_magnitud_momento)));
                    break;

            }
        }

        /*
            Seccion Sismo sensible
         */
        if (sensible) {
            iv_sensible.setVisibility(View.VISIBLE);
        }

        /*
            SECCION IMAGEN MAPA
         */
        final Uri uri = Uri.parse(foto_url);
        Glide.with(this)
                .load(uri)
                .apply(
                        new RequestOptions()
                                .placeholder(R.drawable.placeholder)
                                .error(R.drawable.not_found)
                )
                .transition(withCrossFade())
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        iv_mapa.setImageDrawable(getDrawable(R.drawable.not_found));
                        Log.d(getString(R.string.TAG_INTENT_SHARE_BITMAP), getString(R.string.TAG_INTENT_SHARE_BITMAP_MESSAGE_FAIL));
                        Crashlytics.log(Log.DEBUG, getString(R.string.TAG_INTENT_SHARE_BITMAP), getString(R.string.TAG_INTENT_SHARE_BITMAP_MESSAGE_FAIL));
                        return false;
                    }

                    //No es necesario usarlo (If u want)
                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        bitmapUri = QuakeUtils.getLocalBitmapUri(resource, getApplicationContext());
                        bitmapFB = ((BitmapDrawable) resource).getBitmap();
                        Log.d(getString(R.string.TAG_INTENT_SHARE_BITMAP), getString(R.string.TAG_INTENT_SHARE_BITMAP_MESSAGE));
                        Crashlytics.log(Log.DEBUG, getString(R.string.TAG_INTENT_SHARE_BITMAP), getString(R.string.TAG_INTENT_SHARE_BITMAP_MESSAGE));
                        return false;
                    }
                })
                .into(iv_mapa);


    }


    private void doInstallation(String packageName) {

        try {
            //Abrir app en google play
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.setData(Uri.parse("market://details?id=" + packageName));

            //LOG
            Log.d(getString(R.string.TAG_INTENT), getString(R.string.TAG_INTENT_GOOGLEPLAY_LK));
            Crashlytics.log(Log.DEBUG, getString(R.string.TAG_INTENT), getString(R.string.TAG_INTENT_GOOGLEPLAY_LK));

            startActivity(intent);
        } catch (android.content.ActivityNotFoundException anfe) {

            //Si gogle play no esta abre webview
            Log.d(getString(R.string.TAG_INTENT), getString(R.string.TAG_INTENT_NAVEGADOR_LK));
            Crashlytics.log(Log.DEBUG, getString(R.string.TAG_INTENT), getString(R.string.TAG_INTENT_NAVEGADOR_LK));
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + packageName)));
        }
    }

}

