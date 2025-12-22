package com.focodevsistemas.gerenciamento;

import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

public class UpdateCheckWorker extends Worker {

    public UpdateCheckWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            String owner = BuildConfig.GITHUB_OWNER;
            String repo = BuildConfig.GITHUB_REPO;
            if (owner == null || owner.isEmpty() || repo == null || repo.isEmpty()) {
                return Result.success();
            }
            String apiUrl = String.format(Locale.US, "https://api.github.com/repos/%s/%s/releases/latest", owner, repo);
            JSONObject latest = fetchJson(apiUrl);
            if (latest == null) {
                return Result.success();
            }
            String tag = latest.optString("tag_name", "");
            String versionLatest = normalizeVersion(tag);
            String versionCurrent = normalizeVersion(BuildConfig.VERSION_NAME);
            boolean isNewer = isVersionNewer(versionCurrent, versionLatest);
            if (isNewer) {
                UpdateNotifier.notifyUpdateAvailable(getApplicationContext(), versionLatest);
            }
            return Result.success();
        } catch (Exception e) {
            e.printStackTrace();
            return Result.success();
        }
    }

    private JSONObject fetchJson(String urlStr) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(15000);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/vnd.github+json");
            conn.setRequestProperty("User-Agent", "GerenciamentoTotalMais/" + BuildConfig.VERSION_NAME + " (Android " + Build.VERSION.SDK_INT + ")");
            int code = conn.getResponseCode();
            InputStream is = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();
            if (is == null) return null;
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            br.close();
            return new JSONObject(sb.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private String normalizeVersion(String v) {
        if (v == null) return "0";
        v = v.trim();
        if (v.startsWith("v") || v.startsWith("V")) {
            v = v.substring(1);
        }
        // Remove any build metadata
        int plusIdx = v.indexOf('+');
        if (plusIdx >= 0) v = v.substring(0, plusIdx);
        return v;
    }

    private boolean isVersionNewer(String current, String latest) {
        try {
            String[] c = current.split("\\.");
            String[] l = latest.split("\\.");
            int n = Math.max(c.length, l.length);
            for (int i = 0; i < n; i++) {
                int ci = i < c.length ? parseIntSafe(c[i]) : 0;
                int li = i < l.length ? parseIntSafe(l[i]) : 0;
                if (li > ci) return true;
                if (li < ci) return false;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private int parseIntSafe(String s) {
        try { return Integer.parseInt(s); } catch (Exception e) { return 0; }
    }
}
