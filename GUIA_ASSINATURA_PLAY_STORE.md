# 📱 Guia de Configuração de Assinatura - Google Play Store

## ✅ Sistema de Verificação Implementado

O app agora possui um sistema completo de verificação de assinatura da Play Store com:

1. **BillingManager** - Gerencia conexão com Google Play Billing
2. **SubscriptionChecker** - Utilitário para verificação em qualquer lugar do app
3. **Verificação automática** - Após login e periodicamente durante uso
4. **Cache inteligente** - Evita verificações excessivas (cache de 5 minutos)

---

## 🔧 Configuração Necessária

### 1. Criar Produto de Assinatura no Play Console

1. Acesse [Google Play Console](https://play.google.com/console)
2. Selecione seu app
3. Vá em **Monetização** → **Produtos** → **Assinaturas**
4. Clique em **Criar assinatura**
5. Preencha:
   - **ID do produto**: Ex: `premium_monthly`, `premium_annual`
   - **Nome**: Ex: "Plano Premium Mensal"
   - **Descrição**: Descrição do plano
   - **Preço**: Defina o preço
   - **Período**: Mensal, Anual, etc.

### 2. Atualizar ID do Produto no Código

Edite o arquivo `SubscriptionActivity.java`:

```java
// Linha 20 - Substitua pelo ID real criado no Play Console
private static final String ID_PRODUTO_ASSINATURA = "premium_monthly";
```

**Importante:** O ID deve ser exatamente igual ao criado no Play Console!

### 3. Testar Assinatura

#### Modo de Teste (Recomendado para desenvolvimento)

1. No Play Console, vá em **Configuração** → **Acesso à API**
2. Adicione contas de teste (emails do Gmail)
3. Faça upload de uma versão de teste (Internal Testing ou Closed Testing)
4. Instale o app na conta de teste
5. As assinaturas serão gratuitas para contas de teste

#### IDs de Teste do Google

O Google fornece IDs de teste que sempre retornam sucesso:
- `android.test.purchased` - Compra bem-sucedida
- `android.test.canceled` - Compra cancelada
- `android.test.refunded` - Compra reembolsada

**⚠️ Não use IDs de teste em produção!**

---

## 🔄 Fluxo de Verificação

### 1. Login
```
LoginActivity → Verifica assinatura → 
  ├─ Assinatura ativa → MenuActivity
  └─ Sem assinatura → SubscriptionActivity
```

### 2. Durante Uso
```
MenuActivity (onResume) → Verifica assinatura →
  ├─ Assinatura ativa → Continua usando
  └─ Assinatura expirada → SubscriptionActivity
```

### 3. Compra de Assinatura
```
SubscriptionActivity → 
  ├─ Usuário clica "Assinar" → 
  ├─ Google Play Billing Flow → 
  ├─ Compra bem-sucedida → 
  └─ MenuActivity (acesso liberado)
```

---

## 📝 Métodos Disponíveis

### SubscriptionChecker

```java
// Verificar assinatura (usa cache se disponível)
SubscriptionChecker checker = SubscriptionChecker.getInstance(context);
checker.checkSubscription(isSubscribed -> {
    if (isSubscribed) {
        // Usuário tem assinatura ativa
    }
});

// Forçar verificação (ignora cache)
checker.forceCheck(isSubscribed -> {
    // ...
});

// Verificar status atual (pode ser do cache)
boolean isSubscribed = checker.isSubscribed();

// Limpar cache
checker.clearCache();
```

### BillingManager

```java
// Verificar assinatura diretamente
BillingManager billing = new BillingManager(context, () -> {
    // BillingClient pronto
    billing.verificarAssinaturaAtiva(isSubscribed -> {
        // ...
    });
});

// Iniciar fluxo de compra
billing.queryAndLaunchBillingFlow(activity, "premium_monthly");
```

---

## 🛡️ Segurança

### Validação no Backend (Recomendado)

Para máxima segurança, valide as compras no seu servidor:

1. Quando uma compra é feita, o Google retorna um `purchaseToken`
2. Envie este token para seu servidor
3. Seu servidor valida com a API do Google Play
4. Só então conceda acesso premium

**Exemplo de validação no backend:**
```java
// No BillingManager, após compra bem-sucedida
String purchaseToken = purchase.getPurchaseToken();
// Enviar para seu servidor para validação
```

### Validação Local (Atual)

O sistema atual valida localmente, o que é suficiente para a maioria dos casos, mas menos seguro que validação em servidor.

---

## 🐛 Troubleshooting

### Problema: "BillingClient não está pronto"

**Solução:**
- Aguarde alguns segundos após inicializar
- Verifique conexão com internet
- Certifique-se de que o Google Play Services está atualizado

### Problema: "Falha ao consultar detalhes do produto"

**Solução:**
- Verifique se o ID do produto está correto
- Certifique-se de que o produto foi criado no Play Console
- Verifique se o app está publicado (ou em teste interno)

### Problema: Assinatura não é detectada

**Solução:**
- Limpe o cache: `SubscriptionChecker.getInstance(context).clearCache()`
- Force nova verificação: `checker.forceCheck(...)`
- Verifique se a conta de teste está configurada corretamente

---

## 📊 Estados de Assinatura

- **PURCHASED** - Assinatura ativa e paga
- **PENDING** - Pagamento pendente (ainda permite acesso)
- **CANCELED** - Assinatura cancelada
- **EXPIRED** - Assinatura expirada

O sistema atual trata `PURCHASED` e `PENDING` como assinaturas válidas.

---

## ✅ Checklist de Publicação

- [ ] Criar produto de assinatura no Play Console
- [ ] Atualizar `ID_PRODUTO_ASSINATURA` no código
- [ ] Testar com conta de teste
- [ ] Verificar fluxo completo (login → verificação → compra → acesso)
- [ ] Testar expiração de assinatura
- [ ] Publicar versão de produção

---

## 📚 Recursos

- [Documentação Google Play Billing](https://developer.android.com/google/play/billing)
- [Guia de Assinaturas](https://developer.android.com/google/play/billing/subscriptions)
- [Teste de Assinaturas](https://developer.android.com/google/play/billing/test)

---

**Desenvolvido por Focodev Sistemas**





