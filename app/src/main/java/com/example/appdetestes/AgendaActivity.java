package com.example.appdetestes;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.widget.CalendarView;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Arrays;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import java.text.NumberFormat;
import java.util.Locale;

public class AgendaActivity extends AppCompatActivity {

    private CalendarView calendarView;
    private ExpandableListView expandableListViewAgendamentos;
    private FloatingActionButton fabAdicionarAgendamento;
    private TextView textViewDica;
    private TextView textTotalSemana;
    private TextView textTotalMes;
    private TextView textTotalAno;
    private TextView textTotalDia; // novo
    private Spinner spinnerStatusFilter; // novo
    private Button buttonSairAgenda;

    private AgendamentoDAO agendamentoDAO;
    private long dataSelecionada;

    private AgendaExpandableListAdapter listAdapter;
    private List<String> listDataHeader;
    private HashMap<String, List<Agendamento>> listDataChild;

    private String statusFiltro = "Todos"; // novo

    /**
     * Chamado quando a activity está sendo criada.
     * @param savedInstanceState Se a activity estiver sendo recriada a partir de um estado salvo anteriormente, este é o estado.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agenda);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        agendamentoDAO = new AgendamentoDAO(this);
        agendamentoDAO.open();

        calendarView = findViewById(R.id.calendarView);
        expandableListViewAgendamentos = findViewById(R.id.expandableListViewAgendamentos);
        fabAdicionarAgendamento = findViewById(R.id.fabAdicionarAgendamento);
        textViewDica = findViewById(R.id.textViewDica);
        textTotalSemana = findViewById(R.id.textTotalSemana);
        textTotalMes = findViewById(R.id.textTotalMes);
        textTotalAno = findViewById(R.id.textTotalAno);
        textTotalDia = findViewById(R.id.textTotalDia);
        spinnerStatusFilter = findViewById(R.id.spinnerStatusFilter);
        buttonSairAgenda = findViewById(R.id.buttonSairAgenda);

        // Configura o Spinner de status
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                Arrays.asList("Todos", "Na fila", "Em andamento", "Finalizado"));
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatusFilter.setAdapter(statusAdapter);
        spinnerStatusFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                statusFiltro = (String) parent.getItemAtPosition(position);
                atualizarListaAgendamentos();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        dataSelecionada = getTimestampInicioDoDia(System.currentTimeMillis());

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, dayOfMonth);
            dataSelecionada = getTimestampInicioDoDia(calendar.getTimeInMillis());
            atualizarListaAgendamentos();
            atualizarTotais();
        });

        fabAdicionarAgendamento.setOnClickListener(v -> {
            Intent intent = new Intent(AgendaActivity.this, AddAgendamentoActivity.class);
            intent.putExtra("dataSelecionada", dataSelecionada);
            startActivity(intent);
        });

        registerForContextMenu(expandableListViewAgendamentos);

        buttonSairAgenda.setOnClickListener(v -> {
            finish();
        });
        // Permite fechar o teclado ao tocar fora (segurança, caso o teclado esteja aberto)
        View root = findViewById(android.R.id.content);
        root.setOnTouchListener((v, ev) -> {
            if (ev.getAction() == MotionEvent.ACTION_DOWN) {
                hideKeyboard();
                v.requestFocus();
            }
            return false;
        });
    }

    private void atualizarListaAgendamentos() {
        List<Agendamento> agendamentosDoDia = agendamentoDAO.getAgendamentosPorDia(dataSelecionada);

        // Aplica filtro de status, ignorando cancelados quando um status específico é escolhido
        List<Agendamento> agendamentosFiltrados = new ArrayList<>();
        for (Agendamento a : agendamentosDoDia) {
            String status = a.getStatus();
            if ("Todos".equals(statusFiltro)) {
                agendamentosFiltrados.add(a);
            } else {
                if (!"Cancelado".equals(status) && status.equals(statusFiltro)) {
                    agendamentosFiltrados.add(a);
                }
            }
        }

        listDataChild = new LinkedHashMap<>();
        for (Agendamento agendamento : agendamentosFiltrados) {
            String nomeCliente = agendamento.getNomeCliente();
            if (!listDataChild.containsKey(nomeCliente)) {
                listDataChild.put(nomeCliente, new ArrayList<>());
            }
            listDataChild.get(nomeCliente).add(agendamento);
        }

        listDataHeader = new ArrayList<>(listDataChild.keySet());

        listAdapter = new AgendaExpandableListAdapter(this, listDataHeader, listDataChild);
        expandableListViewAgendamentos.setAdapter(listAdapter);

        // Controla a visibilidade da dica e da lista baseado no estado dos agendamentos
        if (agendamentosFiltrados.isEmpty()) {
            expandableListViewAgendamentos.setVisibility(View.GONE);
            textViewDica.setVisibility(View.VISIBLE);
            textViewDica.setText("Nenhum agendamento para este dia.\nToque no '+' para adicionar.");
        } else {
            expandableListViewAgendamentos.setVisibility(View.VISIBLE);
            textViewDica.setVisibility(View.GONE);
        }
    }

    private void atualizarTotais() {
        // Dia
        long inicioDia = getTimestampInicioDoDia(dataSelecionada);
        long fimDia = inicioDia + (24L * 60 * 60 * 1000) - 1;
        double totalDia = agendamentoDAO.getTotalValorPeriodo(inicioDia, fimDia);

        // Semana (segunda a domingo)
        Calendar calSemana = Calendar.getInstance();
        calSemana.setTimeInMillis(dataSelecionada);
        calSemana.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        long inicioSemana = getTimestampInicioDoDia(calSemana.getTimeInMillis());
        long fimSemana = inicioSemana + (7L * 24 * 60 * 60 * 1000) - 1;
        double totalSemana = agendamentoDAO.getTotalValorPeriodo(inicioSemana, fimSemana);

        // Mês
        Calendar calMes = Calendar.getInstance();
        calMes.setTimeInMillis(dataSelecionada);
        calMes.set(Calendar.DAY_OF_MONTH, 1);
        long inicioMes = getTimestampInicioDoDia(calMes.getTimeInMillis());
        calMes.add(Calendar.MONTH, 1);
        long inicioProximoMes = getTimestampInicioDoDia(calMes.getTimeInMillis());
        long fimMes = inicioProximoMes - 1;
        double totalMes = agendamentoDAO.getTotalValorPeriodo(inicioMes, fimMes);

        // Ano
        Calendar calAno = Calendar.getInstance();
        calAno.setTimeInMillis(dataSelecionada);
        calAno.set(Calendar.MONTH, Calendar.JANUARY);
        calAno.set(Calendar.DAY_OF_MONTH, 1);
        long inicioAno = getTimestampInicioDoDia(calAno.getTimeInMillis());
        calAno.add(Calendar.YEAR, 1);
        long inicioProximoAno = getTimestampInicioDoDia(calAno.getTimeInMillis());
        long fimAno = inicioProximoAno - 1;
        double totalAno = agendamentoDAO.getTotalValorPeriodo(inicioAno, fimAno);

        setTotalText(textTotalDia, "Total do Dia:", totalDia);
        setTotalText(textTotalSemana, "Total da Semana:", totalSemana);
        setTotalText(textTotalMes, "Total do Mês:", totalMes);
        setTotalText(textTotalAno, "Total do Ano:", totalAno);
    }

    /**
     * Retorna o timestamp para o início do dia do timestamp fornecido.
     * @param timestamp O timestamp para o qual obter o início do dia.
     * @return O timestamp do início do dia.
     */
    private long getTimestampInicioDoDia(long timestamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    /**
     * Chamado quando a activity é destruída.
     */
    @Override
    protected void onDestroy() {
        agendamentoDAO.close();
        super.onDestroy();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo) menuInfo;
        if (ExpandableListView.getPackedPositionType(info.packedPosition) == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
            getMenuInflater().inflate(R.menu.menu_contexto_agendamento, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo) item.getMenuInfo();
        int groupPosition = ExpandableListView.getPackedPositionGroup(info.packedPosition);
        int childPosition = ExpandableListView.getPackedPositionChild(info.packedPosition);
    
        Agendamento agendamentoSelecionado = (Agendamento) listAdapter.getChild(groupPosition, childPosition);
        long agendamentoId = agendamentoSelecionado.getId();
    
        if (item.getItemId() == R.id.menu_apagar) {
            mostrarDialogoDeConfirmacao(agendamentoId);
            return true;
        } else if (item.getItemId() == R.id.menu_editar) {
            Intent intent = new Intent(this, AddAgendamentoActivity.class);
            intent.putExtra("agendamento_id", agendamentoId);
            startActivity(intent);
            return true;
        } else if (item.getItemId() == R.id.menu_reagendar) {
            Intent intent = new Intent(this, AddAgendamentoActivity.class);
            intent.putExtra("agendamento_id", agendamentoId);
            startActivity(intent);
            return true;
        } else if (item.getItemId() == R.id.menu_cancelar) {
            agendamentoSelecionado.setCancelado(1);
            agendamentoSelecionado.setFinalizado(0);
            int rows = agendamentoDAO.atualizarAgendamentoIgnorandoConflito(agendamentoSelecionado);
            if (rows > 0) {
                Toast.makeText(this, "Agendamento cancelado!", Toast.LENGTH_SHORT).show();
                atualizarListaAgendamentos();
                atualizarTotais();
            } else {
                Toast.makeText(this, "Falha ao cancelar agendamento", Toast.LENGTH_SHORT).show();
            }
            return true;
        } else if (item.getItemId() == R.id.menu_finalizar) {
            agendamentoSelecionado.setFinalizado(1);
            agendamentoSelecionado.setCancelado(0);
            int rows = agendamentoDAO.atualizarAgendamentoIgnorandoConflito(agendamentoSelecionado);
            if (rows > 0) {
                Toast.makeText(this, "Agendamento finalizado!", Toast.LENGTH_SHORT).show();
                atualizarListaAgendamentos();
                atualizarTotais();
            } else {
                Toast.makeText(this, "Falha ao finalizar agendamento", Toast.LENGTH_SHORT).show();
            }
            return true;
        }
        return super.onContextItemSelected(item);
    }

    private void mostrarDialogoDeConfirmacao(long agendamentoId) {
        new AlertDialog.Builder(this)
                .setTitle("Confirmar Exclusão")
                .setMessage("Tem certeza de que deseja apagar este agendamento?")
                .setPositiveButton("Sim", (dialog, which) -> {
                    agendamentoDAO.apagarAgendamento(agendamentoId);
                    Toast.makeText(AgendaActivity.this, "Agendamento apagado!", Toast.LENGTH_SHORT).show();
                    atualizarListaAgendamentos();
                    atualizarTotais();
                })
                .setNegativeButton("Não", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        atualizarListaAgendamentos();
        atualizarTotais();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setTotalText(TextView tv, String label, double value) {
        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("pt","BR"));
        String formatted = nf.format(value);
        String full = label + " " + formatted;
        SpannableString ss = new SpannableString(full);
        int start = full.lastIndexOf(formatted);
        if (start >= 0) {
            ss.setSpan(new ForegroundColorSpan(Color.RED), start, start + formatted.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        tv.setText(ss);
    }
    private void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
            view.clearFocus();
        }
    }
}
