package com.example.appdetestes;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
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
                salvarServico();
            }
        });

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

    private void salvarServico() {
        String nome = editTextNomeServico.getText().toString();
        String tempoStr = editTextTempoServico.getText().toString();

        if (nome.isEmpty() || tempoStr.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
            return;
        }

        int tempo = Integer.parseInt(tempoStr);

        Servico servico = new Servico();
        servico.setNome(nome);
        servico.setTempo(tempo);

        long id = servicoDAO.inserirServico(servico);
        Toast.makeText(this, "Serviço salvo com ID: " + id, Toast.LENGTH_SHORT).show();

        editTextNomeServico.setText("");
        editTextTempoServico.setText("");
        atualizarListaServicos();
    }

    private void atualizarListaServicos() {
        List<Servico> servicos = servicoDAO.getAllServicos();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        for (Servico servico : servicos) {
            adapter.add(servico.getNome() + " - " + servico.getTempo() + " min");
        }
        listViewServicos.setAdapter(adapter);
    }

    @Override
    protected void onDestroy() {
        servicoDAO.close();
        super.onDestroy();
    }
}
