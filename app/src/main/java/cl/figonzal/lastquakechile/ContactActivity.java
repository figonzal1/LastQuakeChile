package cl.figonzal.lastquakechile;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.crashlytics.android.Crashlytics;

import java.util.Objects;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class ContactActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        //Sete de toolbar
        Toolbar toolbar = findViewById(R.id.tool_bar_contact);
        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.toolbar_title);

        //Setear collapsing toolbar con titulo estatico superior y animacion de colores al recoger toolbar
        final CollapsingToolbarLayout collapsingToolbarLayout = findViewById(R.id.collapsing_toolbar_contact);
        collapsingToolbarLayout.setTitleEnabled(true);
        collapsingToolbarLayout.setContentScrimColor(getResources().getColor(R.color.colorPrimary, getTheme()));

        final ImageView iv_foto = findViewById(R.id.toolbar_image_contact);
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
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        iv_foto.setImageDrawable(getDrawable(R.drawable.not_found));
                        return false;
                    }

                    //No es necesario usarlo (If u want)
                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        return false;
                    }
                })
                .into(iv_foto);


        ImageButton ib_facebook = findViewById(R.id.ib_facebook);
        ImageButton ib_linkedin = findViewById(R.id.ib_linkedin);

        ib_facebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = getPackageManager().getLaunchIntentForPackage("com.facebook.katana");
                if (intent == null) {
                    try {
                        //Intenta abrir google play
                        intent = new Intent(Intent.ACTION_VIEW);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setData(Uri.parse("market://details?id=" + "com.facebook.katana"));

                        //LOG
                        Log.d(getString(R.string.TAG_INTENT), getString(R.string.TAG_INTENT_GOOGLEPLAY_FB));
                        Crashlytics.log(Log.DEBUG, getString(R.string.TAG_INTENT), getString(R.string.TAG_INTENT_GOOGLEPLAY_FB));

                        startActivity(intent);
                    } catch (android.content.ActivityNotFoundException anfe) {

                        //Si gogle play no esta abre webview
                        Log.d(getString(R.string.TAG_INTENT), getString(R.string.TAG_INTENT_NAVEGADOR_FB));
                        Crashlytics.log(Log.DEBUG, getString(R.string.TAG_INTENT), getString(R.string.TAG_INTENT_NAVEGADOR_FB));

                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + "com.facebook.katana")));
                    }
                } else {

                    Log.d(getString(R.string.TAG_INTENT), getString(R.string.TAG_INTENT_INSTALADA_FB));
                    Crashlytics.log(Log.DEBUG, getString(R.string.TAG_INTENT), getString(R.string.TAG_INTENT_INSTALADA_FB));
                    startActivity(intent);
                }

            }
        });

        ib_linkedin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = getPackageManager().getLaunchIntentForPackage("com.linkedin.android");
                if (intent == null) {
                    try {
                        //Intenta abrir google play
                        intent = new Intent(Intent.ACTION_VIEW);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setData(Uri.parse("market://details?id=" + "com.linkedin.android"));

                        //LOG
                        Log.d(getString(R.string.TAG_INTENT), getString(R.string.TAG_INTENT_GOOGLEPLAY_LK));
                        Crashlytics.log(Log.DEBUG, getString(R.string.TAG_INTENT), getString(R.string.TAG_INTENT_GOOGLEPLAY_LK));

                        startActivity(intent);
                    } catch (android.content.ActivityNotFoundException anfe) {

                        //Si gogle play no esta abre webview
                        Log.d(getString(R.string.TAG_INTENT), getString(R.string.TAG_INTENT_NAVEGADOR_LK));
                        Crashlytics.log(Log.DEBUG, getString(R.string.TAG_INTENT), getString(R.string.TAG_INTENT_NAVEGADOR_LK));
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + "com.linkedin.android")));
                    }
                } else {

                    //LOG
                    Log.d(getString(R.string.TAG_INTENT), getString(R.string.TAG_INTENT_INSTALADA_LK));
                    Crashlytics.log(Log.DEBUG, getString(R.string.TAG_INTENT), getString(R.string.TAG_INTENT_INSTALADA_LK));
                    startActivity(intent);
                }


            }
        });
    }
}
