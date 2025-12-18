package com.focodevsistemas.gerenciamento;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.PackageManager;
import android.content.ComponentName;
import androidx.appcompat.app.AlertDialog;
import android.net.Uri;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Icon;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.os.Build;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;

import android.widget.SimpleAdapter;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

import com.google.android.material.button.MaterialButton;

public class LoginActivity extends AppCompatActivity {

    private EditText editTextUsuario;
    private EditText editTextSenha;
    private MaterialButton buttonLogin;
    private MaterialButton buttonAlterarSenha;
    private CheckBox checkBoxLembrar;
    private ActivityResultLauncher<String> imagePickerLauncher;
    private ActivityResultLauncher<String> postNotificationsPermissionLauncher;

    private static final String PREFS_NAME = "AppDeTestesPrefs";
    private static final String PREF_USUARIO = "usuario"; // apenas para lembrar preenchimento
    private static final String PREF_SENHA = "senha";     // apenas para lembrar preenchimento
    private static final String PREF_LEMBRAR = "lembrar";
    private static final String PREF_ASKED_POST_NOTIFICATIONS = "asked_post_notifications";

    // Credenciais configuradas pelo usuário (persistência real de login)
    private static final String PREF_CONFIG_USUARIO = "config_usuario";
    private static final String PREF_CONFIG_SENHA = "config_senha";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Agendar verificação diária de atualizações via GitHub
        UpdateScheduler.scheduleDaily(this);
        Toast.makeText(this, "Seja bem-vindo!", Toast.LENGTH_SHORT).show();

        editTextUsuario = findViewById(R.id.editTextUsuario);
        editTextSenha = findViewById(R.id.editTextSenha);
        buttonLogin = findViewById(R.id.buttonLogin);
        buttonAlterarSenha = findViewById(R.id.buttonAlterarSenha);
        checkBoxLembrar = findViewById(R.id.checkBoxLembrar);

        postNotificationsPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (!isGranted) {
                        Toast.makeText(this, "Ative notificações para receber avisos de atualização.", Toast.LENGTH_LONG).show();
                    }
                }
        );
        if (Build.VERSION.SDK_INT >= 33
                && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            boolean asked = prefs.getBoolean(PREF_ASKED_POST_NOTIFICATIONS, false);
            if (!asked) {
                prefs.edit().putBoolean(PREF_ASKED_POST_NOTIFICATIONS, true).apply();
                postNotificationsPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }

        // Inicializa seletor de imagem da galeria
        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                handleImageUri(uri);
            }
        });

        // Botões para trocar ícone do app
        MaterialButton buttonIconPadrao = findViewById(R.id.buttonIconPadrao);
        MaterialButton buttonIconAlternativo = findViewById(R.id.buttonIconAlternativo);
        if (buttonIconPadrao != null && buttonIconAlternativo != null) {
            buttonIconPadrao.setOnClickListener(v -> setIconVariant(false));
            buttonIconAlternativo.setOnClickListener(v -> showIconAltOptions());
        }
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

        buttonAlterarSenha.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ChangePasswordActivity.class);
            startActivity(intent);
        });

        carregarCredenciais();
    }

    private void carregarCredenciais() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean lembrar = prefs.getBoolean(PREF_LEMBRAR, false);

        if (lembrar) {
            String usuario = prefs.getString(PREF_USUARIO, "");
            String senha = prefs.getString(PREF_SENHA, "");
            editTextUsuario.setText(usuario);
            editTextSenha.setText(senha);
            checkBoxLembrar.setChecked(true);
        }
    }

    private void salvarOuLimparCredenciais() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        if (checkBoxLembrar.isChecked()) {
            // Salva apenas para preencher automaticamente (não é a credencial oficial)
            editor.putString(PREF_USUARIO, editTextUsuario.getText().toString());
            editor.putString(PREF_SENHA, editTextSenha.getText().toString());
            editor.putBoolean(PREF_LEMBRAR, true);
        } else {
            editor.remove(PREF_USUARIO);
            editor.remove(PREF_SENHA);
            editor.remove(PREF_LEMBRAR);
        }

        editor.apply();
    }

    private void login() {
        String usuario = editTextUsuario.getText().toString();
        String senha = editTextSenha.getText().toString();

        // Sempre permite mestre: admin/admin
        if (usuario.equals("admin") && senha.equals("admin")) {
            autenticarComSucesso();
            return;
        }

        // Caso contrário, verifica credenciais configuradas pelo usuário
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String configUsuario = prefs.getString(PREF_CONFIG_USUARIO, "");
        String configSenha = prefs.getString(PREF_CONFIG_SENHA, "");

        if (!configUsuario.isEmpty() && !configSenha.isEmpty() &&
                usuario.equals(configUsuario) && senha.equals(configSenha)) {
            autenticarComSucesso();
        } else {
            Toast.makeText(this, "Usuário ou senha inválidos", Toast.LENGTH_SHORT).show();
        }
    }

    private void autenticarComSucesso() {
        salvarOuLimparCredenciais();
        Intent intent = new Intent(LoginActivity.this, MenuActivity.class);
        startActivity(intent);
        finish();
    }

    private void showIconAltOptions() {
        List<Map<String, String>> items = new ArrayList<>();
        Map<String, String> m1 = new HashMap<>();
        m1.put("title", "Alternativa 1: Ícone alternativo padrão");
        m1.put("subtitle", "Troca o launcher para 'LauncherAlt' com recurso azul.");
        items.add(m1);
        Map<String, String> m2 = new HashMap<>();
        m2.put("title", "Alternativa 2: Selecionar da galeria");
        m2.put("subtitle", "Cria atalho com ícone personalizado da sua galeria.");
        items.add(m2);
        Map<String, String> m3 = new HashMap<>();
        m3.put("title", "Alternativa 3: Importar da internet (URL)");
        m3.put("subtitle", "Cria atalho usando imagem obtida por link.");
        items.add(m3);

        SimpleAdapter adapter = new SimpleAdapter(
                this,
                items,
                R.layout.dialog_list_item_two_line,
                new String[]{"title", "subtitle"},
                new int[]{android.R.id.text1, android.R.id.text2}
        );

        new AlertDialog.Builder(this)
                .setTitle("Ícone Alternativo — opções")
                .setAdapter(adapter, (dialog, which) -> {
                    if (which == 0) {
                        setIconVariant(true);
                    } else if (which == 1) {
                        imagePickerLauncher.launch("image/*");
                    } else if (which == 2) {
                        showUrlInputDialog();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void showUrlInputDialog() {
        android.widget.EditText input = new android.widget.EditText(this);
        input.setHint("https://exemplo.com/icone.png");
        input.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_URI);
        new AlertDialog.Builder(this)
                .setTitle("Importar ícone da internet")
                .setView(input)
                .setPositiveButton("Importar", (d, w) -> {
                    String url = input.getText().toString().trim();
                    if (!url.isEmpty()) {
                        downloadImage(url);
                    } else {
                        Toast.makeText(this, "URL vazia", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void downloadImage(String urlStr) {
        new Thread(() -> {
            try {
                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(8000);
                conn.setReadTimeout(8000);
                conn.connect();
                try (InputStream is = conn.getInputStream()) {
                    Bitmap bmp = BitmapFactory.decodeStream(is);
                    if (bmp != null) {
                        runOnUiThread(() -> pinShortcutWithIcon(bmp));
                    } else {
                        runOnUiThread(() -> Toast.makeText(this, "Falha ao decodificar imagem", Toast.LENGTH_SHORT).show());
                    }
                }
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Erro ao baixar imagem: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void handleImageUri(Uri uri) {
        try (InputStream is = getContentResolver().openInputStream(uri)) {
            Bitmap bmp = BitmapFactory.decodeStream(is);
            if (bmp != null) {
                pinShortcutWithIcon(bmp);
            } else {
                Toast.makeText(this, "Imagem inválida", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Erro ao abrir imagem: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void pinShortcutWithIcon(Bitmap bitmap) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ShortcutManager sm = getSystemService(ShortcutManager.class);
            if (sm != null && sm.isRequestPinShortcutSupported()) {
                Intent launchIntent = new Intent(this, LoginActivity.class);
                launchIntent.setAction(Intent.ACTION_VIEW);

                ShortcutInfo shortcut = new ShortcutInfo.Builder(this, "appdetestes_custom_icon")
                        .setShortLabel(getString(R.string.app_name))
                        .setLongLabel(getString(R.string.app_name))
                        .setIcon(Icon.createWithBitmap(bitmap))
                        .setIntent(launchIntent)
                        .build();
                sm.requestPinShortcut(shortcut, null);
                Toast.makeText(this, "Solicitado atalho com ícone personalizado", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Launcher não suporta atalhos fixados", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Android não suporta atalhos fixados nesta versão", Toast.LENGTH_SHORT).show();
        }
    }

    private void setIconVariant(boolean useAlt) {
        PackageManager pm = getPackageManager();
        ComponentName defaultAlias = new ComponentName(getPackageName(), "com.focodevsistemas.gerenciamento.LauncherDefault");
        ComponentName altAlias = new ComponentName(getPackageName(), "com.focodevsistemas.gerenciamento.LauncherAlt");

        pm.setComponentEnabledSetting(
                defaultAlias,
                useAlt ? PackageManager.COMPONENT_ENABLED_STATE_DISABLED : PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
        );
        pm.setComponentEnabledSetting(
                altAlias,
                useAlt ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
        );

        Toast.makeText(this, useAlt ? "Ícone alternativo ativado" : "Ícone padrão ativado", Toast.LENGTH_SHORT).show();
    }
}
