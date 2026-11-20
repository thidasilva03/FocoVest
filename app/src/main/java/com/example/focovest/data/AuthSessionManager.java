package com.example.focovest.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.focovest.model.UserAccount;
import com.google.gson.Gson;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.UUID;

/**
 * Gerencia autenticação e sessão local de forma segura.
 * Diferente do modelo legado (que trafegava senhas em texto claro em URLs e Intents),
 * armazena apenas o token de sessão e o hash salted SHA-256 para validação offline.
 */
public class AuthSessionManager {

    private static final String PREFS_NAME = "focovest_auth_session_v1";
    private static final String KEY_ACCOUNT = "user_account_json";
    private static final String KEY_TOKEN = "auth_bearer_token";
    private static final String KEY_LOCAL_SALT = "local_pwd_salt";
    private static final String KEY_LOCAL_HASH = "local_pwd_hash";

    private static AuthSessionManager instance;
    private final SharedPreferences prefs;
    private final Gson gson = new Gson();

    private AuthSessionManager(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized AuthSessionManager getInstance(Context context) {
        if (instance == null) {
            instance = new AuthSessionManager(context);
        }
        return instance;
    }

    public boolean isLoggedIn() {
        return getAccount() != null && getAuthToken() != null;
    }

    public UserAccount getAccount() {
        String raw = prefs.getString(KEY_ACCOUNT, null);
        if (raw == null) return null;
        try {
            return gson.fromJson(raw, UserAccount.class);
        } catch (Exception e) {
            return null;
        }
    }

    public String getAuthToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    public void saveSession(UserAccount account, String token) {
        prefs.edit()
                .putString(KEY_ACCOUNT, gson.toJson(account))
                .putString(KEY_TOKEN, token)
                .apply();
    }

    /**
     * Registra conta localmente (com senha protegida por salt + SHA-256) e inicia sessão.
     */
    public UserAccount registerLocalAccount(String fullName, String username, String email,
                                            String city, String plainPassword) {
        String salt = generateSalt();
        String hash = hashPassword(plainPassword, salt);
        UserAccount account = new UserAccount(
                "usr-" + UUID.randomUUID().toString().substring(0, 8),
                fullName.trim(),
                username.trim(),
                email.trim().toLowerCase(),
                city,
                "aluno"
        );
        String localToken = "local-" + UUID.randomUUID().toString();
        prefs.edit()
                .putString(KEY_ACCOUNT, gson.toJson(account))
                .putString(KEY_TOKEN, localToken)
                .putString(KEY_LOCAL_SALT, salt)
                .putString(KEY_LOCAL_HASH, hash)
                .apply();
        return account;
    }

    /**
     * Valida credenciais contra o hash salvo localmente (modo offline).
     */
    public boolean verifyLocalLogin(String email, String plainPassword) {
        UserAccount saved = getAccount();
        if (saved == null || saved.email == null) return false;
        if (!saved.email.equalsIgnoreCase(email.trim())) return false;

        String salt = prefs.getString(KEY_LOCAL_SALT, null);
        String expectedHash = prefs.getString(KEY_LOCAL_HASH, null);
        if (salt == null || expectedHash == null) return false;

        String computed = hashPassword(plainPassword, salt);
        if (constantTimeEquals(expectedHash, computed)) {
            prefs.edit().putString(KEY_TOKEN, "local-" + UUID.randomUUID().toString()).apply();
            return true;
        }
        return false;
    }

    public void logout() {
        prefs.edit().remove(KEY_TOKEN).apply();
    }

    public void clearAll() {
        prefs.edit().clear().apply();
    }

    private static String generateSalt() {
        byte[] bytes = new byte[16];
        new SecureRandom().nextBytes(bytes);
        return bytesToHex(bytes);
    }

    private static String hashPassword(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest((salt + ":" + password).getBytes(StandardCharsets.UTF_8));
            return bytesToHex(digest);
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 indisponível", e);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null || a.length() != b.length()) return false;
        int diff = 0;
        for (int i = 0; i < a.length(); i++) {
            diff |= a.charAt(i) ^ b.charAt(i);
        }
        return diff == 0;
    }
}
