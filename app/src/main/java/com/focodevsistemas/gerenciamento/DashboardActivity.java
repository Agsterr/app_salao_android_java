package com.focodevsistemas.gerenciamento;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.text.NumberFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Activity de Dashboard Premium.
 * Funcionalidades:
 * - Visão geral de atendimentos
 * - Total de clientes
 * - Total de serviços realizados
 */
public class DashboardActivity extends AppCompatActivity {

    private FeatureGate featureGate;
    private AgendamentoDAO agendamentoDAO;
    private ClienteDAO clienteDAO;
    private ServicoDAO servicoDAO;
    
    private TextView textTotalClientes;
    private TextView textTotalServicos;
    private TextView textAtendimentosHoje;
    private TextView textAtendimentosSemana;
    private TextView textAtendimentosMes;
    private TextView textFaturamentoMes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Proteção de segurança: Verifica se usuário é Premium
        if (!PremiumManager.getInstance(this).verificarAcessoEmActivity(this, "Dashboard")) {
            return; // Interrompe inicialização se não for Premium
        }
        
        setContentView(R.layout.activity_dashboard);
        
        setupActionBar();
        setupDAOs();
        bindViews();
        // setupPremiumUI removido pois a verificação agora é bloqueante no início
        atualizarDashboard();
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
        
        // Se for FREE, mostrar aviso mas permitir visualizar
        if (!isPremium) {
            android.widget.Toast.makeText(this, 
                "Você está visualizando em modo FREE. Faça upgrade para atualizações automáticas.", 
                android.widget.Toast.LENGTH_LONG).show();
        }
    }

    private void setupActionBar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Dashboard");
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
        textTotalClientes = findViewById(R.id.textTotalClientes);
        textTotalServicos = findViewById(R.id.textTotalServicos);
        textAtendimentosHoje = findViewById(R.id.textAtendimentosHoje);
        textAtendimentosSemana = findViewById(R.id.textAtendimentosSemana);
        textAtendimentosMes = findViewById(R.id.textAtendimentosMes);
        textFaturamentoMes = findViewById(R.id.textFaturamentoMes);
    }

    private void atualizarDashboard() {
        // Total de clientes
        List<Cliente> clientes = clienteDAO.getAllClientes();
        textTotalClientes.setText("Total de Clientes: " + clientes.size());
        
        // Total de serviços realizados (agendamentos finalizados)
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MONTH, Calendar.JANUARY);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long inicioAno = cal.getTimeInMillis();
        
        cal.add(Calendar.YEAR, 1);
        long fimAno = cal.getTimeInMillis() - 1;
        
        List<Agendamento> agendamentosAno = agendamentoDAO.getAgendamentosPorPeriodo(inicioAno, fimAno);
        int totalServicosRealizados = 0;
        for (Agendamento ag : agendamentosAno) {
            if (ag.getFinalizado() == 1 && ag.getCancelado() == 0) {
                totalServicosRealizados++;
            }
        }
        textTotalServicos.setText("Total de Serviços Realizados: " + totalServicosRealizados);
        
        // Atendimentos hoje
        cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long inicioHoje = cal.getTimeInMillis();
        cal.add(Calendar.DAY_OF_MONTH, 1);
        long fimHoje = cal.getTimeInMillis() - 1;
        
        List<Agendamento> agendamentosHoje = agendamentoDAO.getAgendamentosPorPeriodo(inicioHoje, fimHoje);
        int atendimentosHoje = 0;
        for (Agendamento ag : agendamentosHoje) {
            if (ag.getCancelado() == 0) {
                atendimentosHoje++;
            }
        }
        textAtendimentosHoje.setText("Atendimentos Hoje: " + atendimentosHoje);
        
        // Atendimentos esta semana
        cal = Calendar.getInstance();
        int diaSemana = cal.get(Calendar.DAY_OF_WEEK);
        int diasParaSegunda = (diaSemana == Calendar.SUNDAY) ? -6 : Calendar.MONDAY - diaSemana;
        cal.add(Calendar.DAY_OF_MONTH, diasParaSegunda);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long inicioSemana = cal.getTimeInMillis();
        cal.add(Calendar.DAY_OF_MONTH, 7);
        long fimSemana = cal.getTimeInMillis() - 1;
        
        List<Agendamento> agendamentosSemana = agendamentoDAO.getAgendamentosPorPeriodo(inicioSemana, fimSemana);
        int atendimentosSemana = 0;
        for (Agendamento ag : agendamentosSemana) {
            if (ag.getCancelado() == 0) {
                atendimentosSemana++;
            }
        }
        textAtendimentosSemana.setText("Atendimentos Esta Semana: " + atendimentosSemana);
        
        // Atendimentos este mês
        cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long inicioMes = cal.getTimeInMillis();
        cal.add(Calendar.MONTH, 1);
        long fimMes = cal.getTimeInMillis() - 1;
        
        List<Agendamento> agendamentosMes = agendamentoDAO.getAgendamentosPorPeriodo(inicioMes, fimMes);
        int atendimentosMes = 0;
        for (Agendamento ag : agendamentosMes) {
            if (ag.getCancelado() == 0) {
                atendimentosMes++;
            }
        }
        textAtendimentosMes.setText("Atendimentos Este Mês: " + atendimentosMes);
        
        // Faturamento do mês
        double faturamentoMes = agendamentoDAO.getTotalValorPeriodo(inicioMes, fimMes);
        if (Double.isNaN(faturamentoMes)) faturamentoMes = 0.0;
        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        textFaturamentoMes.setText("Faturamento do Mês: " + nf.format(faturamentoMes));
    }

    @Override
    protected void onResume() {
        super.onResume();
        atualizarDashboard();
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

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

