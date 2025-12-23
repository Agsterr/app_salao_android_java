package com.focodevsistemas.gerenciamento;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Serviço centralizado para gerenciamento de assinaturas mensais.
 * 
 * Esta classe prepara a estrutura para integração futura com Google Play Billing.
 * Atualmente utiliza métodos simulados para ativar/desativar o plano PREMIUM.
 * 
 * Quando o Google Play Billing for implementado, os métodos simulados devem ser
 * substituídos pelas chamadas reais ao BillingManager.
 */
public class SubscriptionService {

    private static final String TAG = "SubscriptionService";
    private static final String PREFS_NAME = "SubscriptionServicePrefs";
    private static final String KEY_SUBSCRIPTION_ACTIVE = "subscription_active";
    private static final String KEY_SUBSCRIPTION_PRODUCT_ID = "subscription_product_id";
    private static final String KEY_SUBSCRIPTION_EXPIRY = "subscription_expiry";
    private static final String KEY_IS_SIMULATED = "is_simulated";

    // ID do produto de assinatura mensal (deve corresponder ao configurado no Google Play Console)
    private static final String MONTHLY_SUBSCRIPTION_PRODUCT_ID = "premium_monthly";

    private static volatile SubscriptionService instance;
    private final Context appContext;
    private final SharedPreferences sharedPreferences;
    private final PlanManager planManager;
    private BillingManager billingManager;

    /**
     * Listener para eventos de assinatura
     */
    public interface SubscriptionListener {
        /**
         * Chamado quando a assinatura é ativada
         * 
         * @param productId ID do produto de assinatura
         */
        void onSubscriptionActivated(String productId);

        /**
         * Chamado quando a assinatura é desativada
         */
        void onSubscriptionDeactivated();

        /**
         * Chamado quando ocorre um erro na assinatura
         * 
         * @param error Mensagem de erro
         */
        void onSubscriptionError(String error);
    }

    private SubscriptionService(Context context) {
        this.appContext = context.getApplicationContext();
        this.sharedPreferences = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.planManager = PlanManager.getInstance(context);
        
        // Sincroniza o plano atual com o status da assinatura
        syncPlanWithSubscription();
    }

    /**
     * Obtém a instância única do SubscriptionService (Singleton).
     * 
     * @param context Contexto da aplicação
     * @return Instância do SubscriptionService
     */
    public static SubscriptionService getInstance(Context context) {
        if (instance == null) {
            synchronized (SubscriptionService.class) {
                if (instance == null) {
                    instance = new SubscriptionService(context);
                }
            }
        }
        return instance;
    }

    /**
     * Inicializa o serviço de assinatura.
     * Inicializa o BillingManager para verificação real de assinaturas.
     * 
     * @param listener Listener para eventos de billing (pode ser null)
     */
    public void initialize(BillingManager.BillingReadyListener listener) {
        if (billingManager == null) {
            billingManager = new BillingManager(appContext, () -> {
                Log.d(TAG, "BillingManager inicializado");
                if (listener != null) {
                    listener.onBillingClientReady();
                }
                // Verifica assinatura real quando billing estiver pronto
                checkRealSubscription();
            });
        } else {
            // Se já está inicializado, apenas verifica assinatura
            if (billingManager.isReady()) {
                checkRealSubscription();
                if (listener != null) {
                    listener.onBillingClientReady();
                }
            }
        }
    }

    /**
     * Ativa o plano PREMIUM iniciando o fluxo de compra do Google Play Billing.
     * 
     * Se o BillingManager estiver disponível e pronto, inicia o fluxo real de compra.
     * Caso contrário, usa método simulado para desenvolvimento/testes.
     * 
     * @param activity Activity necessária para iniciar o fluxo de compra
     * @param listener Listener para receber o resultado da operação
     */
    public void activatePremiumSubscription(android.app.Activity activity, SubscriptionListener listener) {
        if (billingManager != null && billingManager.isReady()) {
            // Usa billing real
            Log.d(TAG, "Iniciando fluxo de compra real do Google Play Billing");
            billingManager.queryAndLaunchBillingFlow(activity, MONTHLY_SUBSCRIPTION_PRODUCT_ID);
            
            // O resultado será processado pelo PurchasesUpdatedListener no BillingManager
            // que deve chamar onPurchaseCompleted() quando a compra for concluída
            if (listener != null) {
                // Armazena listener temporariamente para quando compra for concluída
                // Nota: Em produção, isso deve ser gerenciado de forma mais robusta
                Log.d(TAG, "Fluxo de compra iniciado, aguardando resultado do Google Play");
            }
        } else {
            // Fallback para método simulado (desenvolvimento/testes)
            Log.d(TAG, "BillingManager não disponível, usando método simulado");
            activatePremiumSubscriptionSimulated(listener);
        }
    }
    
