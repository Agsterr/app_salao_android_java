package com.focodevsistemas.gerenciamento;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

// Implementa as interfaces para receber os callbacks
public class SubscriptionActivity extends AppCompatActivity implements 
        BillingManager.SubscriptionVerificationListener, 
        BillingManager.BillingReadyListener {

    private BillingManager billingManager;
    private MaterialButton buttonAssinar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscription);

        buttonAssinar = findViewById(R.id.buttonAssinar);
        buttonAssinar.setVisibility(View.GONE); // Mantém o botão oculto inicialmente

        buttonAssinar.setOnClickListener(v -> {
            billingManager.queryAndLaunchBillingFlow(SubscriptionActivity.this, BuildConfig.SUBSCRIPTION_PRODUCT_ID);
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
                Intent intent = new Intent(SubscriptionActivity.this, MenuActivity.class);
                startActivity(intent);
                finish();
            } else {
                // Nenhuma assinatura ativa, mostra o botão para assinar
                buttonAssinar.setVisibility(View.VISIBLE);
            }
        });
    }
}
