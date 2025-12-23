package com.focodevsistemas.gerenciamento;

import android.app.Activity;
import android.content.Intent;

/**
 * Exemplos de uso do controle de acesso por funcionalidade.
 * 
 * Este arquivo demonstra como usar o FeatureGate para proteger
 * funcionalidades Premium (Relatórios, Dashboard, Alertas).
 */
public class AccessControlExample {

    /**
     * Exemplo: Verificar acesso antes de abrir Relatórios.
     */
    public static void exemploAbrirRelatorios(Activity activity) {
        FeatureGate featureGate = new FeatureGate(activity);
        
        // Verifica acesso e bloqueia se necessário
        if (!featureGate.checkAccessAndBlock(activity, "Relatórios", featureGate.canAccessReports())) {
            // Acesso bloqueado - dialog já foi exibido
            return;
        }
        
        // Acesso liberado - pode abrir a Activity de Relatórios
        Intent intent = new Intent(activity, AgendaTotaisActivity.class);
        activity.startActivity(intent);
    }

    /**
     * Exemplo: Verificar acesso antes de abrir Dashboard.
     */
    public static void exemploAbrirDashboard(Activity activity) {
        FeatureGate featureGate = new FeatureGate(activity);
        
        // Verifica acesso e bloqueia se necessário
        if (!featureGate.checkAccessAndBlock(activity, "Dashboard", featureGate.canAccessDashboard())) {
            // Acesso bloqueado - dialog já foi exibido
            return;
        }
        
        // Acesso liberado - pode abrir a Activity de Dashboard
        Intent intent = new Intent(activity, MenuActivity.class);
        activity.startActivity(intent);
    }

    /**
     * Exemplo: Verificar acesso antes de abrir Alertas.
     * 
     * Quando uma Activity de Alertas for criada, use este padrão.
     */
    public static void exemploAbrirAlertas(Activity activity) {
        FeatureGate featureGate = new FeatureGate(activity);
        
        // Verifica acesso e bloqueia se necessário
        if (!featureGate.checkAccessAndBlock(activity, "Alertas", featureGate.canAccessAlerts())) {
            // Acesso bloqueado - dialog já foi exibido
            return;
        }
        
        // Acesso liberado - pode abrir a Activity de Alertas
        // Intent intent = new Intent(activity, AlertasActivity.class);
        // activity.startActivity(intent);
    }

    /**
     * Exemplo: Verificar acesso diretamente na Activity (no onCreate).
     * 
     * Este é o padrão usado em AgendaTotaisActivity e MenuActivity.
     */
    public static void exemploVerificacaoNoOnCreate(Activity activity) {
        // Verificar acesso ao Dashboard antes de abrir
        FeatureGate featureGate = new FeatureGate(activity);
        if (!featureGate.checkAccessAndBlock(activity, "Dashboard", featureGate.canAccessDashboard())) {
            // Acesso bloqueado - dialog já foi exibido, fechar Activity
            activity.finish();
            return;
        }
        
        // Continuar com a inicialização normal da Activity
        // setContentView(R.layout.activity_dashboard);
    }

    /**
     * Exemplo: Verificar acesso antes de executar uma ação.
     */
    public static void exemploVerificacaoAntesDeAcao(Activity activity) {
        FeatureGate featureGate = new FeatureGate(activity);
        
        // Verifica se pode exportar dados
        if (!featureGate.canExportData()) {
            // Bloqueia e mostra dialog
            featureGate.checkAccessAndBlock(activity, "Exportar Dados", false);
            return;
        }
        
        // Pode exportar dados
        // exportarDados();
    }

    /**
     * Exemplo: Verificar múltiplas funcionalidades.
     */
    public static void exemploVerificacaoMultipla(Activity activity) {
        FeatureGate featureGate = new FeatureGate(activity);
        
        // Verifica se pode acessar relatórios
        if (!featureGate.canAccessReports()) {
            featureGate.checkAccessAndBlock(activity, "Relatórios", false);
            return;
        }
        
        // Verifica se pode exportar
        if (!featureGate.canExportData()) {
            featureGate.checkAccessAndBlock(activity, "Exportar Dados", false);
            return;
        }
        
        // Todas as verificações passaram
        // executarAcaoCompleta();
    }
}

