package com.focodevsistemas.gerenciamento;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Activity de Relatórios de Serviços Premium.
 * Funcionalidades:
 * - Faturamento por período
 * - Histórico por cliente
 * - Total mensal
 */
public class RelatoriosServicosActivity extends AppCompatActivity {

    private FeatureGate featureGate;
    private AgendamentoDAO agendamentoDAO;
    private ClienteDAO clienteDAO;
    private ServicoDAO servicoDAO;
    
    private Spinner spinnerPeriodo;
    private Spinner spinnerCliente;
    private Spinner spinnerServico;
    private Spinner spinnerStatus;
    private com.google.android.material.textfield.TextInputEditText editTextValorMinimo;
    private com.google.android.material.textfield.TextInputEditText editTextValorMaximo;
    private Button buttonGerarRelatorio;
    private TextView textFaturamentoTotal;
    private TextView textTotalMensal;
    private ListView listViewHistorico;
    
    private List<Cliente> listaClientes;
    private List<Servico> listaServicos;
    private ArrayAdapter<Cliente> clienteAdapter;
    private ArrayAdapter<Servico> servicoAdapter;
    private ArrayAdapter<String> historicoAdapter;
    
    private long dataInicialSelecionada = 0;
    private long dataFinalSelecionada = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Proteção Premium
        if (!PremiumManager.getInstance(this).verificarAcessoEmActivity(this, "Relatórios de Serviços")) {
            return;
        }
        
        setContentView(R.layout.activity_relatorios_servicos);
        
        setupActionBar();
        setupDAOs();
        bindViews();
        setupSpinners();
        setupListeners();
        // setupPremiumUI removido
        
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
        
