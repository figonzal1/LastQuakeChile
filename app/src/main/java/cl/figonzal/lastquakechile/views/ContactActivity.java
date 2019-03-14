package cl.figonzal.lastquakechile.views;

import android.content.Intent;
import android.graphics.drawable.Drawable;
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

import cl.figonzal.lastquakechile.R;
import cl.figonzal.lastquakechile.services.QuakeUtils;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class ContactActivity extends AppCompatActivity {

    private Intent intent;

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

        //Cargar imagen toolbar
        loadImageToolbar();

        ImageButton ib_facebook = findViewById(R.id.ib_facebook);
        ImageButton ib_linkedin = findViewById(R.id.ib_linkedin);

        ib_facebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                intent = getPackageManager().getLaunchIntentForPackage(getString(R.string.PACKAGE_NAME_FB));
                if (intent == null) {
                    QuakeUtils.doInstallation(getString(R.string.PACKAGE_NAME_FB), getApplicationContext());
                } else {

                    Log.d(getString(R.string.TAG_INTENT), getString(R.string.TAG_INTENT_INSTALADA));
                    Crashlytics.log(Log.DEBUG, getString(R.string.TAG_INTENT), getString(R.string.TAG_INTENT_INSTALADA));
                    startActivity(intent);
                }

            }
        });

        ib_linkedin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                intent = getPackageManager().getLaunchIntentForPackage(getString(R.string.PACKAGE_NAME_LINKEDIN));
                if (intent == null) {
                    QuakeUtils.doInstallation(getString(R.string.PACKAGE_NAME_LINKEDIN), getApplicationContext());
                } else {

                    //LOG
                    Log.d(getString(R.string.TAG_INTENT), getString(R.string.TAG_INTENT_INSTALADA));
                    Crashlytics.log(Log.DEBUG, getString(R.string.TAG_INTENT), getString(R.string.TAG_INTENT_INSTALADA));
                    startActivity(intent);
                }


            }
        });
    }

    /**
     * Funcion para cargar la imagen del toolbar en el Image View
     */
    private void loadImageToolbar() {
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
    }

}
