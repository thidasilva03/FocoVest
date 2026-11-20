package com.example.focovest.data.remote;

import android.os.Handler;
import android.os.Looper;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Cliente HTTP centralizado baseado em ExecutorService + Handler (sem AsyncTask depreciada).
 * Aplica timeouts seguros, codificação UTF-8/URLEncoder, suporte a Bearer token e parsing com Gson.
 */
public class ApiClient {

    private static final int CONNECT_TIMEOUT_MS = 10_000;
    private static final int READ_TIMEOUT_MS = 15_000;

    private static ApiClient instance;
    private final ExecutorService executor = Executors.newFixedThreadPool(4);
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final Gson gson = new Gson();
    private String baseUrl = "";

    private ApiClient() {}

    public static synchronized ApiClient getInstance() {
        if (instance == null) {
            instance = new ApiClient();
        }
        return instance;
    }

    public void setBaseUrl(String baseUrl) {
        if (baseUrl == null) {
            this.baseUrl = "";
            return;
        }
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    public boolean isRemoteConfigured() {
        return baseUrl != null && baseUrl.startsWith("https://");
    }

    public <T> void get(String endpoint, Map<String, String> queryParams, String bearerToken,
                        Class<T> responseType, ApiCallback<T> callback) {
        executor.execute(() -> {
            HttpURLConnection conn = null;
            try {
                String fullUrl = buildUrl(endpoint, queryParams);
                conn = openConnection(fullUrl, "GET", bearerToken);
                dispatchResponse(conn, responseType, callback);
            } catch (Exception e) {
                postError(callback, -1, "Falha na comunicação com o servidor.");
            } finally {
                if (conn != null) conn.disconnect();
            }
        });
    }

    public <T> void postJson(String endpoint, Object bodyObject, String bearerToken,
                             Class<T> responseType, ApiCallback<T> callback) {
        executor.execute(() -> {
            HttpURLConnection conn = null;
            try {
                String fullUrl = buildUrl(endpoint, null);
                conn = openConnection(fullUrl, "POST", bearerToken);
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

                String jsonPayload = gson.toJson(bodyObject);
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(jsonPayload.getBytes(StandardCharsets.UTF_8));
                    os.flush();
                }

                dispatchResponse(conn, responseType, callback);
            } catch (Exception e) {
                postError(callback, -1, "Falha ao enviar dados ao servidor.");
            } finally {
                if (conn != null) conn.disconnect();
            }
        });
    }

    public <T> void postForm(String endpoint, Map<String, String> formFields, String bearerToken,
                             Class<T> responseType, ApiCallback<T> callback) {
        executor.execute(() -> {
            HttpURLConnection conn = null;
            try {
                String fullUrl = buildUrl(endpoint, null);
                conn = openConnection(fullUrl, "POST", bearerToken);
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");

                String encodedForm = encodeParams(formFields);
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(encodedForm.getBytes(StandardCharsets.UTF_8));
                    os.flush();
                }

                dispatchResponse(conn, responseType, callback);
            } catch (Exception e) {
                postError(callback, -1, "Falha ao enviar formulário ao servidor.");
            } finally {
                if (conn != null) conn.disconnect();
            }
        });
    }

    private HttpURLConnection openConnection(String fullUrl, String method, String bearerToken) throws Exception {
        URL url = new URL(fullUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        conn.setConnectTimeout(CONNECT_TIMEOUT_MS);
        conn.setReadTimeout(READ_TIMEOUT_MS);
        conn.setDoInput(true);
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("User-Agent", "FocoVest-Android");
        if (bearerToken != null && !bearerToken.isEmpty()) {
            conn.setRequestProperty("Authorization", "Bearer " + bearerToken);
        }
        return conn;
    }

    private <T> void dispatchResponse(HttpURLConnection conn, Class<T> responseType,
                                      ApiCallback<T> callback) throws Exception {
        int code = conn.getResponseCode();
        InputStream stream = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();
        String body = readStream(stream);

        if (code >= 200 && code < 300) {
            if (responseType == String.class) {
                //noinspection unchecked
                postSuccess(callback, (T) body);
            } else {
                T parsed = gson.fromJson(body, responseType);
                postSuccess(callback, parsed);
            }
        } else {
            postError(callback, code, "Erro HTTP " + code);
        }
    }

    private String buildUrl(String endpoint, Map<String, String> queryParams) throws Exception {
        String path = endpoint.startsWith("http")
                ? endpoint
                : baseUrl + (endpoint.startsWith("/") ? endpoint : "/" + endpoint);
        if (queryParams == null || queryParams.isEmpty()) return path;
        return path + "?" + encodeParams(queryParams);
    }

    private static String encodeParams(Map<String, String> params) throws Exception {
        if (params == null || params.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (sb.length() > 0) sb.append("&");
            sb.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            sb.append("=");
            sb.append(URLEncoder.encode(entry.getValue() != null ? entry.getValue() : "", "UTF-8"));
        }
        return sb.toString();
    }

    private static String readStream(InputStream in) throws Exception {
        if (in == null) return "";
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }

    private <T> void postSuccess(ApiCallback<T> callback, T data) {
        if (callback == null) return;
        mainHandler.post(() -> callback.onSuccess(data));
    }

    private <T> void postError(ApiCallback<T> callback, int code, String message) {
        if (callback == null) return;
        mainHandler.post(() -> callback.onError(code, message));
    }
}
