package com.example.appdetestes;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class ServicoActivity extends AppCompatActivity {

    private EditText editTextNomeServico;
    private EditText editTextTempoServico;
    private Button buttonSalvarServico;
    private ListView listViewServicos;

    private ServicoDAO servicoDAO;
    private ArrayAdapter<Servico> servicosAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_servico);

        // Habilita a seta de "voltar" na barra de título
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        servicoDAO = new ServicoDAO(this);
        servicoDAO.open();

        editTextNomeServico = findViewById(R.id.editTextNomeServico);
        editTextTempoServico = findViewById(R.id.editTextTempoServico);
        buttonSalvarServico = findViewById(R.id.buttonSalvarServico);
        listViewServicos = findViewById(R.id.listViewServicos);

        buttonSalvarServico.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarDialogServico();
            }
        });

        registerForContextMenu(listViewServicos);
        atualizarListaServicos();
    }

    // Este método é chamado quando um item do menu (como a seta de voltar) é selecionado
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed(); // Simula o clique no botão de voltar do sistema
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void mostrarDialogServico() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Novo Serviço");
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_servico, null);
        EditText editTextDialogNomeServico = dialogView.findViewById(R.id.editTextDialogNomeServico);
        EditText editTextDialogTempoServico = dialogView.findViewById(R.id.editTextDialogTempoServico);
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
            inserirServicoENotificar(nome, tempo);
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void inserirServicoENotificar(String nome, int tempo) {
        Servico servico = new Servico();
        servico.setNome(nome);
        servico.setTempo(tempo);
        long id = servicoDAO.inserirServico(servico);
        Toast.makeText(this, "Serviço salvo com ID: " + id, Toast.LENGTH_SHORT).show();
        atualizarListaServicos();
    }

    private void atualizarListaServicos() {
        List<Servico> servicos = servicoDAO.getAllServicos();
        if (servicosAdapter == null) {
            servicosAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
            listViewServicos.setAdapter(servicosAdapter);
        }
        servicosAdapter.clear();
        for (Servico servico : servicos) {
            servicosAdapter.add(servico);
        }
        servicosAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        servicoDAO.close();
        super.onDestroy();
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
        if (item.getItemId() == R.id.menu_editar) {
            Servico servicoSelecionado = servicosAdapter.getItem(position);
            mostrarDialogEditarServico(servicoSelecionado);
            return true;
        } else if (item.getItemId() == R.id.menu_apagar) {
            Servico servicoSelecionado = servicosAdapter.getItem(position);
            confirmarApagarServico(servicoSelecionado);
            return true;
        }
        return super.onContextItemSelected(item);
    }

    private void mostrarDialogEditarServico(Servico servico) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Editar Serviço");
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_servico, null);
        EditText editTextDialogNomeServico = dialogView.findViewById(R.id.editTextDialogNomeServico);
        EditText editTextDialogTempoServico = dialogView.findViewById(R.id.editTextDialogTempoServico);
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
            int rows = servicoDAO.atualizarServico(servico);
            if (rows > 0) {
                Toast.makeText(this, "Serviço atualizado", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Falha ao atualizar serviço", Toast.LENGTH_SHORT).show();
            }
            atualizarListaServicos();
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void confirmarApagarServico(Servico servico) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Apagar Serviço");
        builder.setMessage("Tem certeza que deseja apagar este serviço?");
        builder.setPositiveButton("Apagar", (dialog, which) -> {
            int rows = servicoDAO.apagarServico(servico.getId());
            if (rows > 0) {
                Toast.makeText(this, "Serviço apagado", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Falha ao apagar serviço", Toast.LENGTH_SHORT).show();
            }
            atualizarListaServicos();
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());
        builder.show();
    }
}
