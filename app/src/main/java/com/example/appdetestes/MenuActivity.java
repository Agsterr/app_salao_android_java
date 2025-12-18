package com.example.appdetestes;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MenuActivity extends AppCompatActivity {

    public static final String EXTRA_MOSTRAR_SERVICOS = "mostrar_servicos";

    private Button buttonSalvar;
    private Button buttonGerenciarServicos;
    private Button buttonVerAgenda;
    private Button buttonGerenciarProdutos;
    private Button buttonBackup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cliente);
        // Solicitar permissão de notificações no Android 13+
        if (android.os.Build.VERSION.SDK_INT >= 33) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                androidx.core.app.ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1001);
            }
        }

        buttonSalvar = findViewById(R.id.buttonSalvar);
        buttonGerenciarServicos = findViewById(R.id.buttonGerenciarServicos);
        buttonVerAgenda = findViewById(R.id.buttonVerAgenda);
        buttonGerenciarProdutos = findViewById(R.id.buttonGerenciarProdutos);
        buttonBackup = findViewById(R.id.buttonBackup);

        // Botão Agenda Pessoal - abre Activity separada
        Button buttonAgendaPessoal = findViewById(R.id.buttonAgendaPessoal);
        if (buttonAgendaPessoal != null) {
            buttonAgendaPessoal.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MenuActivity.this, AgendaPessoalActivity.class);
                    startActivity(intent);
                }
            });
        }

        // Botão Clientes - abre Activity separada
        buttonSalvar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MenuActivity.this, ClienteActivity.class);
                startActivity(intent);
            }
        });

        // Botão Serviços - abre Activity separada
        buttonGerenciarServicos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MenuActivity.this, ServicoListActivity.class);
                startActivity(intent);
            }
        });

        buttonVerAgenda.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MenuActivity.this, AgendaActivity.class);
                startActivity(intent);
            }
        });

        buttonBackup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MenuActivity.this, BackupActivity.class);
                startActivity(intent);
            }
        });

        buttonGerenciarProdutos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MenuActivity.this, ProdutoActivity.class);
                startActivity(intent);
            }
        });
    }
}



