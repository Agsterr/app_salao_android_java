package com.focodevsistemas.gerenciamento;

import android.content.Context;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public final class BillingEntitlementBackend {

    public interface EntitlementListener {
        void onResult(boolean success, boolean entitled);
    }

    private BillingEntitlementBackend() {}

    public static void verify(Context context, String baseUrl, String purchaseToken, String productId, EntitlementListener listener) {
        if (listener == null) {
            return;
        }
        if (context == null || baseUrl == null || baseUrl.trim().isEmpty() || purchaseToken == null || purchaseToken.trim().isEmpty()) {
            listener.onResult(false, false);
            return;
        }

        new Thread(() -> {
            HttpURLConnection conn = null;
            try {
                String normalized = baseUrl.trim();
                if (normalized.endsWith("/")) {
                    normalized = normalized.substring(0, normalized.length() - 1);
                }
                URL url = new URL(normalized + "/api/billing/entitlement");
                conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(8000);
                conn.setReadTimeout(10000);
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                conn.setRequestProperty("Accept", "application/json");

                JSONObject body = new JSONObject();
                body.put("packageName", context.getPackageName());
                body.put("purchaseToken", purchaseToken);
                body.put("productId", productId == null ? "" : productId);

                byte[] payload = body.toString().getBytes(StandardCharsets.UTF_8);
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(payload);
                    os.flush();
                }

                int code = conn.getResponseCode();
                InputStream is = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();
                String response = "";
                if (is != null) {
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                        StringBuilder sb = new StringBuilder();
                        String line;
                        while ((line = br.readLine()) != null) {
                            sb.append(line);
                        }
                        response = sb.toString();
                    }
                }
                if (code >= 200 && code < 300 && response != null && !response.isEmpty()) {
                    JSONObject json = new JSONObject(response);
                    boolean entitled = json.optBoolean("entitled", false);
                    listener.onResult(true, entitled);
                    return;
                }
                listener.onResult(false, false);
            } catch (Exception e) {
                listener.onResult(false, false);
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        }).start();
    }
}

