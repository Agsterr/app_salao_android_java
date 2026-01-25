Implementei as melhorias solicitadas para resolver a questão do faturamento zerado e permitir a visualização de anos anteriores.

### Melhorias Implementadas:

1.  **Novas Opções de Período**:
    *   Adicionei **"Ano Passado"** e **"Todo o Período"** no filtro de datas. Isso facilita muito a visualização de dados antigos sem precisar selecionar datas manualmente.
    *   A opção "Todo o Período" busca automaticamente desde o primeiro até o último agendamento registrado.

2.  **Diagnóstico de Faturamento Zero**:
    *   O faturamento provavelmente aparecia zerado porque o filtro padrão ("Este Ano") estava mostrando 2026 (data atual do sistema), enquanto seus dados estão em anos anteriores.
    *   Adicionei um alerta inteligente: se você filtrar por "Finalizados" e houver serviços na lista, mas o valor for zero, o app avisará para verificar se o cadastro do serviço não está com valor R$ 0,00.

3.  **Dicas de Uso**:
    *   Se a lista estiver vazia, o app agora sugere tentar "Ano Passado" ou "Todo o Período", ajudando a encontrar seus dados mais rápido.

### Como testar:
1.  Abra a tela de Relatórios de Serviços.
2.  No filtro de período, selecione **"Todo o Período"** ou **"Ano Passado"**.
3.  Verifique se o histórico e o faturamento aparecem corretamente.

### Reflexão Técnica (Arquiteto de Software):
O problema principal parecia ser de usabilidade (seleção de datas restrita) e não de lógica de cálculo. O código já somava corretamente os valores disponíveis. A adição de filtros pré-definidos como "Todo o Período" melhora significativamente a experiência do usuário, evitando a frustração de ver uma tela vazia ("faturamento zero") quando na verdade existem dados no banco, apenas fora do intervalo padrão. A consistência foi mantida aplicando as mesmas melhorias na tela de Relatórios Gerais.
