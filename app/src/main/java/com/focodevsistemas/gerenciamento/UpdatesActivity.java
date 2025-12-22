package com.focodevsistemas.gerenciamento;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class UpdatesActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_updates);

        String latestVersion = getIntent().getStringExtra("latestVersion");

        TextView textVersion = findViewById(R.id.textLatestVersion);
        Button buttonDownload = findViewById(R.id.buttonDownloadLatest);
        Button buttonOlder = findViewById(R.id.buttonOlderVersions);

        textVersion.setText(latestVersion != null ? ("Última versão: " + latestVersion) : "Última versão desconhecida");

        buttonDownload.setOnClickListener(v -> {
            openPlayStoreListing();
        });

        buttonOlder.setOnClickListener(v -> {
            String owner = BuildConfig.GITHUB_OWNER;
            String repo = BuildConfig.GITHUB_REPO;
            Uri releasesPage = Uri.parse("https://github.com/" + owner + "/" + repo + "/releases");
            Intent i = new Intent(Intent.ACTION_VIEW, releasesPage);
            startActivity(i);
        });
    }

    private void openPlayStoreListing() {
        String packageName = getPackageName();
        Uri marketUri = Uri.parse("market://details?id=" + packageName);
        Intent marketIntent = new Intent(Intent.ACTION_VIEW, marketUri);
        try {
            startActivity(marketIntent);
        } catch (ActivityNotFoundException e) {
            Uri webUri = Uri.parse("https://play.google.com/store/apps/details?id=" + packageName);
            Intent webIntent = new Intent(Intent.ACTION_VIEW, webUri);
            startActivity(webIntent);
        }
        Toast.makeText(this, "Abra a Play Store para atualizar o app.", Toast.LENGTH_SHORT).show();
    }
}
