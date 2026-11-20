package com.example.focovest.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.focovest.R;
import com.example.focovest.data.AuthSessionManager;
import com.example.focovest.data.StoreManager;

/**
 * Tela de abertura (Splash) do FocoVest.
 * Remove callbacks no onDestroy() para evitar leaks e direciona conforme sessão e perfil criados.
 */
public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DELAY_MS = 1800L;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable navigateRunnable = this::navigateNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        handler.postDelayed(navigateRunnable, SPLASH_DELAY_MS);
    }

    private void navigateNext() {
        StoreManager store = StoreManager.getInstance(this);
        AuthSessionManager auth = AuthSessionManager.getInstance(this);

        Intent target;
        if (store.getState().profileCreated) {
            target = new Intent(this, MainActivity.class);
        } else if (auth.isLoggedIn()) {
            target = new Intent(this, OnboardingActivity.class);
        } else {
            target = new Intent(this, LoginActivity.class);
        }

        startActivity(target);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(navigateRunnable);
    }
}
