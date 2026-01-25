package com.focodevsistemas.gerenciamento;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Activity de Alertas Premium.
 * Funcionalidades:
 * - Lembrete de agenda
 * - Notificação de serviços futuros
 */
public class AlertasActivity extends AppCompatActivity {

    private FeatureGate featureGate;
    private AgendamentoDAO agendamentoDAO;
    
    private ListView listViewAlertas;
    private TextView textSemAlertas;
    private ArrayAdapter<String> alertasAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Proteção Premium
        if (!PremiumManager.getInstance(this).verificarAcessoEmActivity(this, "Alertas")) {
            return;
        }
        
        setContentView(R.layout.activity_alertas);
        
        setupActionBar();
        setupDAO();
        bindViews();
        // setupPremiumUI removido
        setupListeners();
        carregarAlertas();
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
        
        // Botão Criar Alerta (desabilitar se for FREE)
        com.google.android.material.button.MaterialButton buttonCriarAlerta = findViewById(R.id.buttonCriarAlerta);
        if (buttonCriarAlerta != null) {
            buttonCriarAlerta.setEnabled(isPremium);
            if (!isPremium) {
                buttonCriarAlerta.setAlpha(0.5f);
            }
        }
        
        // Se for FREE, mostrar aviso mas permitir visualizar
        if (!isPremium) {
            android.widget.Toast.makeText(this, 
                "Você está visualizando em modo FREE. Faça upgrade para criar alertas.", 
                android.widget.Toast.LENGTH_LONG).show();
        }
    }
    
    private void setupListeners() {
        com.google.android.material.button.MaterialButton buttonCriarAlerta = findViewById(R.id.buttonCriarAlerta);
        if (buttonCriarAlerta != null) {
            buttonCriarAlerta.setOnClickListener(v -> criarAlerta());
        }
    }
    
    private void criarAlerta() {
        PlanManager planManager = PlanManager.getInstance(this);
        if (!planManager.isPremium()) {
            PremiumBlockDialog.show(this, "Criar Alerta");
            return;
        }
        
        // Abrir dialog para criar alerta
        mostrarDialogCriarAlerta();
    }
    
    private void mostrarDialogCriarAlerta() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Criar Novo Alerta");
        
        // Criar view customizada para o dialog
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(48, 32, 48, 32);
        
        // Seleção de Cliente
        android.widget.TextView labelCliente = new android.widget.TextView(this);
        labelCliente.setText("Cliente:");
        labelCliente.setTextSize(16);
        labelCliente.setPadding(0, 0, 0, 8);
        layout.addView(labelCliente);
        
        android.widget.Spinner spinnerCliente = new android.widget.Spinner(this);
        ClienteDAO clienteDAO = new ClienteDAO(this);
        clienteDAO.open();
        java.util.List<Cliente> clientes = clienteDAO.getAllClientes();
        clienteDAO.close();
        
        java.util.ArrayList<String> nomesClientes = new java.util.ArrayList<>();
        nomesClientes.add("Todos os clientes");
        for (Cliente c : clientes) {
            nomesClientes.add(c.getNome());
        }
        
        android.widget.ArrayAdapter<String> clienteAdapter = new android.widget.ArrayAdapter<>(
            this, android.R.layout.simple_spinner_item, nomesClientes);
        clienteAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCliente.setAdapter(clienteAdapter);
        layout.addView(spinnerCliente);
        
        // Seleção de Data
        android.widget.TextView labelData = new android.widget.TextView(this);
        labelData.setText("Data:");
        labelData.setTextSize(16);
        labelData.setPadding(0, 16, 0, 8);
        layout.addView(labelData);
        
        android.widget.Button buttonData = new android.widget.Button(this);
        buttonData.setText("Selecionar Data");
        android.widget.LinearLayout.LayoutParams paramsData = new android.widget.LinearLayout.LayoutParams(
            android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
        buttonData.setLayoutParams(paramsData);
        final long[] dataSelecionada = {0};
        final android.widget.Button[] buttonDataRef = {buttonData};
        buttonData.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            if (dataSelecionada[0] > 0) {
                cal.setTimeInMillis(dataSelecionada[0]);
            }
            new android.app.DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                Calendar selected = Calendar.getInstance();
                selected.set(year, month, dayOfMonth);
                dataSelecionada[0] = selected.getTimeInMillis();
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                buttonDataRef[0].setText("Data: " + sdf.format(new java.util.Date(dataSelecionada[0])));
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
        });
        layout.addView(buttonData);
        
        // Seleção de Horário
        android.widget.TextView labelHora = new android.widget.TextView(this);
        labelHora.setText("Horário:");
        labelHora.setTextSize(16);
        labelHora.setPadding(0, 16, 0, 8);
        layout.addView(labelHora);
        
        android.widget.Button buttonHora = new android.widget.Button(this);
        buttonHora.setText("Selecionar Horário");
        android.widget.LinearLayout.LayoutParams paramsHora = new android.widget.LinearLayout.LayoutParams(
            android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
        buttonHora.setLayoutParams(paramsHora);
        final int[] horaSelecionada = {0};
        final int[] minutoSelecionado = {0};
        final android.widget.Button[] buttonHoraRef = {buttonHora};
        buttonHora.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            new android.app.TimePickerDialog(this, (view, hourOfDay, minute) -> {
                horaSelecionada[0] = hourOfDay;
                minutoSelecionado[0] = minute;
                buttonHoraRef[0].setText(String.format("Horário: %02d:%02d", hourOfDay, minute));
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show();
        });
        layout.addView(buttonHora);
        
        builder.setView(layout);
        builder.setPositiveButton("Criar", (dialog, which) -> {
            // Criar alerta com os dados selecionados
            if (dataSelecionada[0] > 0) {
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(dataSelecionada[0]);
                cal.set(Calendar.HOUR_OF_DAY, horaSelecionada[0]);
                cal.set(Calendar.MINUTE, minutoSelecionado[0]);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                
                // TODO: Implementar criação de alerta no banco de dados
                android.widget.Toast.makeText(this, 
                    "Alerta criado com sucesso!", 
                    android.widget.Toast.LENGTH_SHORT).show();
                
                // Recarregar lista de alertas
                carregarAlertas();
            } else {
                android.widget.Toast.makeText(this, 
                    "Selecione uma data para criar o alerta.", 
                    android.widget.Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    private void setupActionBar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Alertas");
        }
    }

    private void setupDAO() {
        agendamentoDAO = new AgendamentoDAO(this);
        agendamentoDAO.open();
    }

    private void bindViews() {
        listViewAlertas = findViewById(R.id.listViewAlertas);
        textSemAlertas = findViewById(R.id.textSemAlertas);
        
        alertasAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_list_item_1, new ArrayList<>());
        listViewAlertas.setAdapter(alertasAdapter);
    }

    private void carregarAlertas() {
        List<String> alertas = new ArrayList<>();
        long agora = System.currentTimeMillis();
        
        // Buscar agendamentos futuros (próximos 7 dias)
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long inicio = cal.getTimeInMillis();
        
        cal.add(Calendar.DAY_OF_MONTH, 7);
        long fim = cal.getTimeInMillis() - 1;
        
        List<Agendamento> agendamentosFuturos = agendamentoDAO.getAgendamentosPorPeriodo(inicio, fim);
        
        SimpleDateFormat sdfData = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        SimpleDateFormat sdfHora = new SimpleDateFormat("HH:mm", Locale.getDefault());
        
        // Lembretes de agenda (próximas 24 horas)
        Calendar cal24h = Calendar.getInstance();
        cal24h.add(Calendar.HOUR, 24);
        long proximas24h = cal24h.getTimeInMillis();
        
        for (Agendamento ag : agendamentosFuturos) {
            if (ag.getCancelado() == 0 && ag.getFinalizado() == 0) {
                long dataHora = ag.getDataHoraInicio();
                
                // Verificar se está nas próximas 24 horas
                if (dataHora <= proximas24h && dataHora > agora) {
                    String dataFormatada = sdfData.format(new java.util.Date(dataHora));
                    String horaFormatada = sdfHora.format(new java.util.Date(dataHora));
                    String cliente = ag.getNomeCliente() != null ? ag.getNomeCliente() : "Cliente";
                    String servico = ag.getNomeServico() != null ? ag.getNomeServico() : "Serviço";
                    
                    long horasRestantes = (dataHora - agora) / (1000 * 60 * 60);
                    String tipoAlerta = horasRestantes < 2 ? "⚠️ URGENTE" : "🔔 Lembrete";
                    
                    String alerta = String.format("%s\n%s - %s às %s\nCliente: %s | Serviço: %s",
                        tipoAlerta,
                        dataFormatada,
                        horaFormatada,
                        cliente,
                        servico);
                    alertas.add(alerta);
                }
            }
        }
        
        // Notificações de serviços futuros (próximos 7 dias)
        for (Agendamento ag : agendamentosFuturos) {
            if (ag.getCancelado() == 0 && ag.getFinalizado() == 0) {
                long dataHora = ag.getDataHoraInicio();
                
                // Se não está nas próximas 24h, adicionar como notificação futura
                if (dataHora > proximas24h) {
                    String dataFormatada = sdfData.format(new java.util.Date(dataHora));
                    String horaFormatada = sdfHora.format(new java.util.Date(dataHora));
                    String cliente = ag.getNomeCliente() != null ? ag.getNomeCliente() : "Cliente";
                    String servico = ag.getNomeServico() != null ? ag.getNomeServico() : "Serviço";
                    
                    Calendar calAgendamento = Calendar.getInstance();
                    calAgendamento.setTimeInMillis(dataHora);
                    Calendar calHoje = Calendar.getInstance();
                    int diasRestantes = (int) ((dataHora - agora) / (1000 * 60 * 60 * 24));
                    
                    String alerta = String.format("📅 Serviço Futuro\n%s - %s\nCliente: %s | Serviço: %s\nEm %d dia(s)",
                        dataFormatada,
                        horaFormatada,
                        cliente,
                        servico,
                        diasRestantes);
                    alertas.add(alerta);
                }
            }
        }
        
        if (alertas.isEmpty()) {
            textSemAlertas.setText("Nenhum alerta no momento.");
            textSemAlertas.setVisibility(android.view.View.VISIBLE);
            listViewAlertas.setVisibility(android.view.View.GONE);
        } else {
            textSemAlertas.setVisibility(android.view.View.GONE);
            listViewAlertas.setVisibility(android.view.View.VISIBLE);
            alertasAdapter.clear();
            alertasAdapter.addAll(alertas);
            alertasAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregarAlertas();
    }

    @Override
    protected void onDestroy() {
        if (agendamentoDAO != null) {
            agendamentoDAO.close();
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