    /**
     * Método simulado para ativar assinatura (usado quando billing real não está disponível).
     * 
     * @param listener Listener para receber o resultado da operação
     */
    private void activatePremiumSubscriptionSimulated(SubscriptionListener listener) {
        Log.d(TAG, "Ativando assinatura PREMIUM (simulado)");
        
        try {
            // Simula ativação da assinatura
            long expiryTime = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000); // 30 dias
            
            sharedPreferences.edit()
                    .putBoolean(KEY_SUBSCRIPTION_ACTIVE, true)
                    .putString(KEY_SUBSCRIPTION_PRODUCT_ID, MONTHLY_SUBSCRIPTION_PRODUCT_ID)
                    .putLong(KEY_SUBSCRIPTION_EXPIRY, expiryTime)
                    .putBoolean(KEY_IS_SIMULATED, true)
                    .apply();
            
            // Atualiza o PlanManager
            planManager.setCurrentPlan(PlanType.PREMIUM);
            
            Log.d(TAG, "Assinatura PREMIUM ativada com sucesso (simulado)");
            
            if (listener != null) {
                listener.onSubscriptionActivated(MONTHLY_SUBSCRIPTION_PRODUCT_ID);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Erro ao ativar assinatura PREMIUM", e);
            if (listener != null) {
                listener.onSubscriptionError("Erro ao ativar assinatura: " + e.getMessage());
            }
        }
    }
    
    /**
     * Método sobrecarregado para manter compatibilidade com código existente.
     * Usa método simulado se Activity não for fornecida.
     */
    public void activatePremiumSubscription(SubscriptionListener listener) {
        activatePremiumSubscriptionSimulated(listener);
    }
    
    /**
     * Processa uma compra concluída do Google Play Billing.
     * Deve ser chamado pelo BillingManager quando uma compra for bem-sucedida.
     * 
     * @param purchase Purchase object do Google Play Billing
     */
    public void onPurchaseCompleted(com.android.billingclient.api.Purchase purchase) {
        if (purchase != null && purchase.getPurchaseState() == com.android.billingclient.api.Purchase.PurchaseState.PURCHASED) {
            Log.d(TAG, "Compra concluída: " + purchase.getOrderId());
            
            // Atualiza status da assinatura
            sharedPreferences.edit()
                    .putBoolean(KEY_SUBSCRIPTION_ACTIVE, true)
                    .putString(KEY_SUBSCRIPTION_PRODUCT_ID, purchase.getProducts().isEmpty() ? 
                            MONTHLY_SUBSCRIPTION_PRODUCT_ID : purchase.getProducts().get(0))
                    .putBoolean(KEY_IS_SIMULATED, false)
                    .remove(KEY_SUBSCRIPTION_EXPIRY) // Assinaturas reais não têm expiry local
                    .apply();
            
            // Atualiza o PlanManager
            planManager.setCurrentPlan(PlanType.PREMIUM);
            
            Log.d(TAG, "Assinatura PREMIUM ativada via Google Play Billing");
        }
    }

