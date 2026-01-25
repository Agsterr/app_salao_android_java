package com.focodevsistemas.gerenciamento;

import android.content.Context;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Activity de Relatórios Premium.
 * Funcionalidades:
 * - Faturamento por período
 * - Histórico por cliente
 * - Total mensal
 */
public class RelatoriosActivity extends AppCompatActivity {

    private FeatureGate featureGate;
    private AgendamentoDAO agendamentoDAO;
    private ClienteDAO clienteDAO;
    
    private Spinner spinnerPeriodo;
    private Spinner spinnerCliente;
    private Button buttonGerarRelatorio;
    private TextView textFaturamentoTotal;
    private TextView textTotalMensal;
    private ListView listViewHistorico;
    
    private List<Cliente> listaClientes;
    private ArrayAdapter<Cliente> clienteAdapter;
    private ArrayAdapter<String> historicoAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_relatorios);
        
        setupActionBar();
        setupDAOs();
        bindViews();
        setupSpinners();
        setupListeners();
        setupPremiumUI();
        
        carregarDados();
    }
    
    /**
     * Configura a UI baseada no plano do usuário (FREE ou PREMIUM).
     */
    private void setupPremiumUI() {
        featureGate = new FeatureGate(this);
        PlanManager planManager = PlanManager.getInstance(this);
        boolean isPremium = planManager.isPremium();
        
        // Card de aviso Premium (visível apenas para FREE)
        com.google.android.material.card.MaterialCardView cardAviso = findViewById(R.id.cardAvisoPremium);
        if (cardAviso != null) {
            cardAviso.setVisibility(isPremium ? View.GONE : View.VISIBLE);
        }
        
        // Botão de exportação (visível apenas para PREMIUM)
        com.google.android.material.button.MaterialButton buttonExportar = findViewById(R.id.buttonExportar);
        if (buttonExportar != null) {
            buttonExportar.setVisibility(isPremium ? View.VISIBLE : View.GONE);
            if (isPremium) {
                buttonExportar.setOnClickListener(v -> exportarRelatorio());
            }
        }
        
        // Mostrar botões de serviços
        android.view.View layoutBotoesServicos = findViewById(R.id.layoutBotoesServicos);
        if (layoutBotoesServicos != null) {
            layoutBotoesServicos.setVisibility(View.VISIBLE);
        }
        
        // Desabilitar botões principais se for FREE
        buttonGerarRelatorio.setEnabled(isPremium);
        spinnerPeriodo.setEnabled(isPremium);
        spinnerCliente.setEnabled(isPremium);
        
        // Desabilitar botões de produtos se for FREE
        com.google.android.material.button.MaterialButton buttonRelatorioProdutos = findViewById(R.id.buttonRelatorioProdutos);
        if (buttonRelatorioProdutos != null) {
            buttonRelatorioProdutos.setEnabled(isPremium);
            if (!isPremium) {
                buttonRelatorioProdutos.setAlpha(0.5f);
            }
        }
        
        com.google.android.material.button.MaterialButton buttonRelatorioVendas = findViewById(R.id.buttonRelatorioVendas);
        if (buttonRelatorioVendas != null) {
            buttonRelatorioVendas.setEnabled(isPremium);
            if (!isPremium) {
                buttonRelatorioVendas.setAlpha(0.5f);
            }
        }
        
        
        // Se for FREE, mostrar aviso mas permitir visualizar
        if (!isPremium) {
            android.widget.Toast.makeText(this, 
                "Você está visualizando em modo FREE. Faça upgrade para usar todas as funcionalidades.", 
                android.widget.Toast.LENGTH_LONG).show();
        }
    }

    private void setupActionBar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Relatórios");
        }
    }

    private void setupDAOs() {
        agendamentoDAO = new AgendamentoDAO(this);
        agendamentoDAO.open();
        clienteDAO = new ClienteDAO(this);
        clienteDAO.open();
    }

    private void bindViews() {
        spinnerPeriodo = findViewById(R.id.spinnerPeriodo);
        spinnerCliente = findViewById(R.id.spinnerCliente);
        buttonGerarRelatorio = findViewById(R.id.buttonGerarRelatorio);
        textFaturamentoTotal = findViewById(R.id.textFaturamentoTotal);
        textTotalMensal = findViewById(R.id.textTotalMensal);
        listViewHistorico = findViewById(R.id.listViewHistorico);
    }
    
    private long dataInicialSelecionada = 0;
    private long dataFinalSelecionada = 0;

    private void setupSpinners() {
        // Spinner de período
        String[] periodos = {"Hoje", "Esta Semana", "Este Mês", "Este Ano", "Personalizado"};
        ArrayAdapter<String> periodoAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, periodos);
        periodoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPeriodo.setAdapter(periodoAdapter);
        
        // Spinner de cliente
        listaClientes = new ArrayList<>();
        listaClientes.add(new Cliente()); // Opção "Todos os clientes"
        listaClientes.get(0).setNome("Todos os clientes");
        listaClientes.addAll(clienteDAO.getAllClientes());
        
        clienteAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, listaClientes) {
            @Override
            public View getView(int position, View convertView, android.view.ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text = (TextView) view.findViewById(android.R.id.text1);
                text.setText(listaClientes.get(position).getNome());
                return view;
            }
            
            @Override
            public View getDropDownView(int position, View convertView, android.view.ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView text = (TextView) view.findViewById(android.R.id.text1);
                text.setText(listaClientes.get(position).getNome());
                return view;
            }
        };
        clienteAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCliente.setAdapter(clienteAdapter);
        
        // ListView de histórico
        historicoAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_list_item_1, new ArrayList<>());
        listViewHistorico.setAdapter(historicoAdapter);
    }

    private void setupListeners() {
        buttonGerarRelatorio.setOnClickListener(v -> {
            PlanManager planManager = PlanManager.getInstance(this);
            if (!planManager.isPremium()) {
                PremiumBlockDialog.show(this, "Gerar Relatório");
                return;
            }
            gerarRelatorio();
        });
        
        // Listener para seleção de data inicial
        com.google.android.material.button.MaterialButton buttonDataInicial = findViewById(R.id.buttonDataInicial);
        if (buttonDataInicial != null) {
            buttonDataInicial.setOnClickListener(v -> selecionarDataInicial());
        }
        
        // Listener para seleção de data final
        com.google.android.material.button.MaterialButton buttonDataFinal = findViewById(R.id.buttonDataFinal);
        if (buttonDataFinal != null) {
            buttonDataFinal.setOnClickListener(v -> selecionarDataFinal());
        }
        
        // Botões de Relatórios de Produtos
        com.google.android.material.button.MaterialButton buttonRelatorioProdutos = findViewById(R.id.buttonRelatorioProdutos);
        if (buttonRelatorioProdutos != null) {
            buttonRelatorioProdutos.setOnClickListener(v -> gerarRelatorioProdutos());
        }
        
        com.google.android.material.button.MaterialButton buttonRelatorioVendas = findViewById(R.id.buttonRelatorioVendas);
        if (buttonRelatorioVendas != null) {
            buttonRelatorioVendas.setOnClickListener(v -> gerarRelatorioVendas());
        }
    }
    
    private void selecionarDataInicial() {
        PlanManager planManager = PlanManager.getInstance(this);
        if (!planManager.isPremium()) {
            PremiumBlockDialog.show(this, "Selecionar Data");
            return;
        }
        
        Calendar cal = Calendar.getInstance();
        if (dataInicialSelecionada > 0) {
            cal.setTimeInMillis(dataInicialSelecionada);
        }
        
        new android.app.DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            Calendar selected = Calendar.getInstance();
            selected.set(year, month, dayOfMonth, 0, 0, 0);
            selected.set(Calendar.MILLISECOND, 0);
            dataInicialSelecionada = selected.getTimeInMillis();
            
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            com.google.android.material.button.MaterialButton btn = findViewById(R.id.buttonDataInicial);
            if (btn != null) {
                btn.setText("Data Inicial: " + sdf.format(new java.util.Date(dataInicialSelecionada)));
            }
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }
    
    private void selecionarDataFinal() {
        PlanManager planManager = PlanManager.getInstance(this);
        if (!planManager.isPremium()) {
            PremiumBlockDialog.show(this, "Selecionar Data");
            return;
        }
        
        Calendar cal = Calendar.getInstance();
        if (dataFinalSelecionada > 0) {
            cal.setTimeInMillis(dataFinalSelecionada);
        }
        
        new android.app.DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            Calendar selected = Calendar.getInstance();
            selected.set(year, month, dayOfMonth, 23, 59, 59);
            selected.set(Calendar.MILLISECOND, 999);
            dataFinalSelecionada = selected.getTimeInMillis();
            
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            com.google.android.material.button.MaterialButton btn = findViewById(R.id.buttonDataFinal);
            if (btn != null) {
                btn.setText("Data Final: " + sdf.format(new java.util.Date(dataFinalSelecionada)));
            }
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }
    
    private void exportarRelatorio() {
        PlanManager planManager = PlanManager.getInstance(this);
        if (!planManager.isPremium()) {
            PremiumBlockDialog.show(this, "Exportar Relatório");
            return;
        }
        
        // Buscar relatórios disponíveis
        java.io.File documentsDir = new java.io.File(getExternalFilesDir(null), "Relatorios");
        if (!documentsDir.exists() || documentsDir.listFiles() == null || documentsDir.listFiles().length == 0) {
            android.widget.Toast.makeText(this, 
                "Gere um relatório primeiro antes de exportar.", 
                android.widget.Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Filtrar apenas arquivos PDF
        java.util.List<java.io.File> pdfFiles = new java.util.ArrayList<>();
        java.io.File[] files = documentsDir.listFiles();
        if (files != null) {
            for (java.io.File file : files) {
                if (file.isFile() && file.getName().toLowerCase().endsWith(".pdf")) {
                    pdfFiles.add(file);
                }
            }
        }
        
        if (pdfFiles.isEmpty()) {
            android.widget.Toast.makeText(this, 
                "Nenhum relatório encontrado para exportar.", 
                android.widget.Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Se houver apenas um arquivo, compartilhar diretamente
        if (pdfFiles.size() == 1) {
            compartilharPDF(pdfFiles.get(0));
            return;
        }
        
        // Se houver múltiplos arquivos, mostrar dialog para escolher
        mostrarDialogEscolherRelatorio(pdfFiles);
    }
    
    /**
     * Mostra um dialog para o usuário escolher qual relatório exportar.
     */
    private void mostrarDialogEscolherRelatorio(java.util.List<java.io.File> pdfFiles) {
        // Ordenar por data (mais recente primeiro)
        pdfFiles.sort((f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));
        
        // Criar lista de nomes de arquivos
        java.util.List<String> nomesArquivos = new java.util.ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        for (java.io.File file : pdfFiles) {
            String nome = file.getName().replace("Relatorio_", "").replace(".pdf", "");
            try {
                // Tentar extrair data do nome do arquivo
                SimpleDateFormat sdfNome = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
                java.util.Date data = sdfNome.parse(nome);
                nomesArquivos.add(sdf.format(data) + " - " + file.getName());
            } catch (Exception e) {
                nomesArquivos.add(file.getName());
            }
        }
        
        // Criar adapter para o dialog
        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(
            this,
            android.R.layout.simple_list_item_1,
            nomesArquivos
        );
        
        // Criar dialog
        new android.app.AlertDialog.Builder(this)
            .setTitle("Escolher Relatório para Exportar")
            .setAdapter(adapter, (dialog, which) -> {
                java.io.File arquivoSelecionado = pdfFiles.get(which);
                compartilharPDF(arquivoSelecionado);
            })
            .setNegativeButton("Cancelar", null)
            .show();
    }
    
    /**
     * Compartilha um arquivo PDF com qualquer aplicativo instalado (E-mail, WhatsApp, Drive, etc.)
     */
    private void compartilharPDF(java.io.File pdfFile) {
        try {
            // Criar Intent para compartilhar
            android.content.Intent shareIntent = new android.content.Intent(android.content.Intent.ACTION_SEND);
            
            // Tipo MIME para PDF (compatível com todos os apps)
            shareIntent.setType("application/pdf");
            
            // Usar FileProvider para compartilhar de forma segura (Android 7.0+)
            android.net.Uri uri = androidx.core.content.FileProvider.getUriForFile(
                this,
                getPackageName() + ".fileprovider",
                pdfFile
            );
            
            // Adicionar o arquivo ao Intent
            shareIntent.putExtra(android.content.Intent.EXTRA_STREAM, uri);
            
            // Assunto (usado principalmente por apps de e-mail)
            shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Relatório de Faturamento - Gerenciamento Total Mais");
            
            // Texto adicional (usado por alguns apps)
            String textoMensagem = "Segue em anexo o relatório de faturamento gerado pelo aplicativo Gerenciamento Total Mais.\n\n" +
                "Arquivo: " + pdfFile.getName();
            shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, textoMensagem);
            
            // Dar permissão de leitura temporária para o app que receberá o arquivo
            shareIntent.addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION);
            
            // Para Android 11+ (API 30+), garantir permissão persistente
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                shareIntent.addFlags(android.content.Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            }
            
            // Criar chooser para o usuário escolher o app
            String tituloChooser = "Exportar Relatório";
            android.content.Intent chooser = android.content.Intent.createChooser(shareIntent, tituloChooser);
            
            // Verificar se há apps disponíveis para compartilhar
            if (shareIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(chooser);
                
                // Mostrar mensagem informativa
                android.widget.Toast.makeText(this, 
                    "Escolha o aplicativo para compartilhar o relatório", 
                    android.widget.Toast.LENGTH_SHORT).show();
            } else {
                android.widget.Toast.makeText(this, 
                    "Nenhum aplicativo disponível para compartilhar PDF", 
                    android.widget.Toast.LENGTH_LONG).show();
            }
            
        } catch (android.content.ActivityNotFoundException e) {
            android.util.Log.e("RelatoriosActivity", "Nenhum app para compartilhar encontrado", e);
            android.widget.Toast.makeText(this, 
                "Nenhum aplicativo disponível para compartilhar. Instale um app de e-mail ou WhatsApp.", 
                android.widget.Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            android.util.Log.e("RelatoriosActivity", "Erro ao exportar PDF", e);
            android.widget.Toast.makeText(this, 
                "Erro ao exportar: " + e.getMessage(), 
                android.widget.Toast.LENGTH_LONG).show();
        }
    }

    private void carregarDados() {
        // Carregar total mensal automaticamente (para relatórios de serviços)
        atualizarTotalMensal();
    }

    private void atualizarTotalMensal() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long inicioMes = cal.getTimeInMillis();
        
        cal.add(Calendar.MONTH, 1);
        long fimMes = cal.getTimeInMillis() - 1;
        
        double totalMensal = agendamentoDAO.getTotalValorPeriodo(inicioMes, fimMes);
        if (Double.isNaN(totalMensal)) totalMensal = 0.0;
        
        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        textTotalMensal.setText("Total Mensal: " + nf.format(totalMensal));
    }

    private void gerarRelatorio() {
        PlanManager planManager = PlanManager.getInstance(this);
        if (!planManager.isPremium()) {
            PremiumBlockDialog.show(this, "Gerar Relatório");
            return;
        }
        
        String periodoSelecionado = spinnerPeriodo.getSelectedItem().toString();
        Cliente clienteSelecionado = listaClientes.get(spinnerCliente.getSelectedItemPosition());
        
        Calendar cal = Calendar.getInstance();
        long inicio, fim;
        
        // Calcular período
        if ("Personalizado".equals(periodoSelecionado) && dataInicialSelecionada > 0 && dataFinalSelecionada > 0) {
            // Usar datas personalizadas
            inicio = dataInicialSelecionada;
            fim = dataFinalSelecionada;
        } else {
            // Usar período pré-definido
            switch (periodoSelecionado) {
            case "Hoje":
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                inicio = cal.getTimeInMillis();
                cal.add(Calendar.DAY_OF_MONTH, 1);
                fim = cal.getTimeInMillis() - 1;
                break;
            case "Esta Semana":
                int diaSemana = cal.get(Calendar.DAY_OF_WEEK);
                int diasParaSegunda = (diaSemana == Calendar.SUNDAY) ? -6 : Calendar.MONDAY - diaSemana;
                cal.add(Calendar.DAY_OF_MONTH, diasParaSegunda);
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                inicio = cal.getTimeInMillis();
                cal.add(Calendar.DAY_OF_MONTH, 7);
                fim = cal.getTimeInMillis() - 1;
                break;
            case "Este Mês":
                cal.set(Calendar.DAY_OF_MONTH, 1);
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                inicio = cal.getTimeInMillis();
                cal.add(Calendar.MONTH, 1);
                fim = cal.getTimeInMillis() - 1;
                break;
            case "Este Ano":
                cal.set(Calendar.MONTH, Calendar.JANUARY);
                cal.set(Calendar.DAY_OF_MONTH, 1);
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                inicio = cal.getTimeInMillis();
                cal.add(Calendar.YEAR, 1);
                fim = cal.getTimeInMillis() - 1;
                break;
            case "Ano Passado":
                cal.set(Calendar.MONTH, Calendar.JANUARY);
                cal.set(Calendar.DAY_OF_MONTH, 1);
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                cal.add(Calendar.YEAR, -1); // Voltar para o início do ano passado
                inicio = cal.getTimeInMillis();
                cal.add(Calendar.YEAR, 1); // Avançar um ano para chegar ao início deste ano
                fim = cal.getTimeInMillis() - 1; // Um milissegundo antes do início deste ano
                break;
            case "Todo o Período":
                long minData = agendamentoDAO.getMinDataHoraInicio();
                long maxData = agendamentoDAO.getMaxDataHoraInicio();
                
                if (minData > 0 && maxData > 0) {
                    // Ajustar início para o começo do dia da primeira data
                    cal.setTimeInMillis(minData);
                    cal.set(Calendar.HOUR_OF_DAY, 0);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);
                    cal.set(Calendar.MILLISECOND, 0);
                    inicio = cal.getTimeInMillis();
                    
                    // Ajustar fim para o final do dia da última data (ou hoje, o que for maior)
                    long agora = System.currentTimeMillis();
                    if (agora > maxData) maxData = agora;
                    
                    cal.setTimeInMillis(maxData);
                    cal.set(Calendar.HOUR_OF_DAY, 23);
                    cal.set(Calendar.MINUTE, 59);
                    cal.set(Calendar.SECOND, 59);
                    cal.set(Calendar.MILLISECOND, 999);
                    // Adicionar margem de segurança
                    cal.add(Calendar.DAY_OF_MONTH, 1); 
                    fim = cal.getTimeInMillis();
                } else {
                    // Sem dados, usar mês atual como fallback visual
                    cal.set(Calendar.DAY_OF_MONTH, 1);
                    cal.set(Calendar.HOUR_OF_DAY, 0);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);
                    cal.set(Calendar.MILLISECOND, 0);
                    inicio = cal.getTimeInMillis();
                    cal.add(Calendar.MONTH, 1);
                    fim = cal.getTimeInMillis() - 1;
                }
                break;
            default:
                // Personalizado sem datas selecionadas ou período desconhecido - usar mês atual como padrão
                cal.set(Calendar.DAY_OF_MONTH, 1);
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                inicio = cal.getTimeInMillis();
                cal.add(Calendar.MONTH, 1);
                fim = cal.getTimeInMillis() - 1;
                break;
            }
        }
        
        // Faturamento por período
        double faturamento = agendamentoDAO.getTotalValorPeriodo(inicio, fim);
        if (Double.isNaN(faturamento)) faturamento = 0.0;
        
        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        textFaturamentoTotal.setText("Faturamento: " + nf.format(faturamento));
        
        // Histórico por cliente
        List<String> historico = new ArrayList<>();
        List<Agendamento> agendamentos = agendamentoDAO.getAgendamentosPorPeriodo(inicio, fim);
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        
        for (Agendamento ag : agendamentos) {
            // Filtrar por cliente se selecionado
            if (clienteSelecionado.getId() > 0 && ag.getClienteId() != clienteSelecionado.getId()) {
                continue;
            }
            
            // Log para debug
            android.util.Log.d("RelatoriosActivity", "Processando agendamento: " + ag.getId() + 
                " | Cancelado: " + ag.getCancelado() + 
                " | Finalizado: " + ag.getFinalizado());

            // Filtro relaxado: Mostrar todos não cancelados (Finalizados ou Pendentes)
            if (ag.getCancelado() == 0) {
                String dataFormatada = sdf.format(new java.util.Date(ag.getDataHoraInicio()));
                String nomeCliente = ag.getNomeCliente() != null ? ag.getNomeCliente() : "Cliente";
                String nomeServico = ag.getNomeServico() != null ? ag.getNomeServico() : "Serviço";
                int tempoServico = ag.getTempoServico();
                String tempoFormatado = tempoServico > 0 ? tempoServico + " min" : "N/A";
                
                String status = (ag.getFinalizado() == 1) ? "[Finalizado]" : "[Pendente]";
                
                // Formato melhorado: Data/Hora - Cliente | Serviço | Tempo | Valor
                String linha = String.format("%s %s\n%s | %s | %s | %s", 
                    dataFormatada,
                    status,
                    nomeCliente,
                    nomeServico,
                    tempoFormatado,
                    nf.format(ag.getValor()));
                historico.add(linha);
            }
        }
        
        if (historico.isEmpty()) {
            android.util.Log.w("RelatoriosActivity", "Nenhum histórico encontrado após filtros.");
            historico.add("Nenhum registro encontrado para o período selecionado.");
        }
        
        historicoAdapter.clear();
        historicoAdapter.addAll(historico);
        historicoAdapter.notifyDataSetChanged();
        
        // Gerar PDF automaticamente após gerar relatório
        gerarPDFRelatorio(agendamentos, inicio, fim, clienteSelecionado, faturamento);
    }
    
    /**
     * Gera um PDF com o relatório completo usando iText 5 (compatível com Android).
     */
    private void gerarPDFRelatorio(List<Agendamento> agendamentos, long inicio, long fim, Cliente clienteSelecionado, double faturamento) {
        new Thread(() -> {
            try {
                // Criar diretório para documentos
                java.io.File documentsDir = new java.io.File(getExternalFilesDir(null), "Relatorios");
                if (!documentsDir.exists()) {
                    documentsDir.mkdirs();
                }
                
                // Nome do arquivo com data/hora
                SimpleDateFormat sdfNome = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
                String nomeArquivo = "Relatorio_" + sdfNome.format(new java.util.Date()) + ".pdf";
                java.io.File pdfFile = new java.io.File(documentsDir, nomeArquivo);
                
                // Criar PDF usando iText 5
                com.itextpdf.text.Document document = new com.itextpdf.text.Document();
                com.itextpdf.text.pdf.PdfWriter.getInstance(document, new java.io.FileOutputStream(pdfFile));
                document.open();
                
                // Configurar fonte
                com.itextpdf.text.Font fontNormal = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10);
                com.itextpdf.text.Font fontBold = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 14, com.itextpdf.text.Font.BOLD);
                com.itextpdf.text.Font fontTitulo = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 18, com.itextpdf.text.Font.BOLD);
                
                // Título
                document.add(new com.itextpdf.text.Paragraph("RELATÓRIO DE FATURAMENTO", fontTitulo));
                document.add(new com.itextpdf.text.Paragraph(" ")); // Espaço
                
                // Informações do período
                SimpleDateFormat sdfData = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                
                String periodoInfo = String.format("Período: %s a %s",
                    sdfData.format(new java.util.Date(inicio)),
                    sdfData.format(new java.util.Date(fim)));
                document.add(new com.itextpdf.text.Paragraph(periodoInfo, fontNormal));
                
                if (clienteSelecionado.getId() > 0) {
                    document.add(new com.itextpdf.text.Paragraph("Cliente: " + clienteSelecionado.getNome(), fontNormal));
                } else {
                    document.add(new com.itextpdf.text.Paragraph("Cliente: Todos", fontNormal));
                }
                
                NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
                document.add(new com.itextpdf.text.Paragraph("Faturamento Total: " + nf.format(faturamento), fontBold));
                document.add(new com.itextpdf.text.Paragraph(" ")); // Espaço
                
                // Tabela de dados
                com.itextpdf.text.pdf.PdfPTable table = new com.itextpdf.text.pdf.PdfPTable(5);
                table.setWidthPercentage(100);
                table.setWidths(new float[]{2.5f, 2.5f, 2.5f, 1.5f, 1.5f});
                
                // Cabeçalho da tabela
                com.itextpdf.text.Font fontHeader = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10, com.itextpdf.text.Font.BOLD);
                table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("Data/Hora", fontHeader)));
                table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("Cliente", fontHeader)));
                table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("Serviço", fontHeader)));
                table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("Status", fontHeader)));
                table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("Valor", fontHeader)));
                
                // Dados
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                int count = 0;
                
                android.util.Log.d("RelatoriosActivity", "Gerando PDF com " + agendamentos.size() + " agendamentos totais.");
                
                for (Agendamento ag : agendamentos) {
                    if (clienteSelecionado.getId() > 0 && ag.getClienteId() != clienteSelecionado.getId()) {
                        continue;
                    }
                    
                    // Filtro relaxado: Incluir Pendentes e Finalizados (apenas excluir Cancelados)
                    if (ag.getCancelado() == 0) {
                        String dataFormatada = sdf.format(new java.util.Date(ag.getDataHoraInicio()));
                        String nomeCliente = ag.getNomeCliente() != null ? ag.getNomeCliente() : "Cliente";
                        String nomeServico = ag.getNomeServico() != null ? ag.getNomeServico() : "Serviço";
                        String status = (ag.getFinalizado() == 1) ? "Finalizado" : "Pendente";
                        
                        table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(dataFormatada, fontNormal)));
                        table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(nomeCliente, fontNormal)));
                        table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(nomeServico, fontNormal)));
                        table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(status, fontNormal)));
                        table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(nf.format(ag.getValor()), fontNormal)));
                        count++;
                    }
                }
                
                android.util.Log.d("RelatoriosActivity", "PDF gerado com " + count + " registros.");
                
                if (count == 0) {
                    com.itextpdf.text.pdf.PdfPCell emptyCell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("Nenhum registro encontrado para o período selecionado.", fontNormal));
                    emptyCell.setColspan(5);
                    table.addCell(emptyCell);
                }
                
                document.add(table);
                
                // Rodapé
                document.add(new com.itextpdf.text.Paragraph(" ")); // Espaço
                SimpleDateFormat sdfRodape = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                document.add(new com.itextpdf.text.Paragraph("Gerado em: " + sdfRodape.format(new java.util.Date()), 
                    new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 8)));
                
                document.close();
                
                // Mostrar mensagem de sucesso
                runOnUiThread(() -> {
                    android.widget.Toast.makeText(this, 
                        "PDF gerado com sucesso!\n" + pdfFile.getName(), 
                        android.widget.Toast.LENGTH_LONG).show();
                });
                
            } catch (Exception e) {
                android.util.Log.e("RelatoriosActivity", "Erro ao gerar PDF", e);
                runOnUiThread(() -> {
                    android.widget.Toast.makeText(this, 
                        "Erro ao gerar PDF: " + e.getMessage(), 
                        android.widget.Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    /**
     * Gera relatório de produtos em PDF.
     */
    private void gerarRelatorioProdutos() {
        PlanManager planManager = PlanManager.getInstance(this);
        if (!planManager.isPremium()) {
            PremiumBlockDialog.show(this, "Relatório de Produtos");
            return;
        }
        
        new Thread(() -> {
            try {
                ProdutoDAO produtoDAO = new ProdutoDAO(this);
                produtoDAO.open();
                List<Produto> produtos = produtoDAO.getAllProdutos();
                produtoDAO.close();
                
                android.util.Log.d("RelatoriosActivity", "Produtos encontrados: " + produtos.size());
                
                if (produtos.isEmpty()) {
                    runOnUiThread(() -> {
                        android.widget.Toast.makeText(this, "Nenhum produto encontrado no banco de dados.", 
                            android.widget.Toast.LENGTH_SHORT).show();
                    });
                    return;
                }
                
                File pdfFile = PDFGeneratorHelper.gerarPDFProdutos(this, produtos);
                
                android.util.Log.d("RelatoriosActivity", "PDF de produtos gerado: " + pdfFile.getAbsolutePath());
                
                runOnUiThread(() -> {
                    android.widget.Toast.makeText(this, "PDF gerado com sucesso!\n" + pdfFile.getName(), 
                        android.widget.Toast.LENGTH_LONG).show();
                    PDFGeneratorHelper.compartilharPDF(this, pdfFile);
                });
            } catch (Exception e) {
                android.util.Log.e("RelatoriosActivity", "Erro ao gerar PDF de produtos", e);
                runOnUiThread(() -> {
                    android.widget.Toast.makeText(this, "Erro ao gerar PDF: " + e.getMessage(), 
                        android.widget.Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }
    
    /**
     * Gera relatório de vendas em PDF.
     */
    private void gerarRelatorioVendas() {
        PlanManager planManager = PlanManager.getInstance(this);
        if (!planManager.isPremium()) {
            PremiumBlockDialog.show(this, "Relatório de Vendas");
            return;
        }
        
        new Thread(() -> {
            try {
                VendaDAO vendaDAO = new VendaDAO(this);
                vendaDAO.open();
                ProdutoDAO produtoDAO = new ProdutoDAO(this);
                produtoDAO.open();
                ClienteDAO clienteDAO = new ClienteDAO(this);
                clienteDAO.open();
                VendaItemDAO vendaItemDAO = new VendaItemDAO(this);
                vendaItemDAO.open();
                
                List<Venda> vendas = vendaDAO.getAllVendas();
                
                android.util.Log.d("RelatoriosActivity", "Vendas encontradas: " + vendas.size());
                
                if (vendas.isEmpty()) {
                    runOnUiThread(() -> {
                        android.widget.Toast.makeText(this, "Nenhuma venda encontrada no banco de dados.", 
                            android.widget.Toast.LENGTH_SHORT).show();
                    });
                    vendaDAO.close();
                    produtoDAO.close();
                    clienteDAO.close();
                    vendaItemDAO.close();
                    return;
                }
                
                File pdfFile = PDFGeneratorHelper.gerarPDFVendas(this, vendas, vendaDAO, produtoDAO, clienteDAO, vendaItemDAO);
                
                vendaDAO.close();
                produtoDAO.close();
                clienteDAO.close();
                vendaItemDAO.close();
                
                runOnUiThread(() -> {
                    android.widget.Toast.makeText(this, "PDF gerado com sucesso!\n" + pdfFile.getName(), 
                        android.widget.Toast.LENGTH_LONG).show();
                    PDFGeneratorHelper.compartilharPDF(this, pdfFile);
                });
            } catch (Exception e) {
                android.util.Log.e("RelatoriosActivity", "Erro ao gerar PDF de vendas", e);
                runOnUiThread(() -> {
                    android.widget.Toast.makeText(this, "Erro ao gerar PDF: " + e.getMessage(), 
                        android.widget.Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }
    

    @Override
    protected void onDestroy() {
        if (agendamentoDAO != null) {
            agendamentoDAO.close();
        }
        if (clienteDAO != null) {
            clienteDAO.close();
        }
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

