package com.example.focovest.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.focovest.R;
import com.example.focovest.data.AuthSessionManager;
import com.example.focovest.data.StoreManager;
import com.example.focovest.data.remote.ApiCallback;
import com.example.focovest.data.remote.ApiClient;
import com.example.focovest.model.UserAccount;

import java.util.HashMap;
import java.util.Map;

/**
 * Tela de login do aluno.
 * Envia credenciais exclusivamente via POST JSON (nunca via GET na URL) quando há backend configurado,
 * com fallback seguro para validação local via hash SHA-256 em AuthSessionManager.
 */
public class LoginActivity extends AppCompatActivity {

    private EditText edtEmail;
    private EditText edtPassword;
    private TextView txtError;
    private Button btnSubmit;

    private static class LoginResponse {
        String status;
        String message;
        String token;
        UserAccount user;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        edtEmail = findViewById(R.id.edtLoginEmail);
        edtPassword = findViewById(R.id.edtLoginPassword);
        txtError = findViewById(R.id.txtLoginError);
        btnSubmit = findViewById(R.id.btnSubmitLogin);

        btnSubmit.setOnClickListener(v -> attemptLogin());

        findViewById(R.id.btnContinueOffline).setOnClickListener(v -> openAppDestination());

        findViewById(R.id.btnGoToRegister).setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });
    }

    private void attemptLogin() {
        txtError.setVisibility(View.GONE);
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString();

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError("Informe um e-mail válido para continuar.");
            return;
        }
        if (password.length() < 6) {
            showError("A senha deve ter pelo menos 6 caracteres.");
            return;
        }

        ApiClient api = ApiClient.getInstance();
        if (api.isRemoteConfigured()) {
            btnSubmit.setEnabled(false);
            Map<String, String> payload = new HashMap<>();
            payload.put("email", email);
            payload.put("password", password);

            api.postJson("/auth/login", payload, null, LoginResponse.class, new ApiCallback<LoginResponse>() {
                @Override
                public void onSuccess(LoginResponse data) {
                    if (isFinishing() || isDestroyed()) return;
                    btnSubmit.setEnabled(true);
                    if (data != null && "success".equals(data.status) && data.user != null && data.token != null) {
                        AuthSessionManager.getInstance(LoginActivity.this).saveSession(data.user, data.token);
                        syncProfileName(data.user);
                        openAppDestination();
                    } else {
                        String msg = (data != null && data.message != null) ? data.message : "Credenciais inválidas.";
                        showError(msg);
                    }
                }

                @Override
                public void onError(int httpCode, String message) {
                    if (isFinishing() || isDestroyed()) return;
                    btnSubmit.setEnabled(true);
                    showError(message != null ? message : "Erro ao conectar ao servidor.");
                }
            });
        } else {
            AuthSessionManager auth = AuthSessionManager.getInstance(this);
            if (auth.verifyLocalLogin(email, password)) {
                UserAccount acc = auth.getAccount();
                if (acc != null) syncProfileName(acc);
                Toast.makeText(this, "Bem-vindo(a) de volta!", Toast.LENGTH_SHORT).show();
                openAppDestination();
            } else {
                showError("Conta não encontrada ou senha incorreta. Crie uma conta ou use o modo local.");
            }
        }
    }

    private void syncProfileName(UserAccount acc) {
        StoreManager store = StoreManager.getInstance(this);
        if (store.getState().name == null || store.getState().name.trim().isEmpty()) {
            store.getState().name = acc.fullName;
            if (acc.city != null && !acc.city.isEmpty()) {
                store.getState().city = acc.city;
            }
            store.save();
        }
    }

    private void openAppDestination() {
        StoreManager store = StoreManager.getInstance(this);
        Intent intent = store.getState().profileCreated
                ? new Intent(this, MainActivity.class)
                : new Intent(this, OnboardingActivity.class);
        startActivity(intent);
        finish();
    }

    private void showError(String msg) {
        txtError.setText(msg);
        txtError.setVisibility(View.VISIBLE);
    }
}
