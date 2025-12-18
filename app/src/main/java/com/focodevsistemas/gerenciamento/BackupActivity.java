package com.focodevsistemas.gerenciamento;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BackupActivity extends AppCompatActivity {

    private ActivityResultLauncher<Intent> createDocumentLauncher;
    private ActivityResultLauncher<Intent> openDocumentLauncher;
    private MaterialButton buttonVoltar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backup);

        MaterialButton buttonExportar = findViewById(R.id.buttonExportar);
        MaterialButton buttonImportar = findViewById(R.id.buttonImportar);
        buttonVoltar = findViewById(R.id.buttonVoltar);

        if (buttonVoltar != null) {
            buttonVoltar.setOnClickListener(v -> finish());
        }

        // Exportar: criar documento via SAF
        createDocumentLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        if (uri != null) {
                            boolean ok = exportDatabaseToUri(uri);
                            Toast.makeText(this, ok ? "Backup exportado com sucesso" : "Falha ao exportar backup", Toast.LENGTH_LONG).show();
                        }
                    }
                }
        );

        // Importar: abrir documento via SAF
        openDocumentLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        if (uri != null) {
                            boolean ok = importDatabaseFromUri(uri);
                            if (ok) {
                                Toast.makeText(this, "Dados importados. Reinicie o app para aplicar.", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(this, "Falha ao importar dados", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                }
        );

        buttonExportar.setOnClickListener(v -> {
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(new Date());
            String defaultName = "backup_appdetestes_" + timestamp + ".db";
            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("application/octet-stream");
            intent.putExtra(Intent.EXTRA_TITLE, defaultName);
            createDocumentLauncher.launch(intent);
        });

        buttonImportar.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            openDocumentLauncher.launch(intent);
        });
    }

    private boolean exportDatabaseToUri(Uri destUri) {
        try {
            File dbFile = getDatabasePath("mydatabase.db");
            if (!dbFile.exists()) {
                return false;
            }
            try (InputStream in = new FileInputStream(dbFile); OutputStream out = getContentResolver().openOutputStream(destUri)) {
                if (out == null) return false;
                byte[] buffer = new byte[8192];
                int len;
                while ((len = in.read(buffer)) > 0) {
                    out.write(buffer, 0, len);
                }
                out.flush();
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean importDatabaseFromUri(Uri srcUri) {
        try {
            File dbFile = getDatabasePath("mydatabase.db");
            // Criar backup local antes de substituir
            File bakFile = new File(dbFile.getParentFile(), "mydatabase_before_import.bak");
            if (dbFile.exists()) {
                try (InputStream inBk = new FileInputStream(dbFile); OutputStream outBk = new FileOutputStream(bakFile)) {
                    byte[] buf = new byte[8192];
                    int n;
                    while ((n = inBk.read(buf)) > 0) {
                        outBk.write(buf, 0, n);
                    }
                    outBk.flush();
                } catch (Exception ignore) {}
            }
            try (InputStream in = getContentResolver().openInputStream(srcUri); OutputStream out = new FileOutputStream(dbFile)) {
                if (in == null) return false;
                byte[] buffer = new byte[8192];
                int len;
                while ((len = in.read(buffer)) > 0) {
                    out.write(buffer, 0, len);
                }
                out.flush();
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
