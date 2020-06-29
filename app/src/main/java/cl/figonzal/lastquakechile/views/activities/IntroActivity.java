package cl.figonzal.lastquakechile.views.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.appinvite.FirebaseAppInvite;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;

import cl.figonzal.lastquakechile.R;

public class IntroActivity extends AppCompatActivity {


    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Button btnGetStarted;
    private ImageView ivIcon;
    private TextView tvApp;
    private ImageView ivWaves;
    private Animation pulse, animText, animButton, animWaves;

    private FirebaseCrashlytics crashlytics;

    public IntroActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //FULLSCREEN
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_intro);

        initResources();

        startAnimations();

        checkFirstLoad();

        manageDynamicLink();

    }

    private void manageDynamicLink() {

        //Recibir invitaciones y deep links
        FirebaseDynamicLinks.getInstance()
                .getDynamicLink(getIntent())
                .addOnSuccessListener(this, new OnSuccessListener<PendingDynamicLinkData>() {
                    @Override
                    public void onSuccess(PendingDynamicLinkData data) {
                        if (data == null) {

                            Log.d(getString(R.string.TAG_DEEP_LINK_INVITATION),
                                    getString(R.string.INVITATION_STATUS));

                            crashlytics.log(getString(R.string.TAG_DEEP_LINK_INVITATION) + getString(R.string.INVITATION_STATUS));

                        } else {
                            // Get the deep link
                            Uri deepLink = data.getLink();

                            //LINK https
                            Log.d(getString(R.string.TAG_DEEP_LINK_DATA), String.valueOf(deepLink));
                            crashlytics.log(getString(R.string.TAG_DEEP_LINK_DATA) + deepLink);

                            // Extract invite
                            FirebaseAppInvite invite = FirebaseAppInvite.getInvitation(data);
                            if (invite != null) {
                                String invitationId = invite.getInvitationId();

                                Log.d(getString(R.string.TAG_INVITATION_ID), invitationId);
                                crashlytics.log(getString(R.string.TAG_INVITATION_ID) + invitationId);
                            }
                        }
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(getString(R.string.TAG_INVITATION_RECEIVE), "getDynamicLink" +
                                ":onFailure", e);
                    }
                });
    }

    private void initResources() {

        crashlytics = FirebaseCrashlytics.getInstance();

        ivIcon = findViewById(R.id.iv_icon_app);
        tvApp = findViewById(R.id.tv_app_name);
        btnGetStarted = findViewById(R.id.btn_welcome);
        ivWaves = findViewById(R.id.iv_waves);

        //Setear las animaciones
        pulse = AnimationUtils.loadAnimation(this, R.anim.anim_pulse);

        animText = AnimationUtils.loadAnimation(this, R.anim.anim_textview);
        animText.setStartOffset(900);

        animButton = AnimationUtils.loadAnimation(this, R.anim.anim_button_welcome);
        animButton.setStartOffset(1300);

        animWaves = AnimationUtils.loadAnimation(this, R.anim.anim_waves);
    }

    private void startAnimations() {
        //iniciar las animaciones de icono, texto y boton.
        ivIcon.startAnimation(pulse);
        tvApp.startAnimation(animText);
        ivWaves.startAnimation(animWaves);
        btnGetStarted.startAnimation(animButton);
    }

    private void checkFirstLoad() {
        sharedPreferences = getSharedPreferences(getString(R.string.SHARED_PREF_MASTER_KEY), Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        boolean isFirstLoad = sharedPreferences.getBoolean(getString(R.string.SHARED_PREF_FIRST_LOAD), true);

        Log.d(getString(R.string.SHARED_PREF_FIRST_LOAD), String.valueOf(isFirstLoad));

        if (!isFirstLoad) {
            Intent intent = new Intent(IntroActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        //GET STARTED BUTTON
        btnGetStarted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putBoolean(getString(R.string.SHARED_PREF_FIRST_LOAD), false);
                editor.apply();

                Intent intent = new Intent(IntroActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
