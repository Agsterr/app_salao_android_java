# Integração com Google Play Billing - Preparação

Este documento descreve a estrutura preparada para integração futura com Google Play Billing.

## ✅ Estrutura Implementada

### 1. Dependência do Google Play Billing
- ✅ Dependência já adicionada no `build.gradle`:
  ```gradle
  implementation 'com.android.billingclient:billing:8.0.0'
  ```

### 2. Classes Principais

#### `PlanManager`
- Gerencia o plano atual do usuário (FREE ou PREMIUM)
- Persiste localmente usando SharedPreferences
- Verifica se funcionalidades estão liberadas
- **Plano padrão: FREE**

#### `SubscriptionService`
- Gerencia assinaturas mensais
- Métodos simulados para ativar/desativar PREMIUM
- Estrutura preparada para integração futura com BillingManager
- Sincroniza automaticamente com PlanManager

#### `Feature`
- Enum com todas as funcionalidades do app
- Define qual plano é necessário para cada funcionalidade
- Funcionalidades FREE sempre acessíveis
- Funcionalidades PREMIUM requerem assinatura

### 3. Métodos Simulados

#### Ativar Premium (Simulado)
```java
SubscriptionService subscriptionService = SubscriptionService.getInstance(context);
subscriptionService.activatePremiumSubscription(listener);
```

#### Desativar Premium (Simulado)
```java
subscriptionService.deactivatePremiumSubscription(listener);
```

## 🔄 Como Funciona Atualmente

1. **Plano Padrão**: FREE
2. **Ativação Simulada**: 
   - `activatePremiumSubscription()` ativa PREMIUM por 30 dias (simulado)
   - Atualiza automaticamente o `PlanManager`
3. **Desativação Simulada**:
   - `deactivatePremiumSubscription()` retorna para FREE
   - Atualiza automaticamente o `PlanManager`

## 🚀 Próximos Passos para Integração Real

### Passo 1: Configurar Produto no Google Play Console
1. Acesse Google Play Console
2. Vá em **Monetização > Assinaturas**
3. Crie uma assinatura mensal com ID: `premium_monthly`
4. Configure preço e período

### Passo 2: Ativar Billing Real no SubscriptionService

No arquivo `SubscriptionService.java`, descomentar e ativar:

```java
// No método initialize():
if (billingManager == null) {
    billingManager = new BillingManager(appContext, () -> {
        Log.d(TAG, "BillingManager inicializado");
        if (listener != null) {
            listener.onBillingClientReady();
        }
        checkRealSubscription();
    });
}
```

### Passo 3: Implementar Métodos Reais

Substituir métodos simulados por chamadas reais:

#### `activatePremiumSubscription()` - Versão Real
```java
public void activatePremiumSubscription(SubscriptionListener listener) {
    if (billingManager == null || !billingManager.isReady()) {
        initialize(() -> activatePremiumSubscription(listener));
        return;
    }
    
    // Usar BillingManager para iniciar fluxo de compra
    billingManager.queryAndLaunchBillingFlow(
        activity, 
        MONTHLY_SUBSCRIPTION_PRODUCT_ID
    );
}
```

#### `checkRealSubscription()` - Implementação Real
```java
private void checkRealSubscription() {
    if (billingManager != null && billingManager.isReady()) {
        billingManager.verificarAssinaturaAtiva(isSubscribed -> {
            if (isSubscribed) {
                planManager.setCurrentPlan(PlanType.PREMIUM);
                sharedPreferences.edit()
                    .putBoolean(KEY_SUBSCRIPTION_ACTIVE, true)
                    .putBoolean(KEY_IS_SIMULATED, false)
                    .apply();
            } else {
                if (!isSimulatedSubscription() || !isSubscriptionActive()) {
                    planManager.setCurrentPlan(PlanType.FREE);
                    sharedPreferences.edit()
                        .putBoolean(KEY_SUBSCRIPTION_ACTIVE, false)
                        .apply();
                }
            }
        });
    }
}
```

### Passo 4: Processar Compras no BillingManager

No `BillingManager.java`, implementar processamento de compras:

```java
PurchasesUpdatedListener purchasesUpdatedListener = (billingResult, purchases) -> {
    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
        for (Purchase purchase : purchases) {
            // Validar no backend (recomendado)
            // Atualizar SubscriptionService
            SubscriptionService.getInstance(context)
                .onPurchaseCompleted(purchase);
        }
    }
};
```

## ✅ Garantias

### App Funciona 100% no Plano FREE
- ✅ Todas as funcionalidades FREE estão sempre acessíveis
- ✅ App não depende de funcionalidades PREMIUM para funcionar
- ✅ Plano padrão é sempre FREE
- ✅ Validação disponível em `FreePlanValidator`

### Teste de Validação
```java
// Validar que app funciona no plano FREE
boolean isValid = FreePlanValidator.validateAppWorksOnFreePlan(context);

// Testar transição entre planos
boolean transitionWorks = FreePlanValidator.testPlanTransition(context);
```

## 📝 Notas Importantes

1. **Modo Simulado**: Atualmente usa métodos simulados. Quando billing real for ativado, os métodos simulados devem ser substituídos.

2. **Persistência**: O plano é persistido localmente. Quando billing real for ativado, deve verificar com Google Play a cada inicialização.

3. **Segurança**: Para produção, sempre validar compras no backend antes de conceder acesso premium.

4. **Testes**: Use `FreePlanValidator` para garantir que o app funciona corretamente no plano FREE.

## 🔍 Estrutura de Arquivos

```
app/src/main/java/com/focodevsistemas/gerenciamento/
├── PlanManager.java              # Gerencia planos (FREE/PREMIUM)
├── SubscriptionService.java      # Gerencia assinaturas (preparado para billing)
├── Feature.java                  # Enum de funcionalidades
├── PlanType.java                 # Enum de planos (FREE, PREMIUM)
├── BillingManager.java           # Cliente Google Play Billing (já existe)
├── FreePlanValidator.java        # Validação de funcionamento no FREE
└── SubscriptionServiceExample.java # Exemplos de uso
```

## 📚 Exemplos de Uso

Ver arquivo `SubscriptionServiceExample.java` para exemplos completos de:
- Inicialização do serviço
- Ativação/desativação de assinatura
- Verificação de funcionalidades
- Uso em Activities


