package com.focodevsistemas.gerenciamento;

import android.app.Activity;
import android.content.Context;

/**
 * Classe para controlar acesso a funcionalidades baseado no plano do usuário.
 * Usa PlanManager para verificar permissões.
 * 
 * Em modo DEBUG ou TEST, todas as funcionalidades são liberadas automaticamente.
 */
public class FeatureGate {

    private final PlanManager planManager;

    public FeatureGate(Context context) {
        this.planManager = PlanManager.getInstance(context);
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
     * Verifica se o usuário pode acessar relatórios.
     * Requer plano PREMIUM (exceto em modo de teste).
     * 
     * @return true se pode acessar, false caso contrário
     */
    public boolean canAccessReports() {
        // Em modo de teste, sempre libera acesso
        if (isTestMode()) {
            return true;
        }
        return planManager.isFeatureEnabled(Feature.ADVANCED_REPORTS);
    }

    /**
     * Verifica se o usuário pode acessar dashboard.
     * Requer plano PREMIUM (exceto em modo de teste).
     * 
     * @return true se pode acessar, false caso contrário
     */
    public boolean canAccessDashboard() {
        // Em modo de teste, sempre libera acesso
        if (isTestMode()) {
            return true;
        }
        // Dashboard requer PREMIUM conforme requisito
        return planManager.isFeatureEnabled(Feature.DASHBOARD) && planManager.isPremium();
    }

    /**
     * Verifica se o usuário pode acessar alertas.
     * Requer plano PREMIUM (exceto em modo de teste).
     * 
     * @return true se pode acessar, false caso contrário
     */
    public boolean canAccessAlerts() {
        // Em modo de teste, sempre libera acesso
        if (isTestMode()) {
            return true;
        }
        // Alertas requerem PREMIUM
        return planManager.isFeatureEnabled(Feature.ALERTS);
    }

    /**
     * Verifica se o usuário pode exportar dados.
     * Requer plano PREMIUM (exceto em modo de teste).
     * 
     * @return true se pode acessar, false caso contrário
     */
    public boolean canExportData() {
        // Em modo de teste, sempre libera acesso
        if (isTestMode()) {
            return true;
        }
        return planManager.isFeatureEnabled(Feature.EXPORT_DATA);
    }

    /**
     * Verifica acesso e bloqueia se necessário, exibindo dialog de upgrade.
     * Em modo de teste, sempre libera acesso sem mostrar dialog.
     * 
     * @param activity Activity atual
     * @param featureName Nome da funcionalidade
     * @param checkMethod Método de verificação (canAccessReports, canAccessDashboard, etc)
     * @return true se acesso liberado, false se bloqueado
     */
    public boolean checkAccessAndBlock(Activity activity, String featureName, boolean checkMethod) {
        // Em modo de teste, sempre libera acesso
        if (isTestMode()) {
            return true;
        }
        
        if (!checkMethod) {
            // Verificar se a Activity ainda está válida antes de mostrar o dialog
            if (activity != null && !activity.isFinishing() && !activity.isDestroyed()) {
                try {
                    PremiumBlockDialog.show(activity, featureName);
                } catch (Exception e) {
                    android.util.Log.e("FeatureGate", "Erro ao exibir dialog de bloqueio", e);
                }
            }
            return false;
        }
        return true;
    }
    
    /**
     * Verifica acesso e bloqueia se necessário, retornando a instância do dialog.
     * Útil para Activities que precisam gerenciar o ciclo de vida do dialog.
     * 
     * @param activity Activity atual
     * @param featureName Nome da funcionalidade
     * @param checkMethod Método de verificação (canAccessReports, canAccessDashboard, etc)
     * @return Instância do PremiumBlockDialog se bloqueado, null se liberado
     */
    public PremiumBlockDialog checkAccessAndBlockWithDialog(Activity activity, String featureName, boolean checkMethod) {
        // Em modo de teste, sempre libera acesso
        if (isTestMode()) {
            return null;
        }
        
        if (!checkMethod) {
            // Verificar se a Activity ainda está válida antes de mostrar o dialog
            if (activity != null && !activity.isFinishing() && !activity.isDestroyed()) {
                try {
                    return PremiumBlockDialog.show(activity, featureName);
                } catch (Exception e) {
                    android.util.Log.e("FeatureGate", "Erro ao exibir dialog de bloqueio", e);
                }
            }
            return null;
        }
        return null;
    }
}