        // Desabilitar botões principais se for FREE
        buttonGerarRelatorio.setEnabled(isPremium);
        spinnerPeriodo.setEnabled(isPremium);
        spinnerCliente.setEnabled(isPremium);
        spinnerServico.setEnabled(isPremium);
        spinnerStatus.setEnabled(isPremium);
        if (editTextValorMinimo != null) {
            editTextValorMinimo.setEnabled(isPremium);
        }
        if (editTextValorMaximo != null) {
            editTextValorMaximo.setEnabled(isPremium);
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
            getSupportActionBar().setTitle("Relatórios de Serviços");
        }
    }

    private void setupDAOs() {
        agendamentoDAO = new AgendamentoDAO(this);
        agendamentoDAO.open();
        clienteDAO = new ClienteDAO(this);
        clienteDAO.open();
        servicoDAO = new ServicoDAO(this);
        servicoDAO.open();
    }

    private void bindViews() {
        spinnerPeriodo = findViewById(R.id.spinnerPeriodo);
        spinnerCliente = findViewById(R.id.spinnerCliente);
        spinnerServico = findViewById(R.id.spinnerServico);
        spinnerStatus = findViewById(R.id.spinnerStatus);
        editTextValorMinimo = findViewById(R.id.editTextValorMinimo);
        editTextValorMaximo = findViewById(R.id.editTextValorMaximo);
        buttonGerarRelatorio = findViewById(R.id.buttonGerarRelatorio);
        textFaturamentoTotal = findViewById(R.id.textFaturamentoTotal);
        textTotalMensal = findViewById(R.id.textTotalMensal);
        listViewHistorico = findViewById(R.id.listViewHistorico);
    }

    private void setupSpinners() {
        // Spinner de período
        String[] periodos = {"Hoje", "Esta Semana", "Este Mês", "Este Ano", "Ano Passado", "Todo o Período", "Personalizado"};
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
        
        // Spinner de serviço
        listaServicos = new ArrayList<>();
        listaServicos.add(new Servico()); // Opção "Todos os serviços"
        listaServicos.get(0).setNome("Todos os serviços");
        listaServicos.addAll(servicoDAO.getAllServicos());
        
        servicoAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, listaServicos) {
            @Override
            public View getView(int position, View convertView, android.view.ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text = (TextView) view.findViewById(android.R.id.text1);
                text.setText(listaServicos.get(position).getNome());
                return view;
            }
            
            @Override
            public View getDropDownView(int position, View convertView, android.view.ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView text = (TextView) view.findViewById(android.R.id.text1);
                text.setText(listaServicos.get(position).getNome());
                return view;
            }
        };
        servicoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerServico.setAdapter(servicoAdapter);
        
        // Spinner de status
        String[] status = {"Todos", "Finalizados", "Cancelados", "Pendentes"};
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, status);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(statusAdapter);
        
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
            shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Relatório de Serviços - Gerenciamento Total Mais");
            
            // Texto adicional (usado por alguns apps)
            String textoMensagem = "Segue em anexo o relatório de serviços gerado pelo aplicativo Gerenciamento Total Mais.\n\n" +
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
            android.util.Log.e("RelatoriosServicosActivity", "Nenhum app para compartilhar encontrado", e);
            android.widget.Toast.makeText(this, 
                "Nenhum aplicativo disponível para compartilhar. Instale um app de e-mail ou WhatsApp.", 
                android.widget.Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            android.util.Log.e("RelatoriosServicosActivity", "Erro ao exportar PDF", e);
            android.widget.Toast.makeText(this, 
                "Erro ao exportar: " + e.getMessage(), 
                android.widget.Toast.LENGTH_LONG).show();
        }
    }

    private void carregarDados() {
        // Carregar total mensal automaticamente
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
        Servico servicoSelecionado = listaServicos.get(spinnerServico.getSelectedItemPosition());
        String statusSelecionado = spinnerStatus.getSelectedItem().toString();
        
        // Obter valores mínimo e máximo
        double valorMinimo = 0.0;
        double valorMaximo = Double.MAX_VALUE;
        try {
            String valorMinStr = editTextValorMinimo.getText().toString().trim();
            if (!valorMinStr.isEmpty()) {
                valorMinimo = Double.parseDouble(valorMinStr.replace(",", "."));
            }
        } catch (Exception e) {
            // Valor inválido, usar 0.0
        }
        try {
            String valorMaxStr = editTextValorMaximo.getText().toString().trim();
            if (!valorMaxStr.isEmpty()) {
                valorMaximo = Double.parseDouble(valorMaxStr.replace(",", "."));
            }
        } catch (Exception e) {
            // Valor inválido, usar MAX_VALUE
        }
        
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
                    // Adicionar margem de segurança para agendamentos futuros
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
        
        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        
        // Histórico com todos os filtros aplicados
        List<String> historico = new ArrayList<>();
        List<Agendamento> agendamentos = agendamentoDAO.getAgendamentosPorPeriodo(inicio, fim);
        this.agendamentosAtuais = agendamentos; // Armazenar para uso na correção
        if (agendamentos.isEmpty()) {
            int totalNoBanco = agendamentoDAO.getTotalAgendamentos();
            if (totalNoBanco > 0) {
                long min = agendamentoDAO.getMinDataHoraInicio();
                long max = agendamentoDAO.getMaxDataHoraInicio();
                SimpleDateFormat sdfD = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                String faixa = (min > 0 && max > 0) ? (sdfD.format(new java.util.Date(min)) + " a " + sdfD.format(new java.util.Date(max))) : "N/A";
                android.widget.Toast.makeText(
                        this,
                        "Sem dados no período. Total no banco: " + totalNoBanco + " (faixa: " + faixa + ")",
                        android.widget.Toast.LENGTH_LONG
                ).show();
            }
        }
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        
        double faturamentoFiltrado = 0.0;
        
        for (Agendamento ag : agendamentos) {
            // Filtrar por cliente se selecionado
            if (clienteSelecionado.getId() > 0 && ag.getClienteId() != clienteSelecionado.getId()) {
                continue;
            }
            
            // Filtrar por serviço se selecionado
            if (servicoSelecionado.getId() > 0 && ag.getServicoId() != servicoSelecionado.getId()) {
                continue;
            }
            
            // Filtrar por status
            boolean deveIncluir = false;
            switch (statusSelecionado) {
                case "Finalizados":
                    deveIncluir = (ag.getCancelado() == 0 && ag.getFinalizado() == 1);
                    break;
                case "Cancelados":
                    deveIncluir = (ag.getCancelado() == 1);
                    break;
                case "Pendentes":
                    deveIncluir = (ag.getCancelado() == 0 && ag.getFinalizado() == 0);
                    break;
                case "Todos":
                default:
                    deveIncluir = true;
                    break;
            }
            
            if (!deveIncluir) {
                continue;
            }
            
            // Filtrar por valor mínimo e máximo
            if (ag.getValor() < valorMinimo || ag.getValor() > valorMaximo) {
                continue;
            }
            
            // Adicionar ao histórico e calcular faturamento
            String dataFormatada = sdf.format(new java.util.Date(ag.getDataHoraInicio()));
            String nomeCliente = ag.getNomeCliente() != null ? ag.getNomeCliente() : "Cliente";
            String nomeServico = ag.getNomeServico() != null ? ag.getNomeServico() : "Serviço";
            int tempoServico = ag.getTempoServico();
            String tempoFormatado = tempoServico > 0 ? tempoServico + " min" : "N/A";
            
            // Adicionar status ao formato
            String statusAgendamento = ag.isCancelado() ? "Cancelado" : 
                                      (ag.isFinalizado() ? "Finalizado" : "Pendente");
            
            // Formato melhorado: Data/Hora - Cliente | Serviço | Tempo | Valor | Status
            String linha = String.format("%s\n%s | %s | %s | %s | %s", 
                dataFormatada,
                nomeCliente,
                nomeServico,
                tempoFormatado,
                nf.format(ag.getValor()),
                statusAgendamento);
            
            // Indicador visual de valor zero
            if (ag.getValor() == 0.0 && ag.getCancelado() == 0) {
                linha += " ⚠️ [Sem Valor]";
            }
            
            historico.add(linha);
            
            // Somar ao faturamento apenas se não estiver cancelado
            if (ag.getCancelado() == 0) {
                faturamentoFiltrado += ag.getValor();
            }
        }
        
        // Atualizar faturamento com valor filtrado
        if (Double.isNaN(faturamentoFiltrado)) faturamentoFiltrado = 0.0;
        textFaturamentoTotal.setText("Faturamento: " + nf.format(faturamentoFiltrado));
        
        // Alerta se houver registros finalizados mas faturamento zerado
        if (faturamentoFiltrado == 0.0 && !agendamentos.isEmpty() && "Finalizados".equals(statusSelecionado)) {
             boolean temFinalizados = false;
             for (Agendamento ag : agendamentos) {
                 if (ag.getFinalizado() == 1 && ag.getCancelado() == 0) {
                     temFinalizados = true;
                     break;
                 }
             }
             if (temFinalizados) {
                 android.widget.Toast.makeText(this, "Aviso: Serviços finalizados encontrados, mas com valor zero. Verifique o cadastro dos serviços.", android.widget.Toast.LENGTH_LONG).show();
             }
        }
        
        if (historico.isEmpty()) {
            if ("Este Ano".equals(periodoSelecionado) || "Hoje".equals(periodoSelecionado) || "Este Mês".equals(periodoSelecionado)) {
                 historico.add("Nenhum registro encontrado neste período.");
                 historico.add("Dica: Tente selecionar 'Ano Passado' ou 'Todo o Período' para ver registros antigos.");
            } else {
                 historico.add("Nenhum registro encontrado para o período selecionado.");
            }
        }
        
        historicoAdapter.clear();
        historicoAdapter.addAll(historico);
        historicoAdapter.notifyDataSetChanged();
        
        // Gerar PDF automaticamente após gerar relatório
        gerarPDFRelatorio(agendamentos, inicio, fim, clienteSelecionado, servicoSelecionado, statusSelecionado, valorMinimo, valorMaximo, faturamentoFiltrado);
    }
    
    /**
     * Gera um PDF com o relatório completo usando iText 5 (compatível com Android).
     */
    private void gerarPDFRelatorio(List<Agendamento> agendamentos, long inicio, long fim, Cliente clienteSelecionado, Servico servicoSelecionado, String statusSelecionado, double valorMinimo, double valorMaximo, double faturamento) {
        new Thread(() -> {
            try {
                // Criar diretório para documentos
                java.io.File documentsDir = new java.io.File(getExternalFilesDir(null), "Relatorios");
                if (!documentsDir.exists()) {
                    documentsDir.mkdirs();
                }
                
                // Nome do arquivo com data/hora
                SimpleDateFormat sdfNome = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
                String nomeArquivo = "Relatorio_Servicos_" + sdfNome.format(new java.util.Date()) + ".pdf";
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
                document.add(new com.itextpdf.text.Paragraph("RELATÓRIO DE SERVIÇOS", fontTitulo));
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
                
                if (servicoSelecionado.getId() > 0) {
                    document.add(new com.itextpdf.text.Paragraph("Serviço: " + servicoSelecionado.getNome(), fontNormal));
                } else {
                    document.add(new com.itextpdf.text.Paragraph("Serviço: Todos", fontNormal));
                }
                
                document.add(new com.itextpdf.text.Paragraph("Status: " + statusSelecionado, fontNormal));
                
                if (valorMinimo > 0 || valorMaximo < Double.MAX_VALUE) {
                    NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
                    String filtroValor = "Filtro de Valor: ";
                    if (valorMinimo > 0) {
                        filtroValor += "Mínimo: " + nf.format(valorMinimo);
                    }
                    if (valorMaximo < Double.MAX_VALUE) {
                        if (valorMinimo > 0) filtroValor += " | ";
                        filtroValor += "Máximo: " + nf.format(valorMaximo);
                    }
                    document.add(new com.itextpdf.text.Paragraph(filtroValor, fontNormal));
                }
                
                NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
                document.add(new com.itextpdf.text.Paragraph("Faturamento Total: " + nf.format(faturamento), fontBold));
                document.add(new com.itextpdf.text.Paragraph(" ")); // Espaço
                
                // Tabela de dados
                com.itextpdf.text.pdf.PdfPTable table = new com.itextpdf.text.pdf.PdfPTable(6);
                table.setWidthPercentage(100);
                table.setWidths(new float[]{2f, 2f, 2f, 1f, 1.5f, 1.5f});
                
                // Cabeçalho da tabela
                com.itextpdf.text.Font fontHeader = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10, com.itextpdf.text.Font.BOLD);
                table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("Data/Hora", fontHeader)));
                table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("Cliente", fontHeader)));
                table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("Serviço", fontHeader)));
                table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("Tempo", fontHeader)));
                table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("Valor", fontHeader)));
                table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("Status", fontHeader)));
                
                // Dados - aplicar os mesmos filtros
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                int count = 0;
                for (Agendamento ag : agendamentos) {
                    // Filtrar por cliente se selecionado
                    if (clienteSelecionado.getId() > 0 && ag.getClienteId() != clienteSelecionado.getId()) {
                        continue;
                    }
                    
                    // Filtrar por serviço se selecionado
                    if (servicoSelecionado.getId() > 0 && ag.getServicoId() != servicoSelecionado.getId()) {
                        continue;
                    }
                    
                    // Filtrar por status
                    boolean deveIncluir = false;
                    switch (statusSelecionado) {
                        case "Finalizados":
                            deveIncluir = (ag.getCancelado() == 0 && ag.getFinalizado() == 1);
                            break;
                        case "Cancelados":
                            deveIncluir = (ag.getCancelado() == 1);
                            break;
                        case "Pendentes":
                            deveIncluir = (ag.getCancelado() == 0 && ag.getFinalizado() == 0);
                            break;
                        case "Todos":
                        default:
                            deveIncluir = true;
                            break;
                    }
                    
                    if (!deveIncluir) {
                        continue;
                    }
                    
                    // Filtrar por valor mínimo e máximo
                    if (ag.getValor() < valorMinimo || ag.getValor() > valorMaximo) {
                        continue;
                    }
                    
                    String dataFormatada = sdf.format(new java.util.Date(ag.getDataHoraInicio()));
                    String nomeCliente = ag.getNomeCliente() != null ? ag.getNomeCliente() : "Cliente";
                    String nomeServico = ag.getNomeServico() != null ? ag.getNomeServico() : "Serviço";
                    int tempoServico = ag.getTempoServico();
                    String tempoFormatado = tempoServico > 0 ? tempoServico + " min" : "N/A";
                    String statusAgendamento = ag.isCancelado() ? "Cancelado" : 
                                              (ag.isFinalizado() ? "Finalizado" : "Pendente");
                    
                    table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(dataFormatada, fontNormal)));
                    table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(nomeCliente, fontNormal)));
                    table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(nomeServico, fontNormal)));
                    table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(tempoFormatado, fontNormal)));
                    table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(nf.format(ag.getValor()), fontNormal)));
                    table.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(statusAgendamento, fontNormal)));
                    count++;
                }
                
                if (count == 0) {
                    com.itextpdf.text.pdf.PdfPCell emptyCell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("Nenhum registro encontrado para os filtros selecionados.", fontNormal));
                    emptyCell.setColspan(6);
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
                android.util.Log.e("RelatoriosServicosActivity", "Erro ao gerar PDF", e);
                runOnUiThread(() -> {
                    android.widget.Toast.makeText(this, 
                        "Erro ao gerar PDF: " + e.getMessage(), 
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
        if (servicoDAO != null) {
            servicoDAO.close();
        }
        super.onDestroy();
    }

    // Variável de membro para armazenar os agendamentos atuais (para uso na correção)
    private List<Agendamento> agendamentosAtuais;

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.menu_relatorios_servicos, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else if (item.getItemId() == R.id.action_corrigir_valores) {
            iniciarCorrecaoValores();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void iniciarCorrecaoValores() {
        if (agendamentosAtuais == null || agendamentosAtuais.isEmpty()) {
            android.widget.Toast.makeText(this, "Nenhum agendamento listado para corrigir.", android.widget.Toast.LENGTH_SHORT).show();
            return;
        }

        // Filtrar agendamentos com valor zero e não cancelados
        java.util.Map<String, List<Agendamento>> servicosZerados = new java.util.HashMap<>();
        int totalZerados = 0;

        for (Agendamento ag : agendamentosAtuais) {
            if (ag.getCancelado() == 0 && ag.getValor() == 0.0) {
                String nomeServico = ag.getNomeServico() != null ? ag.getNomeServico() : "Serviço sem nome";
                if (!servicosZerados.containsKey(nomeServico)) {
                    servicosZerados.put(nomeServico, new ArrayList<>());
                }
                servicosZerados.get(nomeServico).add(ag);
                totalZerados++;
            }
        }

        if (totalZerados == 0) {
            android.widget.Toast.makeText(this, "Não há agendamentos com valor zero na lista atual.", android.widget.Toast.LENGTH_SHORT).show();
            return;
        }

        // Mostrar dialog explicativo
        new android.app.AlertDialog.Builder(this)
            .setTitle("Corrigir Valores")
            .setMessage("Encontramos " + totalZerados + " agendamentos com valor R$ 0,00.\n\n" +
                        "Isso pode acontecer se o valor não foi informado na criação.\n\n" +
                        "Deseja definir um valor para esses serviços agora?")
            .setPositiveButton("Sim, Corrigir", (dialog, which) -> {
                corrigirProximoServico(new ArrayList<>(servicosZerados.keySet()), servicosZerados, 0);
            })
            .setNegativeButton("Cancelar", null)
            .show();
    }

    private void corrigirProximoServico(List<String> nomesServicos, java.util.Map<String, List<Agendamento>> mapa, int index) {
        if (index >= nomesServicos.size()) {
            // Terminou
            android.widget.Toast.makeText(this, "Correção concluída! Atualizando relatório...", android.widget.Toast.LENGTH_SHORT).show();
            gerarRelatorio(); // Recarrega a lista
            return;
        }

        String nomeServico = nomesServicos.get(index);
        List<Agendamento> listaParaCorrigir = mapa.get(nomeServico);
        int qtd = listaParaCorrigir.size();

        // Input para o valor
        final android.widget.EditText input = new android.widget.EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setHint("Ex: 50.00");

        new android.app.AlertDialog.Builder(this)
            .setTitle("Valor para '" + nomeServico + "'")
            .setMessage("Existem " + qtd + " registros deste serviço com valor zero.\nQual deve ser o valor unitário?")
            .setView(input)
            .setPositiveButton("Aplicar", (dialog, which) -> {
                String valorStr = input.getText().toString().trim().replace(",", ".");
                try {
                    double novoValor = Double.parseDouble(valorStr);
                    if (novoValor > 0) {
                        aplicarValorEmMassa(listaParaCorrigir, novoValor);
                        corrigirProximoServico(nomesServicos, mapa, index + 1);
                    } else {
                        android.widget.Toast.makeText(this, "Valor inválido. Pulei este serviço.", android.widget.Toast.LENGTH_SHORT).show();
                        corrigirProximoServico(nomesServicos, mapa, index + 1);
                    }
                } catch (NumberFormatException e) {
                    android.widget.Toast.makeText(this, "Valor inválido. Pulei este serviço.", android.widget.Toast.LENGTH_SHORT).show();
                    corrigirProximoServico(nomesServicos, mapa, index + 1);
                }
            })
            .setNegativeButton("Pular", (dialog, which) -> {
                corrigirProximoServico(nomesServicos, mapa, index + 1);
            })
            .setCancelable(false)
            .show();
    }

    private void aplicarValorEmMassa(List<Agendamento> lista, double valor) {
        int sucesso = 0;
        for (Agendamento ag : lista) {
            ag.setValor(valor);
            // Usar o método ignorando conflito para ser mais rápido e garantir atualização
            if (agendamentoDAO.atualizarAgendamentoIgnorandoConflito(ag) > 0) {
                sucesso++;
            }
        }
        // Log ou Toast opcional
        android.util.Log.d("CorrecaoMassa", "Atualizados " + sucesso + " registros com valor " + valor);
    }
}
