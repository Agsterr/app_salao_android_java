package com.focodevsistemas.gerenciamento;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

/**
 * Dialog para bloquear acesso a funcionalidades Premium e oferecer upgrade.
 */
public class PremiumBlockDialog {

    private final Activity activity;
    private final String featureName;
    private AlertDialog dialog;

    public PremiumBlockDialog(Activity activity, String featureName) {
        this.activity = activity;
        this.featureName = featureName;
    }

    /**
     * Exibe o dialog de bloqueio premium.
     */
    public void show() {
        // Verificar se a Activity ainda está válida antes de mostrar o dialog
        if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
            return;
        }
        
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            
            builder.setView(createCustomView());
            builder.setCancelable(true);
            
            dialog = builder.create();
            
            // Garantir que o dialog seja fechado quando a Activity for destruída
            dialog.setOnDismissListener(dialogInterface -> {
                // Cleanup - dialog já foi fechado
                dialog = null;
            });
            
            // Verificar novamente antes de mostrar
            if (!activity.isFinishing() && !activity.isDestroyed()) {
                try {
                    dialog.show();
                } catch (Exception e) {
                    // Se falhar ao mostrar, limpar referência
                    dialog = null;
                    android.util.Log.e("PremiumBlockDialog", "Erro ao mostrar dialog", e);
                }
            } else {
                dialog = null;
            }
        } catch (Exception e) {
            // Ignorar se a Activity já foi destruída
            android.util.Log.e("PremiumBlockDialog", "Erro ao exibir dialog", e);
            dialog = null;
        }
    }
    
    /**
     * Fecha o dialog se estiver aberto.
     */
    public void dismiss() {
        if (dialog != null) {
            try {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
            } catch (Exception e) {
                // Ignorar se já foi fechado ou Activity foi destruída
                android.util.Log.e("PremiumBlockDialog", "Erro ao fechar dialog", e);
            } finally {
                dialog = null;
            }
        }
    }
    
    /**
     * Verifica se o dialog está sendo exibido.
     */
    public boolean isShowing() {
        return dialog != null && dialog.isShowing();
    }

    /**
     * Cria a view customizada do dialog.
     */
    private View createCustomView() {
        // Criar view programaticamente
        android.widget.LinearLayout layout = new android.widget.LinearLayout(activity);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(48, 32, 48, 32);
        
        // Título
        TextView title = new TextView(activity);
        title.setText("Funcionalidade Premium");
        title.setTextSize(20);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setTextColor(activity.getResources().getColor(android.R.color.black, null));
        title.setPadding(0, 0, 0, 16);
        layout.addView(title);
        
        // Mensagem
        TextView message = new TextView(activity);
        String messageText = String.format(
            "A funcionalidade \"%s\" está disponível apenas no plano PREMIUM.\n\n" +
            "Faça upgrade para desbloquear todos os recursos premium!",
            featureName
        );
        message.setText(messageText);
        message.setTextSize(16);
        message.setTextColor(activity.getResources().getColor(android.R.color.darker_gray, null));
        message.setPadding(0, 0, 0, 24);
        layout.addView(message);
        
        // Container para botões
        android.widget.LinearLayout buttonContainer = new android.widget.LinearLayout(activity);
        buttonContainer.setOrientation(android.widget.LinearLayout.HORIZONTAL);
        buttonContainer.setPadding(0, 0, 0, 0);
        
        // Botão de upgrade
        Button upgradeButton = new Button(activity);
        upgradeButton.setText("Fazer Upgrade");
        android.widget.LinearLayout.LayoutParams upgradeParams = new android.widget.LinearLayout.LayoutParams(
            0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
        upgradeParams.setMargins(0, 0, 8, 0);
        upgradeButton.setLayoutParams(upgradeParams);
        upgradeButton.setOnClickListener(v -> {
            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }
            if (activity != null && !activity.isFinishing() && !activity.isDestroyed()) {
                openSubscriptionActivity();
            }
        });
        buttonContainer.addView(upgradeButton);
        
        // Botão de cancelar
        Button cancelButton = new Button(activity);
        cancelButton.setText("Cancelar");
        android.widget.LinearLayout.LayoutParams cancelParams = new android.widget.LinearLayout.LayoutParams(
            0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
        cancelParams.setMargins(8, 0, 0, 0);
        cancelButton.setLayoutParams(cancelParams);
        cancelButton.setOnClickListener(v -> {
            dismissDialog();
            // Fechar Activity quando cancelar (para Activities que devem ser fechadas)
            closeActivityIfNeeded();
        });
        buttonContainer.addView(cancelButton);
        
        layout.addView(buttonContainer);
        
        return layout;
    }

    /**
     * Fecha o dialog de forma segura.
     */
    private void dismissDialog() {
        if (dialog != null && dialog.isShowing()) {
            try {
                dialog.dismiss();
            } catch (Exception e) {
                android.util.Log.e("PremiumBlockDialog", "Erro ao fechar dialog", e);
            } finally {
                dialog = null;
            }
        }
    }
    
    /**
     * Fecha a Activity se necessário (para Activities que devem ser fechadas quando acesso é bloqueado).
     */
    private void closeActivityIfNeeded() {
        if (activity != null && !activity.isFinishing() && !activity.isDestroyed()) {
            try {
                // Verificar se é MenuActivity ou outras que devem ser fechadas
                String className = activity.getClass().getSimpleName();
                if (className.equals("MenuActivity") || 
                    className.contains("Dashboard") ||
                    className.contains("Relatorios") ||
                    className.contains("Alertas") ||
                    className.contains("AgendaTotais")) {
                    activity.finish();
                }
            } catch (Exception e) {
                android.util.Log.e("PremiumBlockDialog", "Erro ao fechar Activity", e);
            }
        }
    }
    
    /**
     * Abre a Activity de assinatura para upgrade.
     */
    private void openSubscriptionActivity() {
        dismissDialog();
        if (activity != null && !activity.isFinishing() && !activity.isDestroyed()) {
            try {
                Intent intent = new Intent(activity, SubscriptionActivity.class);
                activity.startActivity(intent);
                // Fechar Activity atual após abrir SubscriptionActivity
                closeActivityIfNeeded();
            } catch (Exception e) {
                android.util.Log.e("PremiumBlockDialog", "Erro ao abrir SubscriptionActivity", e);
            }
        }
    }

    /**
     * Método estático para exibir o dialog rapidamente.
     * Retorna a instância do dialog para permitir gerenciamento do ciclo de vida.
     * 
     * @param activity Activity atual
     * @param featureName Nome da funcionalidade bloqueada
     * @return Instância do PremiumBlockDialog para gerenciamento
     */
    public static PremiumBlockDialog show(Activity activity, String featureName) {
        PremiumBlockDialog dialog = new PremiumBlockDialog(activity, featureName);
        dialog.show();
        return dialog;
    }
}