    /**
     * Desativa o plano PREMIUM e retorna para FREE.
     * 
     * Este método simula o cancelamento de uma assinatura.
     * Quando o Google Play Billing for implementado, este método deve ser
     * substituído por uma chamada real ao BillingManager.
     * 
     * @param listener Listener para receber o resultado da operação
     */
    public void deactivatePremiumSubscription(SubscriptionListener listener) {
        Log.d(TAG, "Desativando assinatura PREMIUM (simulado)");
        
        try {
            // Remove dados da assinatura
            sharedPreferences.edit()
                    .putBoolean(KEY_SUBSCRIPTION_ACTIVE, false)
                    .remove(KEY_SUBSCRIPTION_PRODUCT_ID)
                    .remove(KEY_SUBSCRIPTION_EXPIRY)
                    .putBoolean(KEY_IS_SIMULATED, false)
                    .apply();
            
            // Atualiza o PlanManager para FREE
            planManager.setCurrentPlan(PlanType.FREE);
            
            Log.d(TAG, "Assinatura PREMIUM desativada com sucesso (simulado)");
            
            if (listener != null) {
                listener.onSubscriptionDeactivated();
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Erro ao desativar assinatura PREMIUM", e);
            if (listener != null) {
                listener.onSubscriptionError("Erro ao desativar assinatura: " + e.getMessage());
            }
        }
    }

    /**
     * Verifica se há uma assinatura ativa.
     * 
     * @return true se há assinatura ativa, false caso contrário
     */
    public boolean isSubscriptionActive() {
        boolean isActive = sharedPreferences.getBoolean(KEY_SUBSCRIPTION_ACTIVE, false);
        
        if (isActive) {
            // Verifica se a assinatura não expirou (se for simulada)
            if (isSimulatedSubscription()) {
                long expiry = sharedPreferences.getLong(KEY_SUBSCRIPTION_EXPIRY, 0);
                if (expiry > 0 && System.currentTimeMillis() > expiry) {
                    // Assinatura expirada - desativa automaticamente
                    Log.d(TAG, "Assinatura simulada expirada, desativando");
                    deactivatePremiumSubscription(null);
                    return false;
                }
            }
        }
        
        return isActive;
    }

    /**
     * Verifica se a assinatura atual é simulada.
     * 
     * @return true se é simulada, false se for real (quando billing for implementado)
     */
    public boolean isSimulatedSubscription() {
        return sharedPreferences.getBoolean(KEY_IS_SIMULATED, true);
    }

    /**
     * Obtém o ID do produto de assinatura ativo.
     * 
     * @return ID do produto ou null se não houver assinatura
     */
    public String getSubscriptionProductId() {
        if (isSubscriptionActive()) {
            return sharedPreferences.getString(KEY_SUBSCRIPTION_PRODUCT_ID, MONTHLY_SUBSCRIPTION_PRODUCT_ID);
        }
        return null;
    }

    /**
     * Obtém o tempo de expiração da assinatura (apenas para assinaturas simuladas).
     * 
     * @return Timestamp de expiração ou 0 se não houver assinatura
     */
    public long getSubscriptionExpiry() {
        if (isSubscriptionActive() && isSimulatedSubscription()) {
            return sharedPreferences.getLong(KEY_SUBSCRIPTION_EXPIRY, 0);
        }
        return 0;
    }

    /**
     * Sincroniza o plano no PlanManager com o status da assinatura.
     * Garante que o plano está sempre consistente com a assinatura.
     */
    private void syncPlanWithSubscription() {
        if (isSubscriptionActive()) {
            // Se há assinatura ativa, garante que o plano é PREMIUM
            if (planManager.getCurrentPlan() != PlanType.PREMIUM) {
                planManager.setCurrentPlan(PlanType.PREMIUM);
                Log.d(TAG, "Plano sincronizado: PREMIUM (assinatura ativa)");
            }
        } else {
            // Se não há assinatura, garante que o plano é FREE
            if (planManager.getCurrentPlan() != PlanType.FREE) {
                planManager.setCurrentPlan(PlanType.FREE);
                Log.d(TAG, "Plano sincronizado: FREE (sem assinatura)");
            }
        }
    }

    /**
     * Verifica e atualiza o status da assinatura.
     * Chama syncPlanWithSubscription para manter consistência.
     */
    public void refreshSubscriptionStatus() {
        syncPlanWithSubscription();
    }

    /**
     * Verifica assinatura real no Google Play Billing.
     * 
     * Este método verifica com o Google Play se há uma assinatura ativa
     * e atualiza o PlanManager e SharedPreferences accordingly.
     */
    private void checkRealSubscription() {
        if (billingManager == null) {
            Log.w(TAG, "BillingManager não inicializado, usando dados locais");
            syncPlanWithSubscription();
            return;
        }
        
        if (!billingManager.isReady()) {
            Log.w(TAG, "BillingManager não está pronto, usando dados locais");
            syncPlanWithSubscription();
            return;
        }
        
        billingManager.verificarAssinaturaAtiva(isSubscribed -> {
            if (isSubscribed) {
                // Assinatura real ativa - atualiza PlanManager
                Log.d(TAG, "Assinatura real ativa detectada");
                planManager.setCurrentPlan(PlanType.PREMIUM);
                sharedPreferences.edit()
                        .putBoolean(KEY_SUBSCRIPTION_ACTIVE, true)
                        .putBoolean(KEY_IS_SIMULATED, false)
                        .putString(KEY_SUBSCRIPTION_PRODUCT_ID, MONTHLY_SUBSCRIPTION_PRODUCT_ID)
                        .apply();
            } else {
                // Sem assinatura real - verifica se há simulada
                Log.d(TAG, "Nenhuma assinatura real ativa");
                if (isSimulatedSubscription() && isSubscriptionActive()) {
                    // Mantém assinatura simulada se ainda estiver ativa
                    Log.d(TAG, "Mantendo assinatura simulada ativa");
                } else {
                    // Não há assinatura real nem simulada válida - volta para FREE
                    planManager.setCurrentPlan(PlanType.FREE);
                    sharedPreferences.edit()
                            .putBoolean(KEY_SUBSCRIPTION_ACTIVE, false)
                            .putBoolean(KEY_IS_SIMULATED, false)
                            .apply();
                }
            }
        });
    }

    /**
     * Obtém o BillingManager (para uso futuro quando billing real for implementado).
     * 
     * @return BillingManager ou null se não estiver inicializado
     */
    public BillingManager getBillingManager() {
        return billingManager;
    }

    /**
     * Reseta todas as assinaturas (útil para testes).
     * Desativa assinatura e retorna para plano FREE.
     */
    public void resetSubscription() {
        Log.d(TAG, "Resetando assinatura");
        sharedPreferences.edit().clear().apply();
        planManager.resetToDefault();
    }
}

