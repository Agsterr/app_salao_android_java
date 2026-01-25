package com.focodevsistemas.gerenciamento;

import android.content.Context;

/**
 * Exemplo de uso do SubscriptionService.
 * Este arquivo serve como documentação e pode ser removido em produção.
 */
public class SubscriptionServiceExample {

    /**
     * Exemplo básico de uso do SubscriptionService.
     */
    public static void exemploBasico(Context context) {
        // Obter instância do SubscriptionService
        SubscriptionService subscriptionService = SubscriptionService.getInstance(context);

        // Inicializar o serviço (prepara para billing futuro)
        subscriptionService.initialize(() -> {
            // BillingManager está pronto (quando implementado)
        });

        // Verificar se há assinatura ativa
        boolean temAssinatura = subscriptionService.isSubscriptionActive();

        // Verificar se é assinatura simulada
        boolean isSimulada = subscriptionService.isSimulatedSubscription();
    }

    /**
     * Exemplo de ativação de assinatura PREMIUM (simulado).
     */
    public static void exemploAtivarPremium(Context context) {
        SubscriptionService subscriptionService = SubscriptionService.getInstance(context);
        PlanManager planManager = PlanManager.getInstance(context);

        subscriptionService.activatePremiumSubscription(new SubscriptionService.SubscriptionListener() {
            @Override
            public void onSubscriptionActivated(String productId) {
                // Assinatura ativada com sucesso
                // O PlanManager já foi atualizado automaticamente para PREMIUM
                PlanType planoAtual = planManager.getCurrentPlan();
                // planoAtual será PlanType.PREMIUM
            }

            @Override
            public void onSubscriptionDeactivated() {
                // Não será chamado neste caso
            }

            @Override
            public void onSubscriptionError(String error) {
                // Erro ao ativar assinatura
            }
        });
    }

    /**
     * Exemplo de desativação de assinatura PREMIUM.
     */
    public static void exemploDesativarPremium(Context context) {
        SubscriptionService subscriptionService = SubscriptionService.getInstance(context);
        PlanManager planManager = PlanManager.getInstance(context);

        subscriptionService.deactivatePremiumSubscription(new SubscriptionService.SubscriptionListener() {
            @Override
            public void onSubscriptionActivated(String productId) {
                // Não será chamado neste caso
            }

            @Override
            public void onSubscriptionDeactivated() {
                // Assinatura desativada com sucesso
                // O PlanManager já foi atualizado automaticamente para FREE
                PlanType planoAtual = planManager.getCurrentPlan();
                // planoAtual será PlanType.FREE
            }

            @Override
            public void onSubscriptionError(String error) {
                // Erro ao desativar assinatura
            }
        });
    }

    /**
     * Exemplo de verificação de funcionalidade após ativação.
     */
    public static void exemploVerificarFuncionalidade(Context context) {
        SubscriptionService subscriptionService = SubscriptionService.getInstance(context);
        PlanManager planManager = PlanManager.getInstance(context);

        // Verificar se há assinatura ativa
        if (subscriptionService.isSubscriptionActive()) {
            // Verificar se funcionalidade premium está liberada
            if (planManager.isFeatureEnabled(Feature.EXPORT_DATA)) {
                // Funcionalidade liberada - pode exportar dados
            } else {
                // Funcionalidade bloqueada (não deveria acontecer se assinatura está ativa)
            }
        } else {
            // Sem assinatura - apenas funcionalidades FREE disponíveis
            if (planManager.isFeatureEnabled(Feature.DASHBOARD)) {
                // Dashboard sempre disponível (FREE)
            }
        }
    }

    /**
     * Exemplo de uso em uma Activity para gerenciar assinatura.
     */
    public static void exemploEmActivity(Context context) {
        SubscriptionService subscriptionService = SubscriptionService.getInstance(context);
        PlanManager planManager = PlanManager.getInstance(context);

        // Inicializar serviço
        subscriptionService.initialize(() -> {
            // Quando billing estiver pronto (futuro)
            // Por enquanto, apenas verifica status atual
            subscriptionService.refreshSubscriptionStatus();
        });

        // Verificar status atual
        boolean temAssinatura = subscriptionService.isSubscriptionActive();
        PlanType planoAtual = planManager.getCurrentPlan();

        // Exemplo: Botão para ativar premium
        // buttonAtivarPremium.setOnClickListener(v -> {
        //     subscriptionService.activatePremiumSubscription(listener);
        // });

        // Exemplo: Botão para desativar premium
        // buttonDesativarPremium.setOnClickListener(v -> {
        //     subscriptionService.deactivatePremiumSubscription(listener);
        // });
    }

    /**
     * Exemplo garantindo que app funciona 100% no plano FREE.
     */
    public static void exemploGarantirFree(Context context) {
        PlanManager planManager = PlanManager.getInstance(context);

        // Garantir que está no plano FREE
        planManager.setCurrentPlan(PlanType.FREE);

        // Verificar funcionalidades FREE (sempre devem funcionar)
        boolean podeVerDashboard = planManager.isFeatureEnabled(Feature.DASHBOARD);
        boolean podeVerRelatoriosBasicos = planManager.isFeatureEnabled(Feature.BASIC_REPORTS);
        boolean podeGerenciarClientes = planManager.isFeatureEnabled(Feature.CLIENT_MANAGEMENT);

        // Todas as funcionalidades acima devem retornar true mesmo no plano FREE

        // Funcionalidades PREMIUM devem retornar false no plano FREE
        boolean podeExportar = planManager.isFeatureEnabled(Feature.EXPORT_DATA);
        boolean podeFazerBackup = planManager.isFeatureEnabled(Feature.CLOUD_BACKUP);
        // Essas devem retornar false no plano FREE
    }
}


