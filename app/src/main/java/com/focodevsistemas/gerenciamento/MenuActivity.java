package com.focodevsistemas.gerenciamento;

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
    private Button buttonAssinarPremium;
    private PremiumBlockDialog currentDialog;

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

        // Inicializa o sistema de assinatura e verifica status
        SubscriptionService.getInstance(this).initialize(() -> {
            // Callback opcional quando o BillingClient estiver pronto
            // A verificação de assinatura acontece automaticamente dentro do initialize
        });

        buttonSalvar = findViewById(R.id.buttonSalvar);
        buttonGerenciarServicos = findViewById(R.id.buttonGerenciarServicos);
        buttonVerAgenda = findViewById(R.id.buttonVerAgenda);
        buttonGerenciarProdutos = findViewById(R.id.buttonGerenciarProdutos);
        buttonBackup = findViewById(R.id.buttonBackup);
        buttonAssinarPremium = findViewById(R.id.buttonAssinarPremium);
        
        setupAssinaturaButton();

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

        // Botão Relatórios Serviços Premium
        Button buttonRelatoriosServicos = findViewById(R.id.buttonRelatoriosServicos);
        if (buttonRelatoriosServicos != null) {
            buttonRelatoriosServicos.setOnClickListener(v -> {
                PremiumManager.getInstance(this).executarAcaoPremium(this, "Relatórios de Serviços", () -> {
                    Intent intent = new Intent(MenuActivity.this, RelatoriosServicosActivity.class);
                    startActivity(intent);
                });
            });
        }

        // Botão Relatórios Produtos Premium
        Button buttonRelatoriosProdutos = findViewById(R.id.buttonRelatoriosProdutos);
        if (buttonRelatoriosProdutos != null) {
            buttonRelatoriosProdutos.setOnClickListener(v -> {
                PremiumManager.getInstance(this).executarAcaoPremium(this, "Relatórios de Produtos", () -> {
                    Intent intent = new Intent(MenuActivity.this, RelatoriosProdutosActivity.class);
                    startActivity(intent);
                });
            });
        }

        // Botão Dashboard Premium
        Button buttonDashboard = findViewById(R.id.buttonDashboard);
        if (buttonDashboard != null) {
            buttonDashboard.setOnClickListener(v -> {
                PremiumManager.getInstance(this).executarAcaoPremium(this, "Dashboard", () -> {
                    Intent intent = new Intent(MenuActivity.this, DashboardActivity.class);
                    startActivity(intent);
                });
            });
        }

        // Botão Orçamentos Premium
        Button buttonOrcamentos = findViewById(R.id.buttonOrcamentos);
        if (buttonOrcamentos != null) {
            buttonOrcamentos.setOnClickListener(v -> {
                PremiumManager.getInstance(this).executarAcaoPremium(this, "Orçamentos", () -> {
                    Intent intent = new Intent(MenuActivity.this, OrcamentosActivity.class);
                    startActivity(intent);
                });
            });
        }

        // Botão Alertas Premium
        Button buttonAlertas = findViewById(R.id.buttonAlertas);
        if (buttonAlertas != null) {
            buttonAlertas.setOnClickListener(v -> {
                PremiumManager.getInstance(this).executarAcaoPremium(this, "Alertas", () -> {
                    Intent intent = new Intent(MenuActivity.this, AlertasActivity.class);
                    startActivity(intent);
                });
            });
        }
    }
    
    private void setupAssinaturaButton() {
        if (buttonAssinarPremium == null) return;

        // Configura o clique do botão
        buttonAssinarPremium.setOnClickListener(v -> {
            SubscriptionService.getInstance(this).activatePremiumSubscription(this, new SubscriptionService.SubscriptionListener() {
                @Override
                public void onSubscriptionActivated(String productId) {
                    runOnUiThread(() -> {
                        android.widget.Toast.makeText(MenuActivity.this, 
                            "Assinatura Premium ativada com sucesso! Aproveite.", 
                            android.widget.Toast.LENGTH_LONG).show();
                        updatePremiumUI(true);
                    });
                }

                @Override
                public void onSubscriptionDeactivated() {
                    runOnUiThread(() -> updatePremiumUI(false));
                }

                @Override
                public void onSubscriptionError(String error) {
                    runOnUiThread(() -> 
                        android.widget.Toast.makeText(MenuActivity.this, 
                            "Erro na assinatura: " + error, 
                            android.widget.Toast.LENGTH_LONG).show()
                    );
                }
            });
        });

        // Define estado inicial
        updatePremiumUI(SubscriptionService.getInstance(this).isSubscriptionActive());
    }

    private void updatePremiumUI(boolean isPremium) {
        if (buttonAssinarPremium != null) {
            buttonAssinarPremium.setVisibility(isPremium ? View.GONE : View.VISIBLE);
        }
        // Aqui poderíamos também atualizar outros elementos da UI se necessário
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Atualiza UI ao retornar para a Activity (ex: após voltar do fluxo de pagamento do Google Play)
        if (buttonAssinarPremium != null) {
            boolean isPremium = SubscriptionService.getInstance(this).isSubscriptionActive();
            updatePremiumUI(isPremium);
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        // Não fechar dialog no onPause - deixar o usuário interagir
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        // Fechar dialog quando a Activity for parada (mas não destruída ainda)
        // Isso previne WindowLeaked
        if (currentDialog != null && currentDialog.isShowing()) {
            currentDialog.dismiss();
            currentDialog = null;
        }
    }
    
    @Override
    protected void onDestroy() {
        // Garantir que qualquer dialog seja fechado quando a Activity for destruída
        if (currentDialog != null) {
            try {
                if (currentDialog.isShowing()) {
                    currentDialog.dismiss();
                }
            } catch (Exception e) {
                // Ignorar erros durante destruição
            } finally {
                currentDialog = null;
            }
        }
        super.onDestroy();
    }
}


