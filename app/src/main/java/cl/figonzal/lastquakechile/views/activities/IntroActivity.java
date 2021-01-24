package cl.figonzal.lastquakechile.views.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;

import cl.figonzal.lastquakechile.R;
import cl.figonzal.lastquakechile.services.SharedPrefService;
import timber.log.Timber;

public class IntroActivity extends AppCompatActivity {


    private Button btnGetStarted;
    private ImageView ivIcon;
    private TextView tvApp;
    private ImageView ivWaves;
    private Animation pulse, animText, animButton, animWaves;

    private SharedPrefService sharedPrefService;

    public IntroActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //FULLSCREEN
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_intro);

        sharedPrefService = new SharedPrefService(getApplicationContext());

        checkFirstLoad();
    }

    private void manageDynamicLink() {

        //Recibir invitaciones y deep links
        FirebaseDynamicLinks.getInstance()
                .getDynamicLink(getIntent())
                .addOnSuccessListener(this, data -> {

                    if (data == null) {

                        Timber.i(getString(R.string.INVITATION_STATUS));

                    } else {
                        // Get the deep link
                        Uri deepLink = data.getLink();

                        //LINK https
                        Timber.i(getString(R.string.TAG_DEEP_LINK_DATA) + ": " + deepLink);

                    }
                })
                .addOnFailureListener(this, e -> Timber.e(e, "getDynamicLink:onFailure"));
    }

    private void initResources() {

        ivIcon = findViewById(R.id.iv_icon_app);
        tvApp = findViewById(R.id.tv_app_name);
        btnGetStarted = findViewById(R.id.btn_welcome);
        ivWaves = findViewById(R.id.iv_waves);

        //Config Animations
        pulse = AnimationUtils.loadAnimation(this, R.anim.anim_pulse);

        animText = AnimationUtils.loadAnimation(this, R.anim.anim_textview);
        animText.setStartOffset(900);

        animButton = AnimationUtils.loadAnimation(this, R.anim.anim_button_welcome);
        animButton.setStartOffset(1300);

        animWaves = AnimationUtils.loadAnimation(this, R.anim.anim_waves);

        //GET STARTED BUTTON
        btnGetStarted.setOnClickListener(v -> {

            sharedPrefService.saveData(getString(R.string.SHARED_PREF_FIRST_LOAD), false);

            Intent intent = new Intent(IntroActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void startAnimations() {

        //iniciar las animaciones de icono, texto y boton.
        ivIcon.startAnimation(pulse);
        tvApp.startAnimation(animText);
        ivWaves.startAnimation(animWaves);
        btnGetStarted.startAnimation(animButton);
    }

    private void checkFirstLoad() {

        boolean isFirstLoad = (boolean) sharedPrefService.getData(getString(R.string.SHARED_PREF_FIRST_LOAD), false);

        Timber.i(getString(R.string.SHARED_PREF_FIRST_LOAD) + ": " + isFirstLoad);

        if (!isFirstLoad) {

            Timber.i("Abrir main activity directo");

            Intent intent = new Intent(IntroActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        } else {

            Timber.i("Abrir intro activity");

            initResources();

            startAnimations();

            manageDynamicLink();
        }

    }
}
