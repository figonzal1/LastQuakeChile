package cl.figonzal.lastquakechile;

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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.appinvite.FirebaseAppInvite;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invitation);

        ImageButton iv_icon = findViewById(R.id.iv_icon_app);
        TextView tv_app = findViewById(R.id.tv_app_name);
        Button btn_welcome = findViewById(R.id.btn_welcome);
        ImageView iv_waves = findViewById(R.id.iv_waves);

        //Setear las animaciones
        Animation pulse = AnimationUtils.loadAnimation(this, R.anim.anim_pulse);
        Animation anim_text = AnimationUtils.loadAnimation(this, R.anim.anim_fade);
        Animation anim_button = AnimationUtils.loadAnimation(this, R.anim.anim_button_welcome);
        Animation anim_waves = AnimationUtils.loadAnimation(this, R.anim.anim_waves);

        //iniciar las animaciones de icono, texto y boton.
        iv_icon.startAnimation(pulse);
        tv_app.startAnimation(anim_text);
        btn_welcome.startAnimation(anim_button);
        iv_waves.startAnimation(anim_waves);

        //Boton para iniciar la app desde invitacion
        btn_welcome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
                //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });

        //Recibir invitaciones y deep links
        FirebaseDynamicLinks.getInstance()
                .getDynamicLink(getIntent())
                .addOnSuccessListener(this, new OnSuccessListener<PendingDynamicLinkData>() {
                    @Override
                    public void onSuccess(PendingDynamicLinkData data) {
                        if (data == null) {
                            Log.d("INTENT_INVITATCON", "Invitacion sin datos");
                        } else {
                            // Get the deep link
                            Uri deepLink = data.getLink();
                            Log.d("DEEP_LINK", String.valueOf(deepLink));

                            // Extract invite
                            FirebaseAppInvite invite = FirebaseAppInvite.getInvitation(data);
                            if (invite != null) {
                                String invitationId = invite.getInvitationId();
                                Log.d("INVITACION_ID", invitationId);
                            }
                        }
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("INVITACION", "getDynamicLink:onFailure", e);
                    }
                });
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }

    /**
     * Funcion que permite poner la actividad en panatalla completa
     */
    private void hideSystemUI() {

        View view = getWindow().getDecorView();

        view.setSystemUiVisibility(
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
