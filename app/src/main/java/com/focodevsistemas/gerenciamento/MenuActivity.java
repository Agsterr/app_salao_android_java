package com.focodevsistemas.gerenciamento;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MenuActivity extends AppCompatActivity {

    public static final String EXTRA_MOSTRAR_SERVICOS = "mostrar_servicos";

    private Button buttonSalvar;
    private Button buttonGerenciarServicos;
    private Button buttonVerAgenda;
    private Button buttonGerenciarProdutos;
    private Button buttonBackup;
    private SubscriptionChecker subscriptionChecker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cliente);
        
        // Inicializa verificação de assinatura
        subscriptionChecker = SubscriptionChecker.getInstance(this);
        subscriptionChecker.initialize();
        
        // Verifica assinatura periodicamente (a cada vez que a tela é exibida)
        verificarAssinatura();
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
    
    @Override
    protected void onResume() {
        super.onResume();
        // Verifica assinatura sempre que a tela volta ao foco
        verificarAssinatura();
    }
    
    /**
     * Verifica se a assinatura ainda está ativa
     * Se não estiver, redireciona para tela de assinatura
     */
    private void verificarAssinatura() {
        subscriptionChecker.checkSubscription(isSubscribed -> {
            runOnUiThread(() -> {
                if (!isSubscribed) {
                    // Assinatura expirou ou foi cancelada
                    Toast.makeText(this, "Sua assinatura expirou. Renove para continuar usando o app.", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(MenuActivity.this, SubscriptionActivity.class);
                    startActivity(intent);
                    finish();
                }
            });
        });
    }
}



