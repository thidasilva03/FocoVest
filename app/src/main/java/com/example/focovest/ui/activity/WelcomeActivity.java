package com.example.focovest.ui.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.focovest.R;
import com.example.focovest.util.EdgeToEdgeHelper;

/**
 * Tela de boas-vindas exibida na primeira abertura do aplicativo antes da definição de objetivos.
 */
public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdgeHelper.enable(this, true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        EdgeToEdgeHelper.applySystemBarInsets(findViewById(R.id.welcomeRoot), true);

        findViewById(R.id.btnStartWelcome).setOnClickListener(v -> {
            startActivity(new Intent(this, OnboardingActivity.class));
            finish();
        });
    }
}
