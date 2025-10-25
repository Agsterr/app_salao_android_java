package com.example.appdetestes;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText editTextUsuario;
    private EditText editTextSenha;
    private Button buttonLogin;
    private CheckBox checkBoxLembrar;

    private static final String PREFS_NAME = "AppDeTestesPrefs";
    private static final String PREF_USUARIO = "usuario";
    private static final String PREF_SENHA = "senha";
    private static final String PREF_LEMBRAR = "lembrar";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editTextUsuario = findViewById(R.id.editTextUsuario);
        editTextSenha = findViewById(R.id.editTextSenha);
        buttonLogin = findViewById(R.id.buttonLogin);
        checkBoxLembrar = findViewById(R.id.checkBoxLembrar);

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
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
            // Salva as credenciais
            editor.putString(PREF_USUARIO, editTextUsuario.getText().toString());
            editor.putString(PREF_SENHA, editTextSenha.getText().toString());
            editor.putBoolean(PREF_LEMBRAR, true);
        } else {
            // Limpa as credenciais salvas
            editor.remove(PREF_USUARIO);
            editor.remove(PREF_SENHA);
            editor.remove(PREF_LEMBRAR);
        }

        editor.apply();
    }

    private void login() {
        String usuario = editTextUsuario.getText().toString();
        String senha = editTextSenha.getText().toString();

        // Lógica de autenticação simples
        if (usuario.equals("admin") && senha.equals("admin")) {
            salvarOuLimparCredenciais();

            // Redireciona diretamente para a tela principal
            Intent intent = new Intent(LoginActivity.this, ClienteActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Usuário ou senha inválidos", Toast.LENGTH_SHORT).show();
        }
    }
}
