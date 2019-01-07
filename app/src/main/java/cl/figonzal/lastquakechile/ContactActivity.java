package cl.figonzal.lastquakechile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageButton;

import java.util.Objects;

public class ContactActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        //Sete de toolbar
        Toolbar toolbar = findViewById(R.id.tool_bar_contact);
        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Felipe González");

        //Setear collapsing toolbar con titulo estatico superior y animacion de colores al recoger toolbar
        final CollapsingToolbarLayout collapsingToolbarLayout = findViewById(R.id.collapsing_toolbar_contact);
        collapsingToolbarLayout.setTitleEnabled(true);
        collapsingToolbarLayout.setContentScrimColor(getResources().getColor(R.color.colorPrimary, getTheme()));

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
                        startActivity(intent);
                    } catch (android.content.ActivityNotFoundException anfe) {
                        //Si gogle play no esta abre webview
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + "com.facebook.katana")));
                    }
                } else {
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
                        startActivity(intent);
                    } catch (android.content.ActivityNotFoundException anfe) {
                        //Si gogle play no esta abre webview
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + "com.linkedin.android")));
                    }
                } else {
                    startActivity(intent);
                }


            }
        });
    }
}