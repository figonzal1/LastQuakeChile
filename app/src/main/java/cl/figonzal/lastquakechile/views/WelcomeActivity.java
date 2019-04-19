package cl.figonzal.lastquakechile.views;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.appinvite.FirebaseAppInvite;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;

import cl.figonzal.lastquakechile.R;

public class WelcomeActivity extends AppCompatActivity {

	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_invitation);

		ImageView mIvIcon = findViewById(R.id.iv_icon_app);
		TextView mTvApp = findViewById(R.id.tv_app_name);
		Button mBtnWelcome = findViewById(R.id.btn_welcome);
		ImageView mIvWaves = findViewById(R.id.iv_waves);

		//Setear las animaciones
		Animation mPulse = AnimationUtils.loadAnimation(this, R.anim.anim_pulse);
		Animation mAnimText = AnimationUtils.loadAnimation(this, R.anim.anim_fade);
		Animation mAnimButton = AnimationUtils.loadAnimation(this, R.anim.anim_button_welcome);
		Animation mAnimWaves = AnimationUtils.loadAnimation(this, R.anim.anim_waves);

		//iniciar las animaciones de icono, texto y boton.
		mIvIcon.startAnimation(mPulse);
		mTvApp.startAnimation(mAnimText);
		mBtnWelcome.startAnimation(mAnimButton);
		mIvWaves.startAnimation(mAnimWaves);

		//Boton para iniciar la app desde invitacion
		mBtnWelcome.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick (View v) {
				Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
				intent.putExtra(getString(R.string.desde_deep_link), true);
				startActivity(intent);
				finish();
			}
		});

		//Recibir invitaciones y deep links
		FirebaseDynamicLinks.getInstance()
				.getDynamicLink(getIntent())
				.addOnSuccessListener(this, new OnSuccessListener<PendingDynamicLinkData>() {
					@Override
					public void onSuccess (PendingDynamicLinkData data) {
						if (data == null) {
							Log.d(getString(R.string.TAG_DEEP_LINK_INVITATION),
									getString(R.string.INVITATION_STATUS));
						} else {
							// Get the deep link
							Uri deepLink = data.getLink();
							Log.d(getString(R.string.TAG_DEEP_LINK_DATA),
									String.valueOf(deepLink));

							// Extract invite
							FirebaseAppInvite invite = FirebaseAppInvite.getInvitation(data);
							if (invite != null) {
								String invitationId = invite.getInvitationId();
								Log.d(getString(R.string.TAG_INVITATION_ID), invitationId);
							}
						}
					}
				})
				.addOnFailureListener(this, new OnFailureListener() {
					@Override
					public void onFailure (@NonNull Exception e) {
						Log.w(getString(R.string.TAG_INVITATION_RECEIVE), "getDynamicLink" +
								":onFailure", e);
					}
				});
	}

	@Override
	public void onWindowFocusChanged (boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus) {
			hideSystemUI();
		}
	}

	/**
	 * Funcion que permite poner la actividad en panatalla completa
	 */
	private void hideSystemUI () {

		View mView = getWindow().getDecorView();

		mView.setSystemUiVisibility(
				View.SYSTEM_UI_FLAG_IMMERSIVE
						// Set the content to appear under the system bars so that the
						// content doesn't resize when the system bars hide and show.
						| View.SYSTEM_UI_FLAG_LAYOUT_STABLE
						| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
						| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
						// Hide the nav bar and status bar
						| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
						| View.SYSTEM_UI_FLAG_FULLSCREEN);
	}
}
