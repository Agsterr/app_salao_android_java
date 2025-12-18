package com.focodevsistemas.gerenciamento;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

public class ServicoListActivity extends AppCompatActivity {

    private ListView listViewServicos;
    private Button buttonAdicionarServico;
    private Button buttonVoltar;
    private TextInputEditText editTextBuscarServicos;
    private ArrayAdapter<Servico> servicosAdapter;
    private ServicoDAO servicoDAO;
    private List<Servico> todosServicos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_servico_list);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        servicoDAO = new ServicoDAO(this);
        servicoDAO.open();

        bindViews();
        setupListeners();
        atualizarListaServicos();
        registerForContextMenu(listViewServicos);
    }

    private void bindViews() {
        listViewServicos = findViewById(R.id.listViewServicos);
        buttonAdicionarServico = findViewById(R.id.buttonAdicionarServico);
        buttonVoltar = findViewById(R.id.buttonVoltar);
        editTextBuscarServicos = findViewById(R.id.editTextBuscarServicos);
    }

    private void setupListeners() {
        buttonAdicionarServico.setOnClickListener(v -> mostrarDialogServico());
        
        if (buttonVoltar != null) {
            buttonVoltar.setOnClickListener(v -> finish());
        }

        editTextBuscarServicos.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filtrarServicos(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void atualizarListaServicos() {
        todosServicos = servicoDAO.getAllServicos();
        if (servicosAdapter == null) {
            servicosAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
            listViewServicos.setAdapter(servicosAdapter);
        }
        filtrarServicos(editTextBuscarServicos.getText().toString());
    }

    private void filtrarServicos(String filtro) {
        servicosAdapter.clear();
        if (filtro == null || filtro.trim().isEmpty()) {
            for (Servico servico : todosServicos) {
                servicosAdapter.add(servico);
            }
        } else {
            String filtroLower = filtro.toLowerCase();
            for (Servico servico : todosServicos) {
                if (servico.getNome() != null && servico.getNome().toLowerCase().contains(filtroLower)) {
                    servicosAdapter.add(servico);
                }
            }
        }
        servicosAdapter.notifyDataSetChanged();
    }

    private void mostrarDialogServico() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Novo Serviço");
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_servico, null);
        android.widget.EditText editTextDialogNomeServico = dialogView.findViewById(R.id.editTextDialogNomeServico);
        android.widget.EditText editTextDialogTempoServico = dialogView.findViewById(R.id.editTextDialogTempoServico);
        builder.setView(dialogView);
        builder.setPositiveButton("Salvar", (dialog, which) -> {
            String nome = editTextDialogNomeServico.getText().toString().trim();
            String tempoStr = editTextDialogTempoServico.getText().toString().trim();
            if (nome.isEmpty() || tempoStr.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
                return;
            }
            int tempo;
            try {
                tempo = Integer.parseInt(tempoStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Tempo inválido", Toast.LENGTH_SHORT).show();
                return;
            }
            Servico servico = new Servico();
            servico.setNome(nome);
            servico.setTempo(tempo);
            long id = servicoDAO.inserirServico(servico);
            Toast.makeText(this, "Serviço salvo com ID: " + id, Toast.LENGTH_SHORT).show();
            atualizarListaServicos();
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId() == R.id.listViewServicos) {
            getMenuInflater().inflate(R.menu.menu_contexto_item, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int position = info.position;
        Servico servicoSelecionado = servicosAdapter.getItem(position);
        
        if (item.getItemId() == R.id.menu_editar) {
            mostrarDialogEditarServico(servicoSelecionado);
            return true;
        } else if (item.getItemId() == R.id.menu_apagar) {
            confirmarApagarServico(servicoSelecionado);
            return true;
        }
        return super.onContextItemSelected(item);
    }

    private void mostrarDialogEditarServico(Servico servico) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Editar Serviço");
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_servico, null);
        android.widget.EditText editTextDialogNomeServico = dialogView.findViewById(R.id.editTextDialogNomeServico);
        android.widget.EditText editTextDialogTempoServico = dialogView.findViewById(R.id.editTextDialogTempoServico);
        editTextDialogNomeServico.setText(servico.getNome());
        editTextDialogTempoServico.setText(String.valueOf(servico.getTempo()));
        builder.setView(dialogView);
        builder.setPositiveButton("Salvar", (dialog, which) -> {
            String nome = editTextDialogNomeServico.getText().toString().trim();
            String tempoStr = editTextDialogTempoServico.getText().toString().trim();
            if (nome.isEmpty() || tempoStr.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
                return;
            }
            int tempo;
            try {
                tempo = Integer.parseInt(tempoStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Tempo inválido", Toast.LENGTH_SHORT).show();
                return;
            }
            servico.setNome(nome);
            servico.setTempo(tempo);
            servicoDAO.atualizarServico(servico);
            Toast.makeText(this, "Serviço atualizado", Toast.LENGTH_SHORT).show();
            atualizarListaServicos();
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void confirmarApagarServico(Servico servico) {
        new AlertDialog.Builder(this)
            .setTitle("Confirmar Exclusão")
            .setMessage("Tem certeza que deseja apagar o serviço \"" + servico.getNome() + "\"?")
            .setPositiveButton("Sim", (dialog, which) -> {
                servicoDAO.apagarServico(servico.getId());
                Toast.makeText(this, "Serviço apagado", Toast.LENGTH_SHORT).show();
                atualizarListaServicos();
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

    @Override
    protected void onDestroy() {
        servicoDAO.close();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        atualizarListaServicos();
    }
}

