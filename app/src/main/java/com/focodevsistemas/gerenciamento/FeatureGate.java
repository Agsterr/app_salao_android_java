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
     * Verifica se o usuário pode acessar relatórios.
     * Requer plano PREMIUM (exceto em modo de teste controlado pelo PlanManager).
     * 
     * @return true se pode acessar, false caso contrário
     */
    public boolean canAccessReports() {
        return planManager.isFeatureEnabled(Feature.ADVANCED_REPORTS);
    }

    /**
     * Verifica se o usuário pode acessar dashboard.
     * Requer plano PREMIUM (exceto em modo de teste controlado pelo PlanManager).
     * 
     * @return true se pode acessar, false caso contrário
     */
    public boolean canAccessDashboard() {
        return planManager.isFeatureEnabled(Feature.DASHBOARD);
    }

    /**
     * Verifica se o usuário pode acessar alertas.
     * Requer plano PREMIUM (exceto em modo de teste controlado pelo PlanManager).
     * 
     * @return true se pode acessar, false caso contrário
     */
    public boolean canAccessAlerts() {
        return planManager.isFeatureEnabled(Feature.ALERTS);
    }

    /**
     * Verifica se o usuário pode exportar dados.
     * Requer plano PREMIUM (exceto em modo de teste controlado pelo PlanManager).
     * 
     * @return true se pode acessar, false caso contrário
     */
    public boolean canExportData() {
        return planManager.isFeatureEnabled(Feature.EXPORT_DATA);
    }

    /**
     * Verifica acesso e bloqueia se necessário, exibindo dialog de upgrade.
     * O PlanManager decide se libera ou não (baseado em plano ou modo de teste).
     * 
     * @param activity Activity atual
     * @param featureName Nome da funcionalidade
     * @param checkMethod Método de verificação (canAccessReports, canAccessDashboard, etc)
     * @return true se acesso liberado, false se bloqueado
     */
    public boolean checkAccessAndBlock(Activity activity, String featureName, boolean checkMethod) {
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
