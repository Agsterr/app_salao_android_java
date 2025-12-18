package com.focodevsistemas.gerenciamento;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class UpdatesActivity extends AppCompatActivity {
    private long currentDownloadId = -1;
    private BroadcastReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_updates);

        String latestVersion = getIntent().getStringExtra("latestVersion");
        String downloadUrl = getIntent().getStringExtra("downloadUrl");

        TextView textVersion = findViewById(R.id.textLatestVersion);
        Button buttonDownload = findViewById(R.id.buttonDownloadLatest);
        Button buttonOlder = findViewById(R.id.buttonOlderVersions);

        textVersion.setText(latestVersion != null ? ("Última versão: " + latestVersion) : "Última versão desconhecida");

        buttonDownload.setOnClickListener(v -> {
            if (downloadUrl == null || downloadUrl.isEmpty()) {
                Toast.makeText(this, "URL de download não disponível.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                boolean canInstall = getPackageManager().canRequestPackageInstalls();
                if (!canInstall) {
                    Intent i = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:" + getPackageName()));
                    startActivity(i);
                    Toast.makeText(this, "Ative 'Instalar apps desconhecidos' e tente novamente.", Toast.LENGTH_LONG).show();
                    return;
                }
            }
            startApkDownload(downloadUrl);
        });

        buttonOlder.setOnClickListener(v -> {
            String owner = BuildConfig.GITHUB_OWNER;
            String repo = BuildConfig.GITHUB_REPO;
            Uri releasesPage = Uri.parse("https://github.com/" + owner + "/" + repo + "/releases");
            Intent i = new Intent(Intent.ACTION_VIEW, releasesPage);
            startActivity(i);
            Toast.makeText(this, "Para voltar a uma versão anterior, pode ser necessário desinstalar a atual.", Toast.LENGTH_LONG).show();
        });

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                if (id == currentDownloadId) {
                    DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                    Uri fileUri = dm.getUriForDownloadedFile(id);
                    if (fileUri != null) {
                        Intent installIntent = new Intent(Intent.ACTION_VIEW);
                        installIntent.setDataAndType(fileUri, "application/vnd.android.package-archive");
                        installIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(installIntent);
                    } else {
                        Toast.makeText(context, "Falha ao obter arquivo baixado.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        };
        registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    private void startApkDownload(String url) {
        try {
            DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
            DownloadManager.Request req = new DownloadManager.Request(Uri.parse(url));
            req.setTitle("Baixando atualização");
            req.setDescription("Gerenciamento Total Mais");
            req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            req.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
            currentDownloadId = dm.enqueue(req);
            Toast.makeText(this, "Download iniciado", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Erro ao iniciar download.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (receiver != null) {
            unregisterReceiver(receiver);
        }
    }
}
