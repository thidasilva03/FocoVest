package com.example.focovest.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.focovest.R;
import com.example.focovest.data.AuthSessionManager;
import com.example.focovest.data.DataRepository;
import com.example.focovest.data.StoreManager;
import com.example.focovest.data.remote.ApiCallback;
import com.example.focovest.data.remote.ApiClient;
import com.example.focovest.model.UserAccount;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText edtFullName;
    private EditText edtUsername;
    private EditText edtEmail;
    private EditText edtPassword;
    private EditText edtConfirmPassword;
    private Spinner spinnerCity;
    private TextView txtError;
    private Button btnSubmit;

    private static class RegisterResponse {
        String status;
        String message;
        String token;
        UserAccount user;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        edtFullName = findViewById(R.id.edtRegFullName);
        edtUsername = findViewById(R.id.edtRegUsername);
        edtEmail = findViewById(R.id.edtRegEmail);
        edtPassword = findViewById(R.id.edtRegPassword);
        edtConfirmPassword = findViewById(R.id.edtRegConfirmPassword);
        spinnerCity = findViewById(R.id.spinnerRegCity);
        txtError = findViewById(R.id.txtRegError);
        btnSubmit = findViewById(R.id.btnSubmitRegister);

        ArrayAdapter<String> cityAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, DataRepository.AM_CITIES);
        spinnerCity.setAdapter(cityAdapter);

        findViewById(R.id.btnRegisterBack).setOnClickListener(v -> finish());
        btnSubmit.setOnClickListener(v -> attemptRegister());
    }

    private void attemptRegister() {
        txtError.setVisibility(View.GONE);

        String fullName = edtFullName.getText().toString().trim();
        String username = edtUsername.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString();
        String confirm = edtConfirmPassword.getText().toString();
        String city = spinnerCity.getSelectedItem() != null
                ? spinnerCity.getSelectedItem().toString()
                : "Manaus";

        if (fullName.length() < 3) {
            showError("Informe seu nome completo.");
            return;
        }
        if (username.length() < 3 || username.contains(" ")) {
            showError("Escolha um nome de usuário com ao menos 3 caracteres e sem espaços.");
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError("Informe um e-mail válido.");
            return;
        }
        if (password.length() < 6) {
            showError("A senha deve conter no mínimo 6 caracteres.");
            return;
        }
        if (!password.equals(confirm)) {
            showError("As senhas não coincidem.");
            return;
        }

        ApiClient api = ApiClient.getInstance();
        if (api.isRemoteConfigured()) {
            btnSubmit.setEnabled(false);
            Map<String, String> payload = new HashMap<>();
            payload.put("fullName", fullName);
            payload.put("username", username);
            payload.put("email", email);
            payload.put("city", city);
            payload.put("password", password);

            api.postJson("/auth/register", payload, null, RegisterResponse.class, new ApiCallback<RegisterResponse>() {
                @Override
                public void onSuccess(RegisterResponse data) {
                    if (isFinishing() || isDestroyed()) return;
                    btnSubmit.setEnabled(true);
                    if (data != null && "success".equals(data.status) && data.user != null && data.token != null) {
                        AuthSessionManager.getInstance(RegisterActivity.this).saveSession(data.user, data.token);
                        completeRegistration(data.user);
                    } else {
                        String msg = (data != null && data.message != null) ? data.message : "Não foi possível concluir o cadastro.";
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
            UserAccount created = AuthSessionManager.getInstance(this)
                    .registerLocalAccount(fullName, username, email, city, password);
            completeRegistration(created);
        }
    }

    private void completeRegistration(UserAccount account) {
        StoreManager store = StoreManager.getInstance(this);
        store.getState().name = account.fullName;
        store.getState().city = account.city;
        store.save();

        Toast.makeText(this, "Conta criada com sucesso!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, OnboardingActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showError(String msg) {
        txtError.setText(msg);
        txtError.setVisibility(View.VISIBLE);
    }
}
