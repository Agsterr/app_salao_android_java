# Gerenciamento Total Mais

App Android completo para **pequenos negócios** — controle de clientes, produtos, vendas, recebimentos, agenda e serviços. Tudo funciona localmente no dispositivo, sem depender de internet.

**Package:** `com.focodevsistemas.gerenciamento`

---

## Funcionalidades

### Clientes
- Cadastro, listagem, busca, edição e exclusão

### Produtos
- Cadastro com foto, preços e descrição
- Busca, filtragem e compartilhamento

### Vendas e recebimentos
- Registro de vendas simples e múltiplas
- Parcelamento e status (A Receber / Pago)
- Agrupamento por cliente

### Agenda
- Calendário visual, agendamentos e lembretes
- Totais e controle de lucros

### Serviços
- Cadastro e vínculo com agendamentos

### Segurança e backup
- Login com senha e conta administrador configurada no primeiro acesso
- Backup e restauração local dos dados

### Assinatura premium
- Google Play Billing para controle de acesso premium

---

## Tecnologias

- Java
- Android SDK 36 (mínimo API 28)
- Material Design 3
- SQLite (dados locais)
- AndroidX, WorkManager, Billing Library

---

## Como compilar

### Pré-requisitos

- Android Studio (versão recente)
- JDK 17+
- Android SDK (API 28+)

### Passos

```bash
git clone https://github.com/Agsterr/app_salao_android_java.git
cd app_salao_android_java
```

Abra no Android Studio, sincronize o Gradle e execute:

```bash
./gradlew :app:assembleProdDebug
```

---

## Canais de distribuição (flavors)

| Flavor | Uso |
|--------|-----|
| `prod` | Build de produção |
| `playTest` | Testes na Play Store (assinatura bypassada) |

```bash
./gradlew :app:assembleProdDebug
./gradlew :app:assemblePlayTestDebug
```

---

## Build de release

```bash
./gradlew :app:assembleProdRelease    # APK
./gradlew :app:bundleProdRelease      # AAB para Play Store
```

Assinatura obrigatória via `keystore.properties` (não commitar) ou variáveis de ambiente:

- `RELEASE_STORE_FILE`
- `RELEASE_STORE_PASSWORD`
- `RELEASE_KEY_ALIAS`
- `RELEASE_KEY_PASSWORD`

---

## Assinatura Google Play

O ID do produto é configurado em build-time via `SUBSCRIPTION_PRODUCT_ID` (padrão: `premium_monthly`).

```powershell
# Windows
$env:SUBSCRIPTION_PRODUCT_ID="premium_monthly"
./gradlew :app:assembleProdDebug
```

---

## Documentação adicional

- [GUIA_ASSINATURA_PLAY_STORE.md](GUIA_ASSINATURA_PLAY_STORE.md)
- [RELATORIO_PLAY_STORE.md](RELATORIO_PLAY_STORE.md)
- [Política de privacidade](https://github.com/Agsterr/privacy-policy)

---

## Privacidade

Todos os dados ficam **no dispositivo**. Nenhuma informação é enviada para servidores externos.

---

## Autor

**Focodev Sistemas** — Agster Junior da Costa Santos
