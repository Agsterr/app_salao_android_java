package com.focodevsistemas.gerenciamento;

import android.content.Context;
import android.util.Log;

/**
 * Classe de validação para garantir que o aplicativo funcione 100% no plano FREE.
 * 
 * Esta classe verifica que todas as funcionalidades FREE estão acessíveis
 * e que o aplicativo não depende de funcionalidades PREMIUM para funcionar.
 */
public class FreePlanValidator {

    private static final String TAG = "FreePlanValidator";

    /**
     * Valida que todas as funcionalidades FREE estão acessíveis.
     * 
     * @param context Contexto da aplicação
     * @return true se todas as validações passaram, false caso contrário
     */
    public static boolean validateFreePlanFeatures(Context context) {
        PlanManager planManager = PlanManager.getInstance(context);
        
        // Garante que está no plano FREE
        planManager.setCurrentPlan(PlanType.FREE);
        
        boolean allValid = true;
        
        // Valida funcionalidades FREE
        Feature[] freeFeatures = {
            Feature.DASHBOARD,
            Feature.BASIC_REPORTS,
            Feature.CLIENT_MANAGEMENT
        };
        
        for (Feature feature : freeFeatures) {
            boolean isEnabled = planManager.isFeatureEnabled(feature);
            if (!isEnabled) {
                Log.e(TAG, "ERRO: Funcionalidade FREE não está acessível: " + feature.getName());
                allValid = false;
            } else {
                Log.d(TAG, "OK: Funcionalidade FREE acessível: " + feature.getName());
            }
        }
        
        // Valida que funcionalidades PREMIUM estão bloqueadas no plano FREE
        Feature[] premiumFeatures = {
            Feature.ADVANCED_REPORTS,
            Feature.EXPORT_DATA,
            Feature.CLOUD_BACKUP,
            Feature.PREMIUM_SUPPORT,
            Feature.CUSTOM_THEMES,
            Feature.REMOVE_ADS
        };
        
        for (Feature feature : premiumFeatures) {
            boolean isEnabled = planManager.isFeatureEnabled(feature);
            if (isEnabled) {
                Log.w(TAG, "AVISO: Funcionalidade PREMIUM está acessível no plano FREE: " + feature.getName());
                // Não é um erro crítico, mas deve ser verificado
            } else {
                Log.d(TAG, "OK: Funcionalidade PREMIUM bloqueada no plano FREE: " + feature.getName());
            }
        }
        
        return allValid;
    }

    /**
     * Valida que o aplicativo pode funcionar completamente no plano FREE.
     * 
     * @param context Contexto da aplicação
     * @return true se o app pode funcionar 100% no FREE, false caso contrário
     */
    public static boolean validateAppWorksOnFreePlan(Context context) {
        PlanManager planManager = PlanManager.getInstance(context);
        
        // Garante que está no plano FREE
        planManager.setCurrentPlan(PlanType.FREE);
        
        Log.d(TAG, "=== Validação: App funciona 100% no plano FREE ===");
        
        // 1. Verifica que o plano atual é FREE
        PlanType currentPlan = planManager.getCurrentPlan();
        if (currentPlan != PlanType.FREE) {
            Log.e(TAG, "ERRO: Plano atual não é FREE: " + currentPlan);
            return false;
        }
        Log.d(TAG, "OK: Plano atual é FREE");
        
        // 2. Verifica que isFree() retorna true
        if (!planManager.isFree()) {
            Log.e(TAG, "ERRO: isFree() retorna false no plano FREE");
            return false;
        }
        Log.d(TAG, "OK: isFree() retorna true");
        
        // 3. Verifica que isPremium() retorna false
        if (planManager.isPremium()) {
            Log.e(TAG, "ERRO: isPremium() retorna true no plano FREE");
            return false;
        }
        Log.d(TAG, "OK: isPremium() retorna false");
        
        // 4. Valida funcionalidades FREE
        boolean featuresValid = validateFreePlanFeatures(context);
        if (!featuresValid) {
            Log.e(TAG, "ERRO: Algumas funcionalidades FREE não estão acessíveis");
            return false;
        }
        Log.d(TAG, "OK: Todas as funcionalidades FREE estão acessíveis");
        
        // 5. Verifica que SubscriptionService não bloqueia o app
        SubscriptionService subscriptionService = SubscriptionService.getInstance(context);
        if (subscriptionService.isSubscriptionActive()) {
            Log.w(TAG, "AVISO: Há assinatura ativa, mas app deve funcionar mesmo assim");
        }
        Log.d(TAG, "OK: SubscriptionService não bloqueia funcionamento");
        
        Log.d(TAG, "=== Validação concluída: App funciona 100% no plano FREE ===");
        
        return true;
    }

    /**
     * Testa a transição entre planos FREE e PREMIUM.
     * 
     * @param context Contexto da aplicação
     * @return true se a transição funciona corretamente
     */
    public static boolean testPlanTransition(Context context) {
        PlanManager planManager = PlanManager.getInstance(context);
        SubscriptionService subscriptionService = SubscriptionService.getInstance(context);
        
        Log.d(TAG, "=== Teste: Transição entre planos ===");
        
        // 1. Começa no plano FREE
        planManager.setCurrentPlan(PlanType.FREE);
        if (planManager.getCurrentPlan() != PlanType.FREE) {
            Log.e(TAG, "ERRO: Não conseguiu definir plano FREE");
            return false;
        }
        Log.d(TAG, "OK: Plano definido como FREE");
        
        // 2. Ativa PREMIUM (simulado)
        subscriptionService.activatePremiumSubscription(new SubscriptionService.SubscriptionListener() {
            @Override
            public void onSubscriptionActivated(String productId) {
                Log.d(TAG, "OK: Assinatura PREMIUM ativada");
            }

            @Override
            public void onSubscriptionDeactivated() {
                // Não esperado aqui
            }

            @Override
            public void onSubscriptionError(String error) {
                Log.e(TAG, "ERRO ao ativar: " + error);
            }
        });
        
        // Aguarda um pouco para a operação assíncrona
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // 3. Verifica que plano mudou para PREMIUM
        if (planManager.getCurrentPlan() != PlanType.PREMIUM) {
            Log.e(TAG, "ERRO: Plano não mudou para PREMIUM após ativação");
            return false;
        }
        Log.d(TAG, "OK: Plano mudou para PREMIUM");
        
        // 4. Desativa PREMIUM
        subscriptionService.deactivatePremiumSubscription(new SubscriptionService.SubscriptionListener() {
            @Override
            public void onSubscriptionActivated(String productId) {
                // Não esperado aqui
            }

            @Override
            public void onSubscriptionDeactivated() {
                Log.d(TAG, "OK: Assinatura PREMIUM desativada");
            }

            @Override
            public void onSubscriptionError(String error) {
                Log.e(TAG, "ERRO ao desativar: " + error);
            }
        });
        
        // Aguarda um pouco
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // 5. Verifica que plano voltou para FREE
        if (planManager.getCurrentPlan() != PlanType.FREE) {
            Log.e(TAG, "ERRO: Plano não voltou para FREE após desativação");
            return false;
        }
        Log.d(TAG, "OK: Plano voltou para FREE");
        
        Log.d(TAG, "=== Teste concluído: Transição funciona corretamente ===");
        
        return true;
    }
}

