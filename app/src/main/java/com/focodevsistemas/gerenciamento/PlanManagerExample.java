package com.focodevsistemas.gerenciamento;

import android.content.Context;

/**
 * Exemplo de uso do PlanManager.
 * Este arquivo serve como documentação e pode ser removido em produção.
 */
public class PlanManagerExample {

    /**
     * Exemplo básico de uso do PlanManager.
     */
    public static void exemploBasico(Context context) {
        // Obter instância do PlanManager
        PlanManager planManager = PlanManager.getInstance(context);

        // Obter o plano atual do usuário
        PlanType planoAtual = planManager.getCurrentPlan();
        // Retorna PlanType.FREE ou PlanType.PREMIUM

        // Verificar se o usuário é premium
        boolean isPremium = planManager.isPremium();

        // Verificar se o usuário é free
        boolean isFree = planManager.isFree();

        // Alterar o plano do usuário
        planManager.setCurrentPlan(PlanType.PREMIUM);
    }

    /**
     * Exemplo de verificação de funcionalidades.
     */
    public static void exemploVerificacaoFuncionalidades(Context context) {
        PlanManager planManager = PlanManager.getInstance(context);

        // Verificar se uma funcionalidade está liberada usando o enum Feature
        boolean podeExportar = planManager.isFeatureEnabled(Feature.EXPORT_DATA);
        boolean podeFazerBackup = planManager.isFeatureEnabled(Feature.CLOUD_BACKUP);
        boolean podeVerDashboard = planManager.isFeatureEnabled(Feature.DASHBOARD);

        // Verificar usando o nome da funcionalidade (string)
        boolean podeRemoverAds = planManager.isFeatureEnabled("remove_ads");

        // Exemplo de uso em uma condição
        if (planManager.isFeatureEnabled(Feature.ADVANCED_REPORTS)) {
            // Mostrar relatórios avançados
        } else {
            // Mostrar mensagem de upgrade para premium
        }
    }

    /**
     * Exemplo de uso em uma Activity.
     */
    public static void exemploEmActivity(Context context) {
        PlanManager planManager = PlanManager.getInstance(context);

        // Verificar antes de acessar uma funcionalidade premium
        if (!planManager.isFeatureEnabled(Feature.CLOUD_BACKUP)) {
            // Mostrar dialog de upgrade
            // ou redirecionar para tela de assinatura
            return;
        }

        // Continuar com a funcionalidade premium
        // fazerBackupNaNuvem();
    }

    /**
     * Exemplo de reset do plano (útil para testes).
     */
    public static void exemploReset(Context context) {
        PlanManager planManager = PlanManager.getInstance(context);
        
        // Resetar para o plano padrão (FREE)
        planManager.resetToDefault();
    }
}

