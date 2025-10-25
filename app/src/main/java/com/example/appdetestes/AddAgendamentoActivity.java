package com.example.appdetestes;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AddAgendamentoActivity extends AppCompatActivity {

    private Spinner spinnerCliente;
    private Spinner spinnerServico;
    private TimePicker timePicker;
    private EditText editTextValor;
    private Button buttonSalvarAgendamento;

    private ClienteDAO clienteDAO;
    private ServicoDAO servicoDAO;
    private AgendamentoDAO agendamentoDAO;

    private List<Cliente> listaClientes;
    private List<Servico> listaServicos;

    private boolean isEditMode = false;
    private long agendamentoId = -1;
    private Agendamento agendamentoAtual;
    private long dataSelecionadaNoCalendario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_agendamento);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Inicialização dos DAOs
        clienteDAO = new ClienteDAO(this);
        clienteDAO.open();
        servicoDAO = new ServicoDAO(this);
        servicoDAO.open();
        agendamentoDAO = new AgendamentoDAO(this);
        agendamentoDAO.open();

        // Componentes da UI
        spinnerCliente = findViewById(R.id.spinnerCliente);
        spinnerServico = findViewById(R.id.spinnerServico);
        timePicker = findViewById(R.id.timePicker);
        timePicker.setIs24HourView(true);
        editTextValor = findViewById(R.id.editTextValor);
        buttonSalvarAgendamento = findViewById(R.id.buttonSalvarAgendamento);

        // Carrega dados
        carregarClientes();
        carregarServicos();

        // Determina o modo (criação ou edição) e configura a UI
        if (getIntent().hasExtra("agendamento_id")) {
            isEditMode = true;
            agendamentoId = getIntent().getLongExtra("agendamento_id", -1);
            agendamentoAtual = agendamentoDAO.getAgendamentoById(agendamentoId);
            dataSelecionadaNoCalendario = agendamentoAtual.getDataHoraInicio();
            setTitle("Editar Agendamento");
            buttonSalvarAgendamento.setText("Atualizar");
            if (agendamentoAtual != null) {
                preencherDadosParaEdicao();
            }
        } else {
            dataSelecionadaNoCalendario = getIntent().getLongExtra("dataSelecionada", System.currentTimeMillis());
            setTitle("Novo Agendamento");
        }

        buttonSalvarAgendamento.setOnClickListener(v -> salvarAgendamento());
    }

    private void preencherDadosParaEdicao() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(agendamentoAtual.getDataHoraInicio());
        timePicker.setHour(calendar.get(Calendar.HOUR_OF_DAY));
        timePicker.setMinute(calendar.get(Calendar.MINUTE));

        for (int i = 0; i < listaClientes.size(); i++) {
            if (listaClientes.get(i).getId() == agendamentoAtual.getClienteId()) {
                spinnerCliente.setSelection(i);
                break;
            }
        }

        for (int i = 0; i < listaServicos.size(); i++) {
            if (listaServicos.get(i).getId() == agendamentoAtual.getServicoId()) {
                spinnerServico.setSelection(i);
                break;
            }
        }

        // Preenche o valor, se disponível
        editTextValor.setText(String.valueOf(agendamentoAtual.getValor()));
    }




    private void salvarAgendamento() {
        if (spinnerCliente.getSelectedItem() == null || spinnerServico.getSelectedItem() == null) {
            Toast.makeText(this, "Selecione um cliente e um serviço", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. Coletar dados da UI
        long clienteId = listaClientes.get(spinnerCliente.getSelectedItemPosition()).getId();
        long servicoId = listaServicos.get(spinnerServico.getSelectedItemPosition()).getId();
        int tempoMin = listaServicos.get(spinnerServico.getSelectedItemPosition()).getTempo();
        long dataHoraInicio = calcularDataHoraInicio();

        // Valor do serviço
        String valorStr = editTextValor.getText().toString().trim();
        if (valorStr.isEmpty()) {
            Toast.makeText(this, "Informe o valor do serviço", Toast.LENGTH_SHORT).show();
            return;
        }
        double valor;
        try {
            valor = Double.parseDouble(valorStr.replace(",", "."));
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Valor inválido", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. Chamar o DAO, que fará a validação interna
        if (isEditMode) {
            // --- MODO DE ATUALIZAÇÃO ---
            agendamentoAtual.setClienteId(clienteId);
            agendamentoAtual.setServicoId(servicoId);
            agendamentoAtual.setDataHoraInicio(dataHoraInicio);
            agendamentoAtual.setTempoServico(tempoMin);
            agendamentoAtual.setValor(valor);

            // O DAO retorna 0 se houver conflito
            int linhasAfetadas = agendamentoDAO.atualizarAgendamento(agendamentoAtual);

            if (linhasAfetadas > 0) {
                Toast.makeText(this, "Agendamento atualizado!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                // Se falhou, é porque houve um conflito. Buscamos os detalhes para exibir o erro.
                long fim = dataHoraInicio + (tempoMin * 60000L);
                Agendamento conflito = agendamentoDAO.getAgendamentoConflitante(dataHoraInicio, fim, agendamentoAtual.getId());
                exibirDialogoDeConflitoComOpcao(conflito, () -> {
                    int atualizado = agendamentoDAO.atualizarAgendamentoIgnorandoConflito(agendamentoAtual);
                    if (atualizado > 0) {
                        Toast.makeText(this, "Agendamento atualizado mesmo com conflito!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Falha ao atualizar agendamento.", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            // ... dentro do método salvarAgendamento()
        } else {
            // --- MODO DE INSERÇÃO ---
            Agendamento novoAgendamento = new Agendamento();
            novoAgendamento.setClienteId(clienteId);
            novoAgendamento.setServicoId(servicoId);
            novoAgendamento.setDataHoraInicio(dataHoraInicio);
            novoAgendamento.setTempoServico(tempoMin);
            novoAgendamento.setValor(valor);

            // ---- ADICIONE ESTES LOGS PARA DEPURAR ----
            android.util.Log.d("DEBUG_AGENDAMENTO", "--- Preparando para Inserir ---");
            android.util.Log.d("DEBUG_AGENDAMENTO", "Cliente ID: " + novoAgendamento.getClienteId());
            android.util.Log.d("DEBUG_AGENDAMENTO", "Serviço ID: " + novoAgendamento.getServicoId());
            android.util.Log.d("DEBUG_AGENDAMENTO", "Tempo (min): " + novoAgendamento.getTempoServico());
            // Formata o timestamp para um formato legível
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
            android.util.Log.d("DEBUG_AGENDAMENTO", "Início (Timestamp): " + novoAgendamento.getDataHoraInicio());
            android.util.Log.d("DEBUG_AGENDAMENTO", "Início (Formatado): " + sdf.format(new Date(novoAgendamento.getDataHoraInicio())));
            android.util.Log.d("DEBUG_AGENDAMENTO", "---------------------------------");
            // ---- FIM DOS LOGS ----

            // O DAO retorna -1 se houver conflito
            long novoId = agendamentoDAO.inserirAgendamento(novoAgendamento);

            if (novoId != -1) {


                Toast.makeText(this, "Agendamento salvo!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                // Se falhou, é porque houve um conflito. Buscamos os detalhes para exibir o erro.
                long fim = dataHoraInicio + (tempoMin * 60000L);
                Agendamento conflito = agendamentoDAO.getAgendamentoConflitante(dataHoraInicio, fim, -1);
                exibirDialogoDeConflitoComOpcao(conflito, () -> {
                    long novoIdForcado = agendamentoDAO.inserirAgendamentoIgnorandoConflito(novoAgendamento);
                    if (novoIdForcado != -1) {
                        Toast.makeText(this, "Agendamento salvo mesmo com conflito!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Falha ao salvar agendamento.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    // Método auxiliar para exibir o erro (evita repetição de código)
    private void exibirDialogoDeConflito(Agendamento conflito) {
        String mensagem = "Já existe um agendamento nesse período.";

        // Adiciona detalhes se o objeto de conflito foi encontrado
        if (conflito != null) {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault());
            Calendar calConflitoFim = Calendar.getInstance();
            calConflitoFim.setTimeInMillis(conflito.getDataHoraInicio());
            calConflitoFim.add(Calendar.MINUTE, conflito.getTempoServico());
            String fimStr = sdf.format(calConflitoFim.getTime());

            mensagem += "\n\nConflito com: " + conflito.getNomeCliente() + " (" + conflito.getNomeServico() + ")." +
                    "\nDisponível somente após " + fimStr + ".";
        }

        new AlertDialog.Builder(this)
                .setTitle("Conflito de Horário")
                .setMessage(mensagem)
                .setPositiveButton("OK", null)
                .show();
    }

    // Novo diálogo com opção de continuar ou cancelar
    private void exibirDialogoDeConflitoComOpcao(Agendamento conflito, Runnable onConfirmar) {
        String mensagem = "Já existe um agendamento nesse período.";
        if (conflito != null) {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault());
            Calendar calConflitoFim = Calendar.getInstance();
            calConflitoFim.setTimeInMillis(conflito.getDataHoraInicio());
            calConflitoFim.add(Calendar.MINUTE, conflito.getTempoServico());
            String fimStr = sdf.format(calConflitoFim.getTime());
            mensagem += "\n\nConflito com: " + conflito.getNomeCliente() + " (" + conflito.getNomeServico() + ")." +
                    "\nDisponível somente após " + fimStr + "." +
                    "\n\nVocê deseja assim mesmo continuar com o agendamento?";
        } else {
            mensagem += "\n\nVocê deseja assim mesmo continuar com o agendamento?";
        }

        new AlertDialog.Builder(this)
                .setTitle("Conflito de Horário")
                .setMessage(mensagem)
                .setPositiveButton("Sim", (dialog, which) -> {
                    if (onConfirmar != null) {
                        onConfirmar.run();
                    }
                })
                .setNegativeButton("Não", (dialog, which) -> {
                    Toast.makeText(this, "Agendamento cancelado.", Toast.LENGTH_SHORT).show();
                })
                .show();
    }




    private long calcularDataHoraInicio() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(dataSelecionadaNoCalendario);
        calendar.set(Calendar.HOUR_OF_DAY, timePicker.getHour());
        calendar.set(Calendar.MINUTE, timePicker.getMinute());
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void carregarClientes() {
        listaClientes = clienteDAO.getAllClientes();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        for (Cliente cliente : listaClientes) {
            adapter.add(cliente.getNome());
        }
        spinnerCliente.setAdapter(adapter);
    }

    private void carregarServicos() {
        listaServicos = servicoDAO.getAllServicos();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        for (Servico servico : listaServicos) {
            adapter.add(servico.getNome());
        }
        spinnerServico.setAdapter(adapter);
    }

    @Override
    protected void onDestroy() {
        clienteDAO.close();
        servicoDAO.close();
        agendamentoDAO.close();
        super.onDestroy();
    }
}
