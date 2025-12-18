# 📱 Gerenciamento Total Mais

Sistema completo de gerenciamento para pequenos negócios, incluindo controle de clientes, produtos, vendas, recebimentos e agenda.

## 🎯 Funcionalidades

### 👥 Gestão de Clientes
- Cadastro completo de clientes
- Listagem e busca de clientes
- Edição e exclusão de registros

### 📦 Gestão de Produtos
- Cadastro de produtos com foto
- Definição de preços padrão
- Descrição detalhada
- Busca e filtragem
- Compartilhamento de produtos

### 💰 Vendas e Recebimentos
- Registro de vendas
- Vendas múltiplas (vários produtos)
- Controle de recebimentos
- Parcelamento de vendas
- Status de pagamento (A Receber / Pago)
- Agrupamento por cliente

### 📅 Agenda e Agendamentos
- Agenda pessoal
- Agendamentos de serviços
- Calendário visual
- Lembretes e notificações
- Totais e lucros

### 🔧 Serviços
- Cadastro de serviços
- Gestão de serviços oferecidos
- Vinculação com agendamentos

### 🔐 Segurança
- Sistema de login com senha
- Conta administrador padrão
- Alteração de senha
- Proteção de dados locais

### 💾 Backup
- Sistema de backup dos dados
- Restauração de backup

## 🚀 Como Compilar

### Pré-requisitos
- Android Studio (versão mais recente)
- JDK 17 ou superior
- Android SDK (API 28+)

### Passos

1. **Clone o repositório**
   ```bash
   git clone [seu-repositorio]
   cd appDeTestes
   ```

2. **Abra no Android Studio**
   - File → Open → Selecione a pasta do projeto

3. **Sincronize o Gradle**
   - O Android Studio irá sincronizar automaticamente
   - Aguarde o download das dependências

4. **Compile o projeto**
   - Build → Make Project (Ctrl+F9)
   - Ou execute: `./gradlew build`

5. **Execute no dispositivo/emulador**
   - Conecte um dispositivo Android ou inicie um emulador
   - Clique em Run (Shift+F10)

## 📦 Build de Release

Para gerar um APK/AAB para publicação:

```bash
./gradlew assembleRelease
```

O arquivo será gerado em: `app/build/outputs/apk/release/app-release.apk`

Para gerar um AAB (Android App Bundle):

```bash
./gradlew bundleRelease
```

O arquivo será gerado em: `app/build/outputs/bundle/release/app-release.aab`

## 🔑 Credenciais Padrão

**Usuário:** `admin`  
**Senha:** `admin`

⚠️ **Importante:** Altere a senha padrão após o primeiro acesso!

## 📋 Requisitos do Sistema

- **Android mínimo:** 9.0 (API 28)
- **Android alvo:** 14.0+ (API 36)
- **Permissões necessárias:**
  - Câmera (para fotos de produtos)
  - Armazenamento (para salvar imagens)
  - Notificações (para lembretes)
  - Internet (para verificação de atualizações)

## 🛠️ Tecnologias Utilizadas

- **Linguagem:** Java
- **UI:** Material Design 3
- **Banco de Dados:** SQLite
- **Arquitetura:** MVC (Model-View-Controller)
- **Bibliotecas:**
  - AndroidX AppCompat
  - Material Components
  - WorkManager (para tarefas em background)
  - Billing Library (para assinaturas)

## 📁 Estrutura do Projeto

```
app/
├── src/
│   ├── main/
│   │   ├── java/com/focodevsistemas/gerenciamento/
│   │   │   ├── Activities/          # Telas do app
│   │   │   ├── DAOs/                # Acesso a dados
│   │   │   ├── Models/              # Modelos de dados
│   │   │   └── Utils/               # Utilitários
│   │   ├── res/                     # Recursos (layouts, imagens, etc.)
│   │   └── AndroidManifest.xml
│   ├── test/                        # Testes unitários
│   └── androidTest/                 # Testes instrumentados
└── build.gradle
```

## 🔒 Privacidade e Segurança

- Todos os dados são armazenados **localmente** no dispositivo
- Nenhum dado é enviado para servidores externos
- Dados protegidos por senha do aplicativo
- Backup opcional (armazenado localmente)

## 📝 Licença

Este projeto é propriedade de Focodev Sistemas.

## 👨‍💻 Desenvolvimento

**Package Name:** `com.focodevsistemas.gerenciamento`  
**Versão:** 1.0  
**Build:** 1

## 🐛 Reportar Problemas

Se encontrar algum problema ou tiver sugestões:
1. Abra uma issue no repositório
2. Descreva o problema detalhadamente
3. Inclua logs se possível

## 📞 Suporte

Para suporte, entre em contato através do app ou pelo repositório.

## 🔄 Atualizações

O app verifica automaticamente por atualizações quando configurado com um repositório GitHub.

---

**Desenvolvido com ❤️ por Focodev Sistemas**

