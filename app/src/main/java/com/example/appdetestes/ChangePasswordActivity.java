package com.example.appdetestes;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ChangePasswordActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "AppDeTestesPrefs";
    private static final String PREF_CONFIG_USUARIO = "config_usuario";
    private static final String PREF_CONFIG_SENHA = "config_senha";

    private EditText editTextUsuarioAtual;
    private EditText editTextSenhaAtual;
    private EditText editTextNovoUsuario;
    private EditText editTextNovaSenha;
    private Button buttonSalvar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        editTextUsuarioAtual = findViewById(R.id.editTextUsuarioAtual);
        editTextSenhaAtual = findViewById(R.id.editTextSenhaAtual);
        editTextNovoUsuario = findViewById(R.id.editTextNovoUsuario);
        editTextNovaSenha = findViewById(R.id.editTextNovaSenha);
        buttonSalvar = findViewById(R.id.buttonSalvarAlteracao);

        buttonSalvar.setOnClickListener(v -> salvarAlteracoes());

        preencherCamposAtuais();
    }

    private void preencherCamposAtuais() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String usuarioAtual = prefs.getString(PREF_CONFIG_USUARIO, "");
        String senhaAtual = prefs.getString(PREF_CONFIG_SENHA, "");

        editTextUsuarioAtual.setText(usuarioAtual);
        editTextSenhaAtual.setText(senhaAtual);
    }

    private void salvarAlteracoes() {
        String usuarioAtualInput = editTextUsuarioAtual.getText().toString().trim();
        String senhaAtualInput = editTextSenhaAtual.getText().toString().trim();
        String novoUsuario = editTextNovoUsuario.getText().toString().trim();
        String novaSenha = editTextNovaSenha.getText().toString().trim();

        if (TextUtils.isEmpty(usuarioAtualInput) || TextUtils.isEmpty(senhaAtualInput)) {
            Toast.makeText(this, "Informe usuário e senha atuais", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(novoUsuario) || TextUtils.isEmpty(novaSenha)) {
            Toast.makeText(this, "Informe novo usuário e nova senha", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validar contra o que está salvo atualmente (se vazio, considerar que não há customização ainda)
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String usuarioAtualSalvo = prefs.getString(PREF_CONFIG_USUARIO, "");
        String senhaAtualSalva = prefs.getString(PREF_CONFIG_SENHA, "");

        // Permitir confirmação com admin/admin como mestre também
        boolean confirmacaoValida = (usuarioAtualInput.equals(usuarioAtualSalvo) && senhaAtualInput.equals(senhaAtualSalva))
                || (usuarioAtualInput.equals("admin") && senhaAtualInput.equals("admin"));

        if (!confirmacaoValida) {
            Toast.makeText(this, "Usuário/senha atuais incorretos", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PREF_CONFIG_USUARIO, novoUsuario);
        editor.putString(PREF_CONFIG_SENHA, novaSenha);
        editor.apply();

        Toast.makeText(this, "Credenciais atualizadas com sucesso", Toast.LENGTH_SHORT).show();
        finish();
    }
}