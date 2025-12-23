package com.focodevsistemas.gerenciamento;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Classe central responsável pelo gerenciamento de planos do usuário.
 * Gerencia a persistência e verificação de funcionalidades baseadas no plano ativo.
 */
public class PlanManager {

    // Constantes para SharedPreferences
    private static final String PREFS_NAME = "PlanManagerPrefs";
    private static final String KEY_CURRENT_PLAN = "current_plan";
    
    // Plano padrão
    private static final PlanType DEFAULT_PLAN = PlanType.FREE;

    // Singleton instance
    private static volatile PlanManager instance;

    private final Context appContext;
    private final SharedPreferences sharedPreferences;
    private volatile PlanType currentPlan;

    /**
     * Construtor privado para implementar padrão Singleton.
     * 
     * @param context Contexto da aplicação
     */
    private PlanManager(Context context) {
        this.appContext = context.getApplicationContext();
        this.sharedPreferences = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.currentPlan = loadPlanFromPreferences();
    }

    /**
     * Obtém a instância única do PlanManager (Singleton).
     * 
     * @param context Contexto da aplicação
     * @return Instância do PlanManager
     */
    public static PlanManager getInstance(Context context) {
        if (instance == null) {
            synchronized (PlanManager.class) {
                if (instance == null) {
                    instance = new PlanManager(context);
                }
            }
        }
        return instance;
    }

    /**
     * Verifica se está em modo de teste (DEBUG ou flavor de teste).
     * 
     * @return true se está em modo de teste, false caso contrário
     */
    private boolean isTestMode() {
        try {
            // Verifica se está em modo DEBUG
            if (BuildConfig.DEBUG) {
                return true;
            }
            
            // Verifica se está no flavor de teste
            String channel = BuildConfig.DISTRIBUTION_CHANNEL;
            if ("test".equals(channel)) {
                return true;
            }
        } catch (Exception e) {
            // Se houver erro ao verificar, assume modo de produção
            return false;
        }
        return false;
    }

    /**
     * Retorna o plano atual do usuário.
     * Em modo de teste, sempre retorna PREMIUM.
     * 
     * @return Plano atual (FREE ou PREMIUM)
     */
    public PlanType getCurrentPlan() {
        // Em modo de teste, sempre retorna PREMIUM para liberar todas as funcionalidades
        if (isTestMode()) {
            return PlanType.PREMIUM;
        }
        return currentPlan;
    }

    /**
     * Define o plano atual do usuário e persiste localmente.
     * 
     * @param plan Novo plano a ser definido
     */
    public void setCurrentPlan(PlanType plan) {
        if (plan == null) {
            plan = DEFAULT_PLAN;
        }
        
        this.currentPlan = plan;
        savePlanToPreferences(plan);
    }

    /**
     * Verifica se o usuário possui plano PREMIUM.
     * Em modo de teste, sempre retorna true.
     * 
     * @return true se o plano for PREMIUM, false caso contrário
     */
    public boolean isPremium() {
        // Em modo de teste, sempre retorna true
        if (isTestMode()) {
            return true;
        }
        return currentPlan == PlanType.PREMIUM;
    }

    /**
     * Verifica se o usuário possui plano FREE.
     * Em modo de teste, sempre retorna false.
     * 
     * @return true se o plano for FREE, false caso contrário
     */
    public boolean isFree() {
        // Em modo de teste, sempre retorna false (porque é PREMIUM)
        if (isTestMode()) {
            return false;
        }
        return currentPlan == PlanType.FREE;
    }

    /**
     * Verifica se uma funcionalidade está liberada para o plano atual.
     * Em modo de teste, sempre retorna true.
     * 
     * @param feature Funcionalidade a ser verificada
     * @return true se a funcionalidade está liberada, false caso contrário
     */
    public boolean isFeatureEnabled(Feature feature) {
        if (feature == null) {
            return false;
        }
        
        // Em modo de teste, sempre libera todas as funcionalidades
        if (isTestMode()) {
            return true;
        }
        
        // Funcionalidades FREE estão sempre liberadas
        if (feature.getRequiredPlan() == PlanType.FREE) {
            return true;
        }
        
        // Funcionalidades PREMIUM requerem plano PREMIUM
        if (feature.getRequiredPlan() == PlanType.PREMIUM) {
            return isPremium();
        }
        
        return false;
    }

    /**
     * Verifica se uma funcionalidade está liberada usando o nome da funcionalidade.
     * Método auxiliar para facilitar o uso.
     * 
     * @param featureName Nome da funcionalidade
     * @return true se a funcionalidade está liberada, false caso contrário
     */
    public boolean isFeatureEnabled(String featureName) {
        Feature feature = Feature.fromName(featureName);
        return isFeatureEnabled(feature);
    }

    /**
     * Carrega o plano salvo nas preferências.
     * Se não houver plano salvo, retorna o plano padrão (FREE).
     * 
     * @return Plano carregado ou padrão
     */
    private PlanType loadPlanFromPreferences() {
        String savedPlan = sharedPreferences.getString(KEY_CURRENT_PLAN, null);
        
        if (savedPlan == null) {
            // Primeira execução - salva o plano padrão
            savePlanToPreferences(DEFAULT_PLAN);
            return DEFAULT_PLAN;
        }
        
        try {
            return PlanType.valueOf(savedPlan);
        } catch (IllegalArgumentException e) {
            // Valor inválido - retorna padrão e corrige
            savePlanToPreferences(DEFAULT_PLAN);
            return DEFAULT_PLAN;
        }
    }

    /**
     * Salva o plano nas preferências.
     * 
     * @param plan Plano a ser salvo
     */
    private void savePlanToPreferences(PlanType plan) {
        sharedPreferences.edit()
                .putString(KEY_CURRENT_PLAN, plan.name())
                .apply();
    }

    /**
     * Reseta o plano para o padrão (FREE).
     * Útil para testes ou reset de conta.
     */
    public void resetToDefault() {
        setCurrentPlan(DEFAULT_PLAN);
    }
}

