package com.focodevsistemas.gerenciamento;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

/**
 * Gerenciador centralizado de status Premium e Billing.
 * Fachada simplificada para verificação de assinaturas em todo o app.
 */
public class PremiumManager {

    private static final String TAG = "PremiumManager";
    private static volatile PremiumManager instance;
    private final Context context;
    private final SubscriptionService subscriptionService;
    private final PlanManager planManager;

    private PremiumManager(Context context) {
        this.context = context.getApplicationContext();
        this.subscriptionService = SubscriptionService.getInstance(this.context);
        this.planManager = PlanManager.getInstance(this.context);
    }

    /**
     * Obtém a instância única (Singleton).
     */
    public static PremiumManager getInstance(Context context) {
        if (instance == null) {
            synchronized (PremiumManager.class) {
                if (instance == null) {
                    instance = new PremiumManager(context);
                }
            }
        }
        return instance;
    }

    /**
     * Verifica se o usuário é Premium.
     * Checa tanto a assinatura ativa quanto o estado do plano local.
     * 
     * @return true se o usuário tem acesso Premium válido.
     */
    public boolean isUsuarioPremium() {
        // Verifica assinatura ativa (real ou simulada validada)
        boolean assinaturaAtiva = subscriptionService.isSubscriptionActive();
        
        // Verifica consistência com PlanManager
        boolean planoPremium = planManager.isPremium();
        
        // Se houver divergência, prioriza a assinatura e corrige o plano
        if (assinaturaAtiva && !planoPremium) {
            Log.w(TAG, "Correção: Assinatura ativa mas plano não era Premium. Corrigindo...");
            planManager.setCurrentPlan(PlanType.PREMIUM);
            planoPremium = true;
        } else if (!assinaturaAtiva && planoPremium) {
            // Se plano diz que é premium mas assinatura expirou
            Log.w(TAG, "Correção: Plano Premium mas sem assinatura ativa. Corrigindo...");
            
            // Só corrige se não for uma assinatura simulada válida (o que isSubscriptionActive já checa)
            planManager.setCurrentPlan(PlanType.FREE);
            planoPremium = false;
        }

        return assinaturaAtiva;
    }

    /**
     * Executa uma ação Premium se o usuário tiver acesso.
     * Se não tiver, exibe o fluxo de bloqueio/assinatura automaticamente.
     * 
     * @param activity Activity atual (necessária para exibir dialogs)
     * @param featureName Nome da funcionalidade (para exibir no dialog)
     * @param acaoPremium Ação a ser executada se for Premium (Runnable)
     */
    public void executarAcaoPremium(Activity activity, String featureName, Runnable acaoPremium) {
        if (isUsuarioPremium()) {
            // Usuário é Premium - executa a ação
            Log.d(TAG, "Acesso liberado para: " + featureName);
            if (acaoPremium != null) {
                acaoPremium.run();
            }
        } else {
            // Usuário não é Premium - bloqueia e redireciona
            Log.d(TAG, "Acesso bloqueado para: " + featureName);
            bloquearERedirecionar(activity, featureName);
        }
    }

    /**
     * Verifica acesso em uma Activity e fecha a activity se não for Premium.
     * Útil para chamar no onCreate de activities protegidas.
     * 
     * @param activity Activity atual
     * @param featureName Nome da funcionalidade
     * @return true se acesso permitido, false se bloqueado (e activity fechada)
     */
    public boolean verificarAcessoEmActivity(Activity activity, String featureName) {
        if (isUsuarioPremium()) {
            return true;
        }
        
        // Bloqueia e mostra o dialog
        bloquearERedirecionar(activity, featureName);
        
        // Configura para fechar a activity quando o dialog for dispensado (cancelado)
        // Como o PremiumBlockDialog não expõe o listener facilmente aqui, 
        // forçamos o fechamento da activity para garantir segurança.
        // O usuário verá o dialog sobre a activity anterior (Menu) ou sobre esta vazia prestes a fechar.
        // A melhor prática é fechar imediatamente para não mostrar conteúdo sensível.
        activity.finish();
        
        return false;
    }

    /**
     * Método interno para lidar com o bloqueio e redirecionamento.
     */
    private void bloquearERedirecionar(Activity activity, String featureName) {
        if (activity == null || activity.isFinishing()) return;

        // Usa o PremiumBlockDialog que agora já possui o botão "Fazer Upgrade"
        // que redireciona diretamente para o fluxo de pagamento.
        PremiumBlockDialog.show(activity, featureName);
    }
}
