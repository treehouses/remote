package io.treehouses.remote;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

public class SplashScreenActivity extends AppCompatActivity {

    private static final int SPLASH_TIME_OUT = 2000;
    Animation logoAnimation, textAnimation;
    ImageView logo;
    TextView logoText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        logo = findViewById(R.id.splash_logo);
        logoAnimation = AnimationUtils.loadAnimation(this, R.anim.splash_logo_anim);
        logo.setAnimation(logoAnimation);

        logoText = findViewById(R.id.logo_text);
        textAnimation = AnimationUtils.loadAnimation(this, R.anim.splash_text_anim);
        logoText.setAnimation(textAnimation);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashScreenActivity.this, InitialActivity.class);
                startActivity(intent);
                finish();
            }
        }, SPLASH_TIME_OUT);
    }
}
