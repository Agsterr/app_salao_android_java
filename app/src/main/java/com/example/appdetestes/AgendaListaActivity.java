package com.example.appdetestes;

import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Arrays;
import java.util.HashMap;

public class AgendaListaActivity extends AppCompatActivity {

    private ExpandableListView expandableListViewAgendamentos;
    private Button buttonVoltar;
    private Spinner spinnerStatusFilter;
    private TextView textViewDica;
    private TextView textViewTitulo;
    private AgendamentoDAO agendamentoDAO;
    private long dataSelecionada = -1; // -1 significa mostrar todos
    private boolean mostrarTodos = true;

    private AgendaExpandableListAdapter listAdapter;
    private List<String> listDataHeader;
    private HashMap<String, List<Agendamento>> listDataChild;

    private String statusFiltro = "Todos";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agenda_lista);

        setupActionBar();
        setupDao();
        bindViews();
        setupListeners();

        // Verifica se há uma data passada via Intent
        long dataExtra = getIntent().getLongExtra("dataSelecionada", -1);
        if (dataExtra != -1) {
            dataSelecionada = dataExtra;
            mostrarTodos = false;
        } else {
            mostrarTodos = true;
        }

        setupStatusSpinner();
        atualizarTitulo();
        atualizarListaAgendamentos();
        registerForContextMenu(expandableListViewAgendamentos);
    }

    private void atualizarTitulo() {
        if (textViewTitulo != null) {
            if (mostrarTodos) {
                textViewTitulo.setText("📋 Todos os Agendamentos");
            } else {
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(dataSelecionada);
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
                textViewTitulo.setText("📋 Agendamentos de " + sdf.format(cal.getTime()));
            }
        }
    }

    private void setupActionBar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Agendamentos");
        }
    }

    private void setupDao() {
        agendamentoDAO = new AgendamentoDAO(this);
        agendamentoDAO.open();
    }

    private void bindViews() {
        expandableListViewAgendamentos = findViewById(R.id.expandableListViewAgendamentos);
        buttonVoltar = findViewById(R.id.buttonVoltar);
        spinnerStatusFilter = findViewById(R.id.spinnerStatusFilter);
        textViewDica = findViewById(R.id.textViewDica);
        textViewTitulo = findViewById(R.id.textViewTitulo);
    }

    private void setupListeners() {
        if (buttonVoltar != null) {
            buttonVoltar.setOnClickListener(v -> finish());
        }
    }

    private void setupStatusSpinner() {
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                Arrays.asList("Todos", "Na fila", "Em andamento", "Finalizado")
        );
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatusFilter.setAdapter(statusAdapter);
        spinnerStatusFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                statusFiltro = (String) parent.getItemAtPosition(position);
                atualizarListaAgendamentos();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void atualizarListaAgendamentos() {
        List<Agendamento> agendamentos;
        
        if (mostrarTodos) {
            // Busca todos os agendamentos (próximos 6 meses)
            Calendar cal = Calendar.getInstance();
            long inicio = getTimestampInicioDoDia(cal.getTimeInMillis());
            cal.add(Calendar.MONTH, 6);
            long fim = getTimestampInicioDoDia(cal.getTimeInMillis()) + (24L * 60 * 60 * 1000) - 1;
            agendamentos = agendamentoDAO.getAgendamentosPorPeriodo(inicio, fim);
        } else {
            // Busca apenas do dia selecionado
            agendamentos = agendamentoDAO.getAgendamentosPorDia(dataSelecionada);
        }

        // Aplica filtro de status
        List<Agendamento> agendamentosFiltrados = new ArrayList<>();
        for (Agendamento a : agendamentos) {
            if (a == null) continue;
            
            String status = a.getStatus();
            if (status == null) continue;
            
            if ("Todos".equals(statusFiltro)) {
                agendamentosFiltrados.add(a);
            } else {
                if (!"Cancelado".equals(status) && statusFiltro.equals(status)) {
                    agendamentosFiltrados.add(a);
                }
            }
        }

        // Agrupa por dia primeiro (se mostrarTodos), depois por cliente
        if (mostrarTodos) {
            // Agrupa por dia primeiro, depois por cliente
            LinkedHashMap<String, LinkedHashMap<String, List<Agendamento>>> agrupadoPorDia = new LinkedHashMap<>();
            
            for (Agendamento agendamento : agendamentosFiltrados) {
                if (agendamento == null) continue;
                
                // Obtém a data do agendamento
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(agendamento.getDataHoraInicio());
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
                String dataFormatada = sdf.format(cal.getTime());
                String chaveDia = "📅 " + dataFormatada;
                
                // Obtém o nome do cliente
                String nomeCliente = agendamento.getNomeCliente();
                if (nomeCliente == null || nomeCliente.trim().isEmpty()) {
                    nomeCliente = "Cliente sem nome";
                }
                
                // Cria estrutura: Dia -> Cliente -> Lista de Agendamentos
                if (!agrupadoPorDia.containsKey(chaveDia)) {
                    agrupadoPorDia.put(chaveDia, new LinkedHashMap<>());
                }
                
                LinkedHashMap<String, List<Agendamento>> clientesDoDia = agrupadoPorDia.get(chaveDia);
                if (!clientesDoDia.containsKey(nomeCliente)) {
                    clientesDoDia.put(nomeCliente, new ArrayList<>());
                }
                clientesDoDia.get(nomeCliente).add(agendamento);
            }
            
            // Converte para estrutura plana: Dia-Cliente -> Lista de Agendamentos
            listDataChild = new LinkedHashMap<>();
            listDataHeader = new ArrayList<>();
            
            for (String dia : agrupadoPorDia.keySet()) {
                LinkedHashMap<String, List<Agendamento>> clientesDoDia = agrupadoPorDia.get(dia);
                
                for (String cliente : clientesDoDia.keySet()) {
                    String chaveCompleta = dia + " • " + cliente;
                    listDataHeader.add(chaveCompleta);
                    listDataChild.put(chaveCompleta, clientesDoDia.get(cliente));
                }
            }
        } else {
            // Para um dia específico, agrupa apenas por cliente (sem mostrar a data no grupo)
            listDataChild = new LinkedHashMap<>();
            for (Agendamento agendamento : agendamentosFiltrados) {
                if (agendamento == null) continue;
                
                String nomeCliente = agendamento.getNomeCliente();
                if (nomeCliente == null || nomeCliente.trim().isEmpty()) {
                    nomeCliente = "Cliente sem nome";
                }
                
                if (!listDataChild.containsKey(nomeCliente)) {
                    listDataChild.put(nomeCliente, new ArrayList<>());
                }
                listDataChild.get(nomeCliente).add(agendamento);
            }
            
            listDataHeader = new ArrayList<>(listDataChild.keySet());
        }

        listAdapter = new AgendaExpandableListAdapter(this, listDataHeader, listDataChild);
        expandableListViewAgendamentos.setAdapter(listAdapter);

        // Controla a visibilidade da dica e da lista
        if (agendamentosFiltrados.isEmpty()) {
            expandableListViewAgendamentos.setVisibility(View.GONE);
            textViewDica.setVisibility(View.VISIBLE);
            if (mostrarTodos) {
                textViewDica.setText("Nenhum agendamento encontrado.\nToque no '+' para adicionar.");
            } else {
                textViewDica.setText("Nenhum agendamento para este dia.\nToque no '+' para adicionar.");
            }
        } else {
            expandableListViewAgendamentos.setVisibility(View.VISIBLE);
            textViewDica.setVisibility(View.GONE);
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
        atualizarListaAgendamentos();
    }

    @Override
    protected void onDestroy() {
        if (agendamentoDAO != null) {
            agendamentoDAO.close();
        }
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
        if (listAdapter == null) return super.onContextItemSelected(item);

        ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo) item.getMenuInfo();
        if (info == null) return super.onContextItemSelected(item);
        
        int groupPosition = ExpandableListView.getPackedPositionGroup(info.packedPosition);
        int childPosition = ExpandableListView.getPackedPositionChild(info.packedPosition);
    
        Agendamento agendamentoSelecionado = (Agendamento) listAdapter.getChild(groupPosition, childPosition);
        if (agendamentoSelecionado == null) return super.onContextItemSelected(item);
        
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
                    Toast.makeText(AgendaListaActivity.this, "Agendamento apagado!", Toast.LENGTH_SHORT).show();
                    atualizarListaAgendamentos();
                })
                .setNegativeButton("Não", null)
                .show();
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

