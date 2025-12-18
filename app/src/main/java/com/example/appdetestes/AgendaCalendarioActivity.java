package com.example.appdetestes;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AgendaCalendarioActivity extends AppCompatActivity {

    private CalendarView calendarView;
    private Button buttonVoltar;
    private TextView textViewInfo;
    private ExtendedFloatingActionButton fabNovoAgendamento;
    private AgendamentoDAO agendamentoDAO;
    private Set<Long> diasComAgendamentos = new HashSet<>();
    private long dataSelecionada = -1; // -1 significa nenhuma data selecionada

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agenda_calendario);

        setupActionBar();
        setupDao();
        bindViews();
        setupListeners();
        carregarDiasComAgendamentos();
        atualizarCalendario();
    }

    private void setupActionBar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Agendar");
        }
    }

    private void setupDao() {
        agendamentoDAO = new AgendamentoDAO(this);
        agendamentoDAO.open();
    }

    private void bindViews() {
        calendarView = findViewById(R.id.calendarView);
        buttonVoltar = findViewById(R.id.buttonVoltar);
        textViewInfo = findViewById(R.id.textViewInfo);
        fabNovoAgendamento = findViewById(R.id.fabNovoAgendamento);
    }

    private void setupListeners() {
        if (buttonVoltar != null) {
            buttonVoltar.setOnClickListener(v -> finish());
        }

        if (fabNovoAgendamento != null) {
            fabNovoAgendamento.setOnClickListener(v -> {
                Intent intent = new Intent(this, AddAgendamentoActivity.class);
                if (dataSelecionada != -1) {
                    intent.putExtra("dataSelecionada", dataSelecionada);
                }
                startActivity(intent);
            });
        }

        // Inicializa com a data atual
        dataSelecionada = getTimestampInicioDoDia(System.currentTimeMillis());
        atualizarInfoDataSelecionada();

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, dayOfMonth);
            dataSelecionada = getTimestampInicioDoDia(calendar.getTimeInMillis());
            atualizarInfoDataSelecionada();
        });
    }

    private void carregarDiasComAgendamentos() {
        // Busca agendamentos dos próximos 3 meses
        Calendar cal = Calendar.getInstance();
        long inicio = getTimestampInicioDoDia(cal.getTimeInMillis());
        cal.add(Calendar.MONTH, 3);
        long fim = getTimestampInicioDoDia(cal.getTimeInMillis()) + (24L * 60 * 60 * 1000) - 1;
        
        List<Agendamento> todosAgendamentos = agendamentoDAO.getAgendamentosPorPeriodo(inicio, fim);
        
        diasComAgendamentos.clear();
        if (todosAgendamentos != null) {
            for (Agendamento a : todosAgendamentos) {
                if (a != null) {
                    long dataInicio = a.getDataHoraInicio();
                    long diaTimestamp = getTimestampInicioDoDia(dataInicio);
                    diasComAgendamentos.add(diaTimestamp);
                }
            }
        }
    }

    private void atualizarCalendario() {
        // Infelizmente, o CalendarView padrão do Android não permite marcar datas diretamente
        // Mas podemos usar um TextView informativo para mostrar quantos dias têm agendamentos
        int totalDias = diasComAgendamentos.size();
        if (totalDias > 0) {
            textViewInfo.setText(String.format("📅 %d dia(s) com agendamentos neste período", totalDias));
        } else {
            textViewInfo.setText("📅 Nenhum agendamento encontrado");
        }
    }

    private void atualizarInfoDataSelecionada() {
        if (dataSelecionada == -1) {
            textViewInfo.setText("📅 Selecione uma data para agendar");
            return;
        }

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(dataSelecionada);
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
        String dataFormatada = sdf.format(cal.getTime());
        
        List<Agendamento> agendamentos = agendamentoDAO.getAgendamentosPorDia(dataSelecionada);
        int total = agendamentos != null ? agendamentos.size() : 0;
        
        if (total > 0) {
            double valorTotal = 0.0;
            for (Agendamento a : agendamentos) {
                if (a != null) {
                    valorTotal += a.getValor();
                }
            }
            textViewInfo.setText(String.format("📅 Data selecionada: %s\n%d agendamento(s) - Total: R$ %.2f", dataFormatada, total, valorTotal));
        } else {
            textViewInfo.setText(String.format("📅 Data selecionada: %s\nNenhum agendamento para este dia", dataFormatada));
        }
    }

    private long getTimestampInicioDoDia(long timestamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregarDiasComAgendamentos();
        if (dataSelecionada == -1) {
            dataSelecionada = getTimestampInicioDoDia(System.currentTimeMillis());
        }
        atualizarInfoDataSelecionada();
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

