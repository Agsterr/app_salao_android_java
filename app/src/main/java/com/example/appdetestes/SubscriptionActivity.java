package com.example.appdetestes;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

// Implementa as interfaces para receber os callbacks
public class SubscriptionActivity extends AppCompatActivity implements 
        BillingManager.SubscriptionVerificationListener, 
        BillingManager.BillingReadyListener {

    private BillingManager billingManager;
    private Button buttonAssinar;

    // IMPORTANTE: Substitua este valor pelo ID real da sua assinatura no Google Play Console
    private static final String ID_PRODUTO_ASSINATURA = "sua_assinatura_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscription);

        buttonAssinar = findViewById(R.id.buttonAssinar);
        buttonAssinar.setVisibility(View.GONE); // Mantém o botão oculto inicialmente

        buttonAssinar.setOnClickListener(v -> {
            billingManager.queryAndLaunchBillingFlow(SubscriptionActivity.this, ID_PRODUTO_ASSINATURA);
        });

        // Inicializa o BillingManager, passando a Activity como ouvinte de prontidão
        billingManager = new BillingManager(this, this);
    }

    // Este método é chamado pelo BillingManager quando a conexão com o Google Play é estabelecida
    @Override
    public void onBillingClientReady() {
        // Agora que a conexão está pronta, podemos verificar a assinatura com segurança
        billingManager.verificarAssinaturaAtiva(this);
    }

    // Este método é chamado com o resultado da verificação da assinatura
    @Override
    public void onVerificationResult(boolean isSubscribed) {
        runOnUiThread(() -> {
            if (isSubscribed) {
                // Assinatura ativa, navega para a tela principal
                Intent intent = new Intent(SubscriptionActivity.this, ClienteActivity.class);
                startActivity(intent);
                finish();
            } else {
                // Nenhuma assinatura ativa, mostra o botão para assinar
                buttonAssinar.setVisibility(View.VISIBLE);
            }
        });
    }
}
