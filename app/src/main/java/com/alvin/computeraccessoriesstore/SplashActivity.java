package com.alvin.computeraccessoriesstore;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SplashActivity extends AppCompatActivity {

    @BindView(R.id.logoApp)
    ImageView logoApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ButterKnife.bind(this);

//        new Handler().postDelayed(() -> {
//            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
//            finish();
//        }, 2000);

        splashAnimation();
    }

    private void splashAnimation() {
//        Animation animation = AnimationUtils.loadAnimation(this, R.anim.top_to_bot);
//        logoApp.startAnimation(animation);


        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        logoApp.startAnimation(fadeIn);

        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    sleep(3000);
                    startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                    finish();

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                super.run();
            }
        };
        t.start();
    }
}
