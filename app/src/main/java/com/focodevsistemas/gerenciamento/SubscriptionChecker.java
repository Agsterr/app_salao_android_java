package com.focodevsistemas.gerenciamento;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Utilitário para verificação de assinatura em qualquer lugar do app
 * Cacheia o resultado para evitar verificações excessivas
 */
public class SubscriptionChecker {
    
    private static final String TAG = "SubscriptionChecker";
    private static final String PREFS_NAME = "SubscriptionPrefs";
    private static final String KEY_IS_SUBSCRIBED = "is_subscribed";
    private static final String KEY_LAST_CHECK = "last_check";
    private static final String KEY_LAST_CHANNEL = "last_channel";
    private static final long CACHE_DURATION_MS = 5 * 60 * 1000; // 5 minutos
    
    private static BillingManager billingManager;
    private static SubscriptionChecker instance;
    private final Context context;
    
    private SubscriptionChecker(Context context) {
        this.context = context.getApplicationContext();
    }
    
    public static synchronized SubscriptionChecker getInstance(Context context) {
        if (instance == null) {
            instance = new SubscriptionChecker(context);
        }
        return instance;
    }
    
    /**
     * Inicializa o BillingManager (deve ser chamado uma vez no início do app)
     */
    public void initialize() {
        if (billingManager == null) {
            billingManager = new BillingManager(context, () -> {
                Log.d(TAG, "BillingManager pronto");
                // Verifica assinatura automaticamente quando pronto
                checkSubscription(null);
            });
        }
    }
    
    /**
     * Verifica se há assinatura ativa (usa cache se disponível)
     * @param listener Callback para receber o resultado
     */
    public void checkSubscription(SubscriptionCheckListener listener) {
        if (shouldBypassSubscription()) {
            saveSubscriptionStatus(true);
            if (listener != null) {
                listener.onResult(true);
            }
            return;
        }

        // Verifica cache primeiro
        if (isCacheValid()) {
            boolean cached = getCachedSubscriptionStatus();
            Log.d(TAG, "Usando resultado em cache: " + cached);
            if (listener != null) {
                listener.onResult(cached);
            }
            return;
        }
        
        // Se não há cache válido, verifica com Play Store
        if (billingManager == null) {
            initialize();
            // Aguarda conexão
            if (listener != null) {
                listener.onResult(false);
            }
            return;
        }
        
        billingManager.verificarAssinaturaAtiva(isSubscribed -> {
            // Salva no cache
            saveSubscriptionStatus(isSubscribed);
            Log.d(TAG, "Verificação concluída: " + isSubscribed);
            if (listener != null) {
                listener.onResult(isSubscribed);
            }
        });
    }
    
    /**
     * Força uma nova verificação (ignora cache)
     */
    public void forceCheck(SubscriptionCheckListener listener) {
        clearCache();
        checkSubscription(listener);
    }
    
    /**
     * Verifica se o cache é válido
     */
    private boolean isCacheValid() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        long lastCheck = prefs.getLong(KEY_LAST_CHECK, 0);
        String lastChannel = prefs.getString(KEY_LAST_CHANNEL, "");
        if (!getDistributionChannel().equals(lastChannel)) {
            return false;
        }
        return (System.currentTimeMillis() - lastCheck) < CACHE_DURATION_MS;
    }
    
    /**
     * Obtém status do cache
     */
    private boolean getCachedSubscriptionStatus() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_IS_SUBSCRIBED, false);
    }

    private boolean shouldBypassSubscription() {
        return "test".equals(getDistributionChannel());
    }

    private String getDistributionChannel() {
        try {
            return BuildConfig.DISTRIBUTION_CHANNEL;
        } catch (Throwable t) {
            return "prod";
        }
    }
    
    /**
     * Salva status da assinatura no cache
     */
    private void saveSubscriptionStatus(boolean isSubscribed) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .putBoolean(KEY_IS_SUBSCRIBED, isSubscribed)
                .putLong(KEY_LAST_CHECK, System.currentTimeMillis())
                .putString(KEY_LAST_CHANNEL, getDistributionChannel())
                .apply();
    }
    
    /**
     * Limpa o cache
     */
    public void clearCache() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .remove(KEY_IS_SUBSCRIBED)
                .remove(KEY_LAST_CHECK)
                .remove(KEY_LAST_CHANNEL)
                .apply();
    }
    
    /**
     * Obtém status atual (pode ser do cache)
     */
    public boolean isSubscribed() {
        if (isCacheValid()) {
            return getCachedSubscriptionStatus();
        }
        return false; // Por padrão retorna false se não houver cache
    }
    
    public interface SubscriptionCheckListener {
        void onResult(boolean isSubscribed);
    }
    
    /**
     * Obtém instância do BillingManager (para uso avançado)
     */
    public BillingManager getBillingManager() {
        if (billingManager == null) {
            initialize();
        }
        return billingManager;
    }
}






