package com.example.appdetestes;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CalendarView;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class AgendaActivity extends AppCompatActivity {

    private CalendarView calendarView;
    private ExpandableListView expandableListViewAgendamentos;
    private FloatingActionButton fabAdicionarAgendamento;
    private TextView textViewDica;
    private TextView textTotalSemana;
    private TextView textTotalMes;
    private TextView textTotalAno;

    private AgendamentoDAO agendamentoDAO;
    private long dataSelecionada;

    private AgendaExpandableListAdapter listAdapter;
    private List<String> listDataHeader;
    private HashMap<String, List<Agendamento>> listDataChild;

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
    }

    /**
     * Chamado quando o menu de contexto para a view é criado.
     * @param menu O menu de contexto que está sendo construído.
     * @param v A view para a qual o menu de contexto está sendo construído.
     * @param menuInfo Informações extras sobre o item para o qual o menu de contexto deve ser mostrado.
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo) menuInfo;

        if (ExpandableListView.getPackedPositionType(info.packedPosition) == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
            getMenuInflater().inflate(R.menu.menu_contexto_agendamento, menu);
        }
    }

    /**
     * Este hook é chamado sempre que um item em seu menu de contexto é selecionado.
     * @param item O item do menu de contexto que foi selecionado.
     * @return boolean Retorna false para permitir que o processamento normal do menu de contexto continue, true para consumi-lo aqui.
     */
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
        }

        return super.onContextItemSelected(item);
    }

    /**
     * Exibe um diálogo de confirmação para apagar um agendamento.
     * @param agendamentoId O ID do agendamento a ser apagado.
     */
    private void mostrarDialogoDeConfirmacao(long agendamentoId) {
        new AlertDialog.Builder(this)
                .setTitle("Confirmar Exclusão")
                .setMessage("Tem certeza de que deseja apagar este agendamento?")
                .setPositiveButton("Sim", (dialog, which) -> {
                    agendamentoDAO.apagarAgendamento(agendamentoId);
                    Toast.makeText(AgendaActivity.this, "Agendamento apagado!", Toast.LENGTH_SHORT).show();
                    atualizarListaAgendamentos();
                })
                .setNegativeButton("Não", null)
                .show();
    }

    /**
     * Chamado quando a activity irá começar a interagir com o usuário.
     */
    @Override
    protected void onResume() {
        super.onResume();
        atualizarListaAgendamentos();
        atualizarTotais();
    }

    /**
     * Este hook é chamado sempre que um item no seu menu de opções é selecionado.
     * @param item O item de menu que foi selecionado.
     * @return boolean Retorna false para permitir que o processamento normal do menu continue, true para consumi-lo aqui.
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // Alterado de onBackPressed() para finish()
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Atualiza a lista de agendamentos para o dia selecionado.
     */
    private void atualizarListaAgendamentos() {
        List<Agendamento> agendamentosDoDia = agendamentoDAO.getAgendamentosPorDia(dataSelecionada);

        listDataChild = new LinkedHashMap<>();
        for (Agendamento agendamento : agendamentosDoDia) {
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
        if (agendamentosDoDia.isEmpty()) {
            // Se a lista está VAZIA, mostra a dica "Nenhum agendamento..." e esconde a lista
            expandableListViewAgendamentos.setVisibility(View.GONE);
            textViewDica.setVisibility(View.VISIBLE);
            textViewDica.setText("Nenhum agendamento para este dia.\nToque no '+' para adicionar.");
        } else {
            // Se a lista NÃO está vazia, mostra a lista e esconde a dica
            expandableListViewAgendamentos.setVisibility(View.VISIBLE);
            textViewDica.setVisibility(View.GONE);
        }
    }

    private void atualizarTotais() {
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

        textTotalSemana.setText("Total da Semana: R$ " + String.format("%.2f", totalSemana));
        textTotalMes.setText("Total do Mês: R$ " + String.format("%.2f", totalMes));
        textTotalAno.setText("Total do Ano: R$ " + String.format("%.2f", totalAno));
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
}
