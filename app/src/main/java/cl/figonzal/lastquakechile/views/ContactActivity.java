package cl.figonzal.lastquakechile.views;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.crashlytics.android.Crashlytics;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import java.util.Objects;

import cl.figonzal.lastquakechile.R;
import cl.figonzal.lastquakechile.services.QuakeUtils;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class ContactActivity extends AppCompatActivity {

	private Intent mIntent;

	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contact);

		//Sete de toolbar
		Toolbar mToolbar = findViewById(R.id.tool_bar_contact);
		setSupportActionBar(mToolbar);

		Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle(R.string.contact);

		//Setear collapsing toolbar con titulo estatico superior y animacion de colores al recoger
		// toolbar
		final CollapsingToolbarLayout mCollapsingToolbarLayout =
				findViewById(R.id.collapsing_toolbar_contact);
		mCollapsingToolbarLayout.setTitleEnabled(true);
		mCollapsingToolbarLayout.setContentScrimColor(getResources().getColor(R.color.colorPrimary
				, getTheme()));

		//Cargar imagen toolbar
		loadImageToolbar();

		ImageButton mIbFacebook = findViewById(R.id.ib_facebook);
		ImageButton mIbLinkedin = findViewById(R.id.ib_linkedin);

		mIbFacebook.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick (View v) {

				mIntent =
						getPackageManager().getLaunchIntentForPackage(getString(R.string.PACKAGE_NAME_FB));
				if (mIntent == null) {
					QuakeUtils.doInstallation(getString(R.string.PACKAGE_NAME_FB),
							getApplicationContext());
				} else {

					Log.d(getString(R.string.TAG_INTENT),
							getString(R.string.TAG_INTENT_INSTALADA));
					Crashlytics.log(Log.DEBUG, getString(R.string.TAG_INTENT),
							getString(R.string.TAG_INTENT_INSTALADA));
					startActivity(mIntent);
				}

			}
		});

		mIbLinkedin.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick (View v) {

				mIntent =
						getPackageManager().getLaunchIntentForPackage(getString(R.string.PACKAGE_NAME_LINKEDIN));
				if (mIntent == null) {
					QuakeUtils.doInstallation(getString(R.string.PACKAGE_NAME_LINKEDIN),
							getApplicationContext());
				} else {

					//LOG
					Log.d(getString(R.string.TAG_INTENT),
							getString(R.string.TAG_INTENT_INSTALADA));
					Crashlytics.log(Log.DEBUG, getString(R.string.TAG_INTENT),
							getString(R.string.TAG_INTENT_INSTALADA));
					startActivity(mIntent);
				}


			}
		});
	}

	/**
	 * Funcion para cargar la imagen del toolbar en el Image View
	 */
	private void loadImageToolbar () {
		final ImageView mIvFoto = findViewById(R.id.toolbar_image_contact);
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
					public boolean onLoadFailed (@Nullable GlideException e, Object model,
					                             Target<Drawable> target,
					                             boolean isFirstResource) {
						mIvFoto.setImageDrawable(getDrawable(R.drawable.not_found));
						return false;
					}

					//No es necesario usarlo (If u want)
					@Override
					public boolean onResourceReady (Drawable resource, Object model,
					                                Target<Drawable> target,
					                                DataSource dataSource,
					                                boolean isFirstResource) {
						return false;
					}
				})
				.into(mIvFoto);
	}

}
