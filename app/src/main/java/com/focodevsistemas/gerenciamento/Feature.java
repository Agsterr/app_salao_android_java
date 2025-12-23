package com.focodevsistemas.gerenciamento;

/**
 * Enum que representa as funcionalidades do aplicativo e o plano necessário para acessá-las.
 */
public enum Feature {
    
    // Funcionalidades FREE (disponíveis para todos)
    BASIC_REPORTS("basic_reports", PlanType.FREE),
    CLIENT_MANAGEMENT("client_management", PlanType.FREE),
    
    // Funcionalidades PREMIUM (requerem assinatura)
    DASHBOARD("dashboard", PlanType.PREMIUM),
    ADVANCED_REPORTS("advanced_reports", PlanType.PREMIUM),
    ALERTS("alerts", PlanType.PREMIUM),
    EXPORT_DATA("export_data", PlanType.PREMIUM),
    CLOUD_BACKUP("cloud_backup", PlanType.PREMIUM),
    PREMIUM_SUPPORT("premium_support", PlanType.PREMIUM),
    CUSTOM_THEMES("custom_themes", PlanType.PREMIUM),
    REMOVE_ADS("remove_ads", PlanType.PREMIUM);

    private final String name;
    private final PlanType requiredPlan;

    Feature(String name, PlanType requiredPlan) {
        this.name = name;
        this.requiredPlan = requiredPlan;
    }

    /**
     * Retorna o nome da funcionalidade.
     * 
     * @return Nome da funcionalidade
     */
    public String getName() {
        return name;
    }

    /**
     * Retorna o plano necessário para acessar esta funcionalidade.
     * 
     * @return Plano necessário (FREE ou PREMIUM)
     */
    public PlanType getRequiredPlan() {
        return requiredPlan;
    }

    /**
     * Obtém uma funcionalidade pelo nome.
     * 
     * @param name Nome da funcionalidade
     * @return Feature correspondente ou null se não encontrada
     */
    public static Feature fromName(String name) {
        if (name == null) {
            return null;
        }
        
        for (Feature feature : values()) {
            if (feature.name.equalsIgnoreCase(name)) {
                return feature;
            }
        }
        
        return null;
    }
}

